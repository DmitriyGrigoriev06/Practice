package com.courserec.recommendation.repository

import com.courserec.recommendation.database.RecommendationTable
import com.courserec.recommendation.model.Recommendation
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import java.util.UUID

class RecommendationRepository {
    fun saveRecommendations(recommendations: List<RecommendationData>) {
        transaction {
            // Delete old recommendations for the user
            val userId = recommendations.firstOrNull()?.userId
            if (userId != null) {
                RecommendationTable.deleteWhere { RecommendationTable.userId eq userId }
            }

            // Insert new recommendations
            recommendations.forEach { data ->
                Recommendation.new {
                    this.userId = data.userId
                    this.courseId = data.courseId
                    this.relevanceScore = data.relevanceScore
                    this.generatedAt = Clock.System.now()
                    this.rank = data.rank
                }
            }
        }
    }

    fun getRecommendationsByUserId(userId: UUID, limit: Int): List<Recommendation> {
        return transaction {
            Recommendation.find {
                RecommendationTable.userId eq userId
            }
                .sortedBy { it.rank }
                .take(limit)
                .toList()
        }
    }
}

data class RecommendationData(
    val userId: UUID,
    val courseId: UUID,
    val relevanceScore: BigDecimal,
    val rank: Int
)

