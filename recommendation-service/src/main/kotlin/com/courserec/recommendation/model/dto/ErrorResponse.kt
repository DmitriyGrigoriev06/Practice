package com.courserec.recommendation.model.dto

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ErrorResponse(
    val error: ErrorDetail
) {
    @Serializable
    data class ErrorDetail(
        val code: String,
        val message: String,
        val timestamp: String = Instant.now().toString(),
        val details: Map<String, String> = emptyMap()
    )
}

