package com.courserec.recommendation.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import java.util.UUID

@kotlinx.serialization.Serializable
data class CourseResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    val category: String? = null
) {
    fun getIdAsUUID(): UUID = UUID.fromString(id)
}

class CourseServiceClient(
    private val httpClient: HttpClient,
    private val courseServiceUrl: String
) {
    private val cache = mutableMapOf<UUID, CachedCourse>()
    private val cacheTtl = 10 * 60 * 1000L // 10 minutes

    suspend fun getCourse(courseId: UUID): CourseResponse? {
        // Check cache
        val cached = cache[courseId]
        if (cached != null && !cached.isExpired(cacheTtl)) {
            return cached.course
        }

        // Fetch from service
        return try {
            val course = httpClient.get("$courseServiceUrl/api/v1/courses/$courseId").body<CourseResponse>()
            if (course.getIdAsUUID() == courseId) {
                cache[courseId] = CachedCourse(course, System.currentTimeMillis())
            }
            course
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCoursesBatch(courseIds: List<UUID>, jwtToken: String? = null): List<CourseResponse> {
        val idsParam = courseIds.joinToString(",")
        return try {
            val request = httpClient.get("$courseServiceUrl/api/v1/courses/batch?ids=$idsParam") {
                jwtToken?.let { 
                    header("Authorization", "Bearer $it")
                }
            }
            request.body<List<CourseResponse>>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private data class CachedCourse(
        val course: CourseResponse,
        val timestamp: Long
    ) {
        fun isExpired(ttl: Long): Boolean = System.currentTimeMillis() - timestamp > ttl
    }
}

