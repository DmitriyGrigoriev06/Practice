package com.courserec.recommendation.config

import com.courserec.recommendation.security.JwtTokenValidator
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import java.util.Properties

fun Application.getJwtSecret(): String {
    val config = environment.config
    return config.property("jwt.secret").getString()
}

fun Application.createJwtTokenValidator(): JwtTokenValidator {
    return JwtTokenValidator(getJwtSecret())
}

