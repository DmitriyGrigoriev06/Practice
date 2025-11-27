package com.courserec.recommendation

import com.courserec.recommendation.client.CourseServiceClient
import com.courserec.recommendation.client.createHttpClient
import com.courserec.recommendation.client.RatingServiceClient
import com.courserec.recommendation.config.configureCorrelationId
import com.courserec.recommendation.database.configureDatabase
import com.courserec.recommendation.database.initializeDatabase
import com.courserec.recommendation.kafka.RatingEventConsumer
import com.courserec.recommendation.kafka.getKafkaConfig
import com.courserec.recommendation.repository.RecommendationRepository
import com.courserec.recommendation.routes.configureRecommendationRoutes
import com.courserec.recommendation.service.RecommendationAlgorithm
import com.courserec.recommendation.service.RecommendationService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8084, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Database configuration
    configureDatabase()
    initializeDatabase()

    // Content Negotiation for JSON
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            }
        )
    }

    // Logging with correlation IDs
    install(CallLogging) {
        level = Level.INFO
    }

    // Correlation ID support
    configureCorrelationId()

    // Get configuration
    val courseServiceUrl = System.getenv("COURSE_SERVICE_URL")
        ?: environment.config.propertyOrNull("services.courseService.url")?.getString()
        ?: "http://course-service:8082"
    val ratingServiceUrl = System.getenv("RATING_SERVICE_URL")
        ?: environment.config.propertyOrNull("services.ratingService.url")?.getString()
        ?: "http://rating-service:8083"

    // Create HTTP client
    val httpClient = createHttpClient()

    // Create clients
    val courseServiceClient = CourseServiceClient(httpClient, courseServiceUrl)
    val ratingServiceClient = RatingServiceClient(httpClient, ratingServiceUrl)

    // Create services
    val recommendationRepository = RecommendationRepository()
    val recommendationAlgorithm = RecommendationAlgorithm()
    val recommendationService = RecommendationService(
        recommendationRepository,
        ratingServiceClient,
        courseServiceClient,
        recommendationAlgorithm
    )

    // Configure routes
    configureRecommendationRoutes(recommendationService, courseServiceClient)

    // Start Kafka consumer (optional - can be started separately)
    // val kafkaConfig = getKafkaConfig()
    // val ratingEventConsumer = RatingEventConsumer(kafkaConfig, recommendationService)
    // ratingEventConsumer.start()
}

