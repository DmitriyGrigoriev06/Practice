package com.courserec.recommendation.database

import com.courserec.recommendation.model.Recommendation
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initializeDatabase() {
    transaction {
        SchemaUtils.create(RecommendationTable)
    }
}

