package com.courserec.recommendation.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import java.util.UUID

@kotlinx.serialization.Serializable
data class RatingResponse(
    val id: String,
    val userId: String,
    val courseId: String,
    val ratingValue: Int
) {
    fun getIdAsUUID(): UUID = UUID.fromString(id)
    fun getUserIdAsUUID(): UUID = UUID.fromString(userId)
    fun getCourseIdAsUUID(): UUID = UUID.fromString(courseId)
}

@kotlinx.serialization.Serializable
data class RatingPageResponse(
    val content: List<RatingResponse>,
    val totalElements: Long
)

class RatingServiceClient(
    private val httpClient: HttpClient,
    private val ratingServiceUrl: String
) {
    private val cache = mutableMapOf<UUID, CachedRatings>()
    private val cacheTtl = 5 * 60 * 1000L // 5 minutes

    suspend fun getUserRatings(userId: UUID, jwtToken: String? = null): List<RatingResponse> {
        // Check cache
        val cached = cache[userId]
        if (cached != null && !cached.isExpired(cacheTtl)) {
            return cached.ratings
        }

        // Fetch from service
        return try {
            val request = httpClient.get("$ratingServiceUrl/api/v1/ratings?userId=$userId&size=1000") {
                jwtToken?.let { 
                    header("Authorization", "Bearer $it")
                }
            }
            val response = request.body<RatingPageResponse>()
            cache[userId] = CachedRatings(response.content, System.currentTimeMillis())
            response.content
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllRatings(jwtToken: String? = null): List<RatingResponse> {
        return try {
            val request = httpClient.get("$ratingServiceUrl/api/v1/ratings?size=10000") {
                jwtToken?.let { 
                    header("Authorization", "Bearer $it")
                }
            }
            val response = request.body<RatingPageResponse>()
            response.content
        } catch (e: Exception) {
            emptyList()
        }
    }

    private data class CachedRatings(
        val ratings: List<RatingResponse>,
        val timestamp: Long
    ) {
        fun isExpired(ttl: Long): Boolean = System.currentTimeMillis() - timestamp > ttl
    }
}

