package com.courserec.recommendation.model.dto

import com.courserec.recommendation.client.CourseResponse as ClientCourseResponse
import com.courserec.recommendation.model.Recommendation

@kotlinx.serialization.Serializable
data class RecommendationResponse(
    val recommendations: List<RecommendationItem>
)

@kotlinx.serialization.Serializable
data class RecommendationItem(
    val courseId: String,
    val course: CourseResponse?,
    val relevanceScore: Double,
    val rank: Int
)

@kotlinx.serialization.Serializable
data class CourseResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    val category: String? = null
)

fun Recommendation.toRecommendationItem(course: ClientCourseResponse?): RecommendationItem {
    val courseResponse = course?.let {
        CourseResponse(
            id = it.id,
            title = it.title,
            description = it.description,
            category = it.category
        )
    }
    return RecommendationItem(
        courseId = this.courseId.toString(),
        course = courseResponse,
        relevanceScore = this.relevanceScore.toDouble(),
        rank = this.rank
    )
}

