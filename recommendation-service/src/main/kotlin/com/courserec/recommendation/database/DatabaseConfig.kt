package com.courserec.recommendation.database

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase(): Database {
    val url = System.getenv("DATABASE_URL") 
        ?: environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:postgresql://recommendation-db:5432/recommendation_db"
    
    val user = System.getenv("DATABASE_USER")
        ?: environment.config.propertyOrNull("database.user")?.getString()
        ?: "postgres"
    
    val password = System.getenv("DATABASE_PASSWORD")
        ?: environment.config.propertyOrNull("database.password")?.getString()
        ?: "postgres"
    
    val driver = environment.config.propertyOrNull("database.driver")?.getString()
        ?: "org.postgresql.Driver"

    val database = Database.connect(
        url = url,
        user = user,
        password = password,
        driver = driver
    )

    return database
}

