package com.electricsheep.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user's answer to a specific question within a quiz session.
 * Used to track individual question performance for learning pattern analysis.
 * 
 * @param id Unique identifier for the answer record
 * @param sessionId ID of the quiz session this answer belongs to
 * @param userId ID of the user who provided the answer
 * @param questionId ID of the question that was answered
 * @param userAnswer The answer provided by the user
 * @param isCorrect Whether the answer was correct
 * @param timeTaken Time taken to answer in milliseconds (optional)
 * @param category The category of the question (for pattern analysis)
 * @param difficulty The difficulty level of the question
 * @param answeredAt Timestamp when the answer was provided (Unix timestamp in milliseconds)
 * @param createdAt When the record was created in the system
 * @param updatedAt When the record was last updated
 */
@Entity(tableName = "quiz_answers")
@Serializable
data class QuizAnswer(
    @PrimaryKey
    val id: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("question_id")
    val questionId: String,
    @SerialName("user_answer")
    val userAnswer: String,
    @SerialName("is_correct")
    val isCorrect: Boolean,
    @SerialName("time_taken")
    val timeTaken: Long? = null,
    val category: String? = null,
    val difficulty: String? = null,
    @SerialName("answered_at")
    val answeredAt: Long,
    @SerialName("created_at")
    val createdAt: Long? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null
) {
    /**
     * Validate that the answer has required fields.
     */
    fun isValid(): Boolean {
        return sessionId.isNotBlank() && 
               userId.isNotBlank() && 
               questionId.isNotBlank() && 
               userAnswer.isNotBlank() &&
               answeredAt > 0
    }
    
    /**
     * Create a copy with updated timestamp set to current time.
     */
    fun withUpdatedTimestamp(): QuizAnswer {
        val now = System.currentTimeMillis()
        return copy(
            updatedAt = now,
            createdAt = createdAt ?: now
        )
    }
}

