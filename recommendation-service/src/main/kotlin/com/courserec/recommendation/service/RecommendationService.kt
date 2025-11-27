package com.courserec.recommendation.service

import com.courserec.recommendation.client.CourseServiceClient
import com.courserec.recommendation.client.RatingResponse
import com.courserec.recommendation.client.RatingServiceClient
import com.courserec.recommendation.model.Recommendation
import com.courserec.recommendation.repository.RecommendationData
import com.courserec.recommendation.repository.RecommendationRepository
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RecommendationService(
    private val recommendationRepository: RecommendationRepository,
    private val ratingServiceClient: RatingServiceClient,
    private val courseServiceClient: CourseServiceClient,
    private val recommendationAlgorithm: RecommendationAlgorithm
) {
    private val logger = LoggerFactory.getLogger(RecommendationService::class.java)
    private val cache = ConcurrentHashMap<UUID, CachedRecommendations>()
    private val cacheTtl = 60 * 60 * 1000L // 1 hour

    suspend fun generateRecommendations(userId: UUID, limit: Int = 10, jwtToken: String? = null): List<Recommendation> {
        // Check cache first
        val cached = cache[userId]
        if (cached != null && !cached.isExpired(cacheTtl)) {
            logger.debug("Returning cached recommendations for userId: {}", userId)
            return cached.recommendations
        }

        logger.info("Generating recommendations for userId: {}, limit: {}", userId, limit)

        // Get user ratings
        val userRatings = ratingServiceClient.getUserRatings(userId, jwtToken)
        logger.info("Found {} ratings for userId: {}", userRatings.size, userId)
        val userRatingMap = userRatings.associate { it.getCourseIdAsUUID() to it.ratingValue }

        val recommendations: List<Pair<UUID, BigDecimal>>

        // Use collaborative filtering if user has >= 3 ratings, otherwise use popularity-based
        if (userRatings.size >= 3) {
            logger.debug("Using collaborative filtering for userId: {} ({} ratings)", userId, userRatings.size)
            
            // Get all ratings for similarity calculation
            val allRatings = ratingServiceClient.getAllRatings(jwtToken)
            logger.info("Found {} total ratings in system", allRatings.size)
            val allUserRatings = allRatings
                .groupBy { it.getUserIdAsUUID() }
                .mapValues { it.value.associate { rating -> rating.getCourseIdAsUUID() to rating.ratingValue } }
                .filter { it.value.size >= 3 } // Only consider users with at least 3 ratings

            logger.info("Found {} users with >= 3 ratings", allUserRatings.size)
            val allCourseIds = allRatings.map { it.getCourseIdAsUUID() }.toSet()

            recommendations = recommendationAlgorithm.generateCollaborativeFilteringRecommendations(
                userId,
                userRatingMap,
                allUserRatings,
                allCourseIds,
                limit
            )
            logger.info("Generated {} collaborative filtering recommendations", recommendations.size)
        } else {
            logger.debug("Using popularity-based recommendations for userId: {} ({} ratings)", userId, userRatings.size)
            
            // Get all ratings for popularity calculation
            val allRatings = ratingServiceClient.getAllRatings(jwtToken)
            logger.info("Found {} total ratings in system for popularity calculation", allRatings.size)
            val excludeCourseIds = userRatingMap.keys.toSet()

            recommendations = recommendationAlgorithm.generatePopularityBasedRecommendations(
                allRatings,
                excludeCourseIds,
                limit
            )
            logger.info("Generated {} popularity-based recommendations", recommendations.size)
        }
        
        if (recommendations.isEmpty()) {
            logger.warn("No recommendations generated for userId: {}. Possible reasons: no ratings in system, no courses with >= 5 ratings, or insufficient data", userId)
            return emptyList()
        }

        // Convert to RecommendationData and save
        val recommendationData = recommendations.mapIndexed { index, (courseId, score) ->
            RecommendationData(userId, courseId, score, index + 1)
        }

        recommendationRepository.saveRecommendations(recommendationData)

        // Fetch recommendations from database
        val savedRecommendations = recommendationRepository.getRecommendationsByUserId(userId, limit)

        // Cache the results
        cache[userId] = CachedRecommendations(savedRecommendations, System.currentTimeMillis())

        logger.info("Generated {} recommendations for userId: {}", savedRecommendations.size, userId)
        return savedRecommendations
    }

    fun invalidateCache(userId: UUID) {
        cache.remove(userId)
        logger.debug("Invalidated recommendation cache for userId: {}", userId)
    }

    private data class CachedRecommendations(
        val recommendations: List<Recommendation>,
        val timestamp: Long
    ) {
        fun isExpired(ttl: Long): Boolean = System.currentTimeMillis() - timestamp > ttl
    }
}

