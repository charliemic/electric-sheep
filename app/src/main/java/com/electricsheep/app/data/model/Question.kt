package com.electricsheep.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a trivia/pub quiz question.
 * 
 * @param id Unique identifier for the question
 * @param questionText The question text
 * @param correctAnswer The correct answer
 * @param incorrectAnswers List of incorrect answer options
 * @param category The category/topic of the question (e.g., "History", "Science", "Sports")
 * @param difficulty The difficulty level ("easy", "medium", "hard")
 * @param type The question type (e.g., "multiple", "boolean")
 */
@Serializable
data class Question(
    val id: String,
    @SerialName("question")
    val questionText: String,
    @SerialName("correct_answer")
    val correctAnswer: String,
    @SerialName("incorrect_answers")
    val incorrectAnswers: List<String>,
    val category: String? = null,
    val difficulty: String? = null,
    val type: String? = null
) {
    /**
     * Get all answer options (correct + incorrect) in a shuffled order.
     * This is useful for displaying multiple choice options.
     */
    fun getAllAnswers(): List<String> {
        return (incorrectAnswers + correctAnswer).shuffled()
    }
    
    /**
     * Check if the provided answer is correct.
     */
    fun isCorrectAnswer(answer: String): Boolean {
        return answer.trim().equals(correctAnswer.trim(), ignoreCase = true)
    }
    
    /**
     * Validate that the question has required fields.
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && 
               questionText.isNotBlank() && 
               correctAnswer.isNotBlank() && 
               incorrectAnswers.isNotEmpty()
    }
}

