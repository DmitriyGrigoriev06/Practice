package com.courserec.recommendation.routes

import com.courserec.recommendation.client.CourseServiceClient
import com.courserec.recommendation.model.dto.ErrorResponse
import com.courserec.recommendation.model.dto.RecommendationItem
import com.courserec.recommendation.model.dto.RecommendationResponse
import com.courserec.recommendation.model.dto.toRecommendationItem
import com.courserec.recommendation.service.RecommendationService
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import java.util.UUID

fun Application.configureRecommendationRoutes(
    recommendationService: RecommendationService,
    courseServiceClient: CourseServiceClient
) {
    val logger = LoggerFactory.getLogger("RecommendationRoutes")

    routing {
        get("/health") {
            call.respond(io.ktor.http.HttpStatusCode.OK, mapOf("status" to "UP"))
        }
        
        get("/api/v1/recommendations/{userId}") {
            // OpenAPI documentation: GET /api/v1/recommendations/{userId}?limit=10
            // Returns personalized course recommendations for a user
            val userIdParam = call.parameters["userId"]
            val limitParam = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

            // Validate limit
            if (limitParam < 1 || limitParam > 50) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = ErrorResponse.ErrorDetail(
                            code = "INVALID_LIMIT",
                            message = "Limit must be between 1 and 50",
                            details = mapOf("limit" to limitParam.toString())
                        )
                    )
                )
                return@get
            }

            if (userIdParam == null) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = ErrorResponse.ErrorDetail(
                            code = "MISSING_USER_ID",
                            message = "User ID is required",
                            details = emptyMap()
                        )
                    )
                )
                return@get
            }

            val userId = try {
                UUID.fromString(userIdParam)
            } catch (e: IllegalArgumentException) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = ErrorResponse.ErrorDetail(
                            code = "INVALID_USER_ID",
                            message = "Invalid user ID format",
                            details = mapOf("userId" to userIdParam)
                        )
                    )
                )
                return@get
            }

            val correlationId = call.request.header("X-Correlation-ID") ?: ""
            val authHeader = call.request.header("Authorization")
            val jwtToken = if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader.substring(7)
            } else {
                null
            }
            
            logger.info("Getting recommendations for userId: {}, limit: {}, correlationId: {}", userId, limitParam, correlationId)

            try {
                // Generate or get cached recommendations
                val recommendations = recommendationService.generateRecommendations(userId, limitParam, jwtToken)

                if (recommendations.isEmpty()) {
                    call.respond(
                        io.ktor.http.HttpStatusCode.NotFound,
                        ErrorResponse(
                            error = ErrorResponse.ErrorDetail(
                                code = "NO_RECOMMENDATIONS",
                                message = "No recommendations found for user",
                                details = mapOf("userId" to userId.toString())
                            )
                        )
                    )
                    return@get
                }

                // Fetch course details for recommendations
                val courseIds = recommendations.map { it.courseId }
                val courses = courseServiceClient.getCoursesBatch(courseIds, jwtToken)
                val courseMap = courses.associateBy { UUID.fromString(it.id) }

                // Build response
                val recommendationItems = recommendations.map { recommendation ->
                    val course = courseMap[recommendation.courseId]
                    recommendation.toRecommendationItem(course)
                }

                val response = RecommendationResponse(recommendationItems)

                logger.info("Returning {} recommendations for userId: {}", recommendationItems.size, userId)
                call.respond(io.ktor.http.HttpStatusCode.OK, response)
            } catch (e: Exception) {
                logger.error("Error generating recommendations for userId: {}", userId, e)
                call.respond(
                    io.ktor.http.HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = ErrorResponse.ErrorDetail(
                            code = "INTERNAL_ERROR",
                            message = "Error generating recommendations",
                            details = emptyMap()
                        )
                    )
                )
            }
        }
    }
}

