package com.courserec.recommendation.model

import com.courserec.recommendation.database.RecommendationTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal
import kotlinx.datetime.Instant
import java.util.UUID

class Recommendation(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Recommendation>(RecommendationTable)

    var userId by RecommendationTable.userId
    var courseId by RecommendationTable.courseId
    var relevanceScore by RecommendationTable.relevanceScore
    var generatedAt by RecommendationTable.generatedAt
    var rank by RecommendationTable.rank
}

