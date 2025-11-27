package com.courserec.recommendation.unit.service

import com.courserec.recommendation.service.RecommendationAlgorithm
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecommendationAlgorithmTest {
    private val algorithm = RecommendationAlgorithm()

    @Test
    fun testCalculateCosineSimilarity() {
        val user1Ratings = mapOf(
            UUID.randomUUID() to 5,
            UUID.randomUUID() to 4,
            UUID.randomUUID() to 3
        )
        val user2Ratings = mapOf(
            user1Ratings.keys.elementAt(0) to 5,
            user1Ratings.keys.elementAt(1) to 4,
            user1Ratings.keys.elementAt(2) to 3
        )

        val similarity = algorithm.calculateCosineSimilarity(user1Ratings, user2Ratings)
        assertEquals(1.0, similarity, 0.01)
    }

    @Test
    fun testCalculateCosineSimilarityNoCommonCourses() {
        val user1Ratings = mapOf(UUID.randomUUID() to 5)
        val user2Ratings = mapOf(UUID.randomUUID() to 4)

        val similarity = algorithm.calculateCosineSimilarity(user1Ratings, user2Ratings)
        assertEquals(0.0, similarity)
    }

    @Test
    fun testGeneratePopularityBasedRecommendations() {
        val ratings = listOf(
            com.courserec.recommendation.client.RatingResponse(
                id = UUID.randomUUID().toString(),
                userId = UUID.randomUUID().toString(),
                courseId = UUID.randomUUID().toString(),
                ratingValue = 5
            ),
            com.courserec.recommendation.client.RatingResponse(
                id = UUID.randomUUID().toString(),
                userId = UUID.randomUUID().toString(),
                courseId = UUID.randomUUID().toString(),
                ratingValue = 4
            )
        )

        val recommendations = algorithm.generatePopularityBasedRecommendations(
            ratings,
            emptySet(),
            10
        )

        assertTrue(recommendations.isNotEmpty())
    }
}

