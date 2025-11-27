package com.courserec.recommendation.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.UUID

object RecommendationTable : UUIDTable("recommendations") {
    val userId = uuid("user_id")
    val courseId = uuid("course_id")
    val relevanceScore = decimal("relevance_score", 5, 4)
    val generatedAt = timestamp("generated_at")
    val rank = integer("rank")

    init {
        index(isUnique = false, userId, generatedAt, rank)
        index(isUnique = false, userId)
        index(isUnique = false, courseId)
    }
}

