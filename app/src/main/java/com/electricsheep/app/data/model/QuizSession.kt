package com.electricsheep.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a quiz session where a user answers multiple questions.
 * Used to track performance and learning patterns.
 * 
 * @param id Unique identifier for the session
 * @param userId ID of the user who took the quiz
 * @param totalQuestions Total number of questions in the session
 * @param correctAnswers Number of correct answers
 * @param category Optional category filter that was used (null = all categories)
 * @param difficulty Optional difficulty level that was requested (null = mixed)
 * @param startedAt Timestamp when the session started (Unix timestamp in milliseconds)
 * @param completedAt Timestamp when the session was completed (null if not completed)
 * @param createdAt When the record was created in the system
 * @param updatedAt When the record was last updated
 */
@Entity(tableName = "quiz_sessions")
@Serializable
data class QuizSession(
    @PrimaryKey
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("total_questions")
    val totalQuestions: Int,
    @SerialName("correct_answers")
    val correctAnswers: Int,
    val category: String? = null,
    val difficulty: String? = null,
    @SerialName("started_at")
    val startedAt: Long,
    @SerialName("completed_at")
    val completedAt: Long? = null,
    @SerialName("created_at")
    val createdAt: Long? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null
) {
    /**
     * Calculate the percentage score for this session.
     */
    fun getScorePercentage(): Double {
        if (totalQuestions == 0) return 0.0
        return (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100.0
    }
    
    /**
     * Check if the session is completed.
     */
    fun isCompleted(): Boolean {
        return completedAt != null
    }
    
    /**
     * Validate that the session has valid data.
     */
    fun isValid(): Boolean {
        return userId.isNotBlank() && 
               totalQuestions > 0 && 
               correctAnswers >= 0 && 
               correctAnswers <= totalQuestions &&
               startedAt > 0
    }
    
    /**
     * Create a copy with updated timestamp set to current time.
     */
    fun withUpdatedTimestamp(): QuizSession {
        val now = System.currentTimeMillis()
        return copy(
            updatedAt = now,
            createdAt = createdAt ?: now
        )
    }
    
    /**
     * Mark the session as completed.
     */
    fun markCompleted(): QuizSession {
        return copy(
            completedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}

