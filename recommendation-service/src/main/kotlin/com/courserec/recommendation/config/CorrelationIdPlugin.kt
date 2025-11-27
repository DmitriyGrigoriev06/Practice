package com.courserec.recommendation.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import java.util.UUID

fun Application.configureCorrelationId() {
    install(CallId) {
        retrieveFromHeader("X-Correlation-ID")
        generate { UUID.randomUUID().toString() }
        verify { it.isNotEmpty() }
        replyToHeader("X-Correlation-ID")
    }
}

