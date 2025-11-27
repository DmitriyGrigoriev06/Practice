package com.courserec.recommendation.service

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.sqrt

class RecommendationAlgorithm {
    /**
     * Calculate cosine similarity between two users based on their rating vectors
     */
    fun calculateCosineSimilarity(
        user1Ratings: Map<UUID, Int>,
        user2Ratings: Map<UUID, Int>
    ): Double {
        val commonCourses = user1Ratings.keys.intersect(user2Ratings.keys)
        if (commonCourses.isEmpty()) {
            return 0.0
        }

        var dotProduct = 0.0
        var norm1 = 0.0
        var norm2 = 0.0

        for (courseId in commonCourses) {
            val rating1 = user1Ratings[courseId]!!.toDouble()
            val rating2 = user2Ratings[courseId]!!.toDouble()
            dotProduct += rating1 * rating2
            norm1 += rating1 * rating1
            norm2 += rating2 * rating2
        }

        val denominator = sqrt(norm1) * sqrt(norm2)
        return if (denominator == 0.0) 0.0 else dotProduct / denominator
    }

    /**
     * Find top N similar users to the target user
     */
    fun findSimilarUsers(
        targetUserId: UUID,
        targetUserRatings: Map<UUID, Int>,
        allUserRatings: Map<UUID, Map<UUID, Int>>,
        topN: Int = 5
    ): List<Pair<UUID, Double>> {
        val similarities = mutableListOf<Pair<UUID, Double>>()

        for ((userId, ratings) in allUserRatings) {
            if (userId == targetUserId) continue

            val similarity = calculateCosineSimilarity(targetUserRatings, ratings)
            if (similarity > 0) {
                similarities.add(Pair(userId, similarity))
            }
        }

        return similarities.sortedByDescending { it.second }.take(topN)
    }

    /**
     * Generate recommendations using collaborative filtering
     */
    fun generateCollaborativeFilteringRecommendations(
        targetUserId: UUID,
        targetUserRatings: Map<UUID, Int>,
        allUserRatings: Map<UUID, Map<UUID, Int>>,
        allCourseIds: Set<UUID>,
        limit: Int = 10
    ): List<Pair<UUID, BigDecimal>> {
        // Find similar users
        val similarUsers = findSimilarUsers(targetUserId, targetUserRatings, allUserRatings)

        if (similarUsers.isEmpty()) {
            return emptyList()
        }

        // Calculate weighted scores for courses
        val courseScores = mutableMapOf<UUID, Pair<Double, Double>>() // (weightedSum, totalWeight)

        for ((similarUserId, similarity) in similarUsers) {
            val similarUserRatings = allUserRatings[similarUserId] ?: continue

            for ((courseId, rating) in similarUserRatings) {
                // Skip courses the target user has already rated
                if (targetUserRatings.containsKey(courseId)) {
                    continue
                }

                val current = courseScores.getOrDefault(courseId, Pair(0.0, 0.0))
                val weightedRating = rating * similarity
                courseScores[courseId] = Pair(
                    current.first + weightedRating,
                    current.second + similarity
                )
            }
        }

        // Calculate final relevance scores
        val recommendations = courseScores.mapNotNull { (courseId, scores) ->
            if (scores.second > 0) {
                val relevanceScore = scores.first / scores.second
                Pair(courseId, BigDecimal(relevanceScore).setScale(4, RoundingMode.HALF_UP))
            } else {
                null
            }
        }.sortedByDescending { it.second }.take(limit)

        return recommendations
    }

    /**
     * Generate popularity-based recommendations (fallback for new users)
     */
    fun generatePopularityBasedRecommendations(
        allRatings: List<com.courserec.recommendation.client.RatingResponse>,
        excludeCourseIds: Set<UUID>,
        limit: Int = 10
    ): List<Pair<UUID, BigDecimal>> {
        // Calculate average rating and count for each course
        val courseStats = mutableMapOf<UUID, Pair<Double, Int>>() // (sum, count)

        for (rating in allRatings) {
            val courseId = rating.getCourseIdAsUUID()
            if (excludeCourseIds.contains(courseId)) {
                continue
            }

            val current = courseStats.getOrDefault(courseId, Pair(0.0, 0))
            courseStats[courseId] = Pair(
                current.first + rating.ratingValue,
                current.second + 1
            )
        }

        // Filter courses with at least 5 ratings and calculate scores
        // If no courses have 5+ ratings, lower the threshold to 1
        val minRatings = if (courseStats.values.any { it.second >= 5 }) 5 else 1
        
        val recommendations = courseStats
            .filter { it.value.second >= minRatings }
            .map { (courseId, stats) ->
                val avgRating = stats.first / stats.second
                val score = avgRating * (1.0 + Math.log10(stats.second.toDouble())) // Boost by log of count
                Pair(courseId, BigDecimal(score).setScale(4, RoundingMode.HALF_UP))
            }
            .sortedByDescending { it.second }
            .take(limit)

        return recommendations
    }
}

