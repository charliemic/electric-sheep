package com.electricsheep.testautomation.perception

import org.slf4j.LoggerFactory
import java.io.File

/**
 * Manages predictions using Predictive Processing model.
 * 
 * Generates predictions before actions, verifies them after actions,
 * and updates predictions based on observations.
 */
class PredictionManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val activePredictions = mutableListOf<Prediction>()
    private val predictionHistory = mutableListOf<Prediction>()
    
    /**
     * Represents a prediction about what should happen.
     */
    data class Prediction(
        val id: String,
        val action: String, // Action that triggered this prediction
        val expectedState: String, // What we expect to see
        val expectedElements: List<String>, // Elements we expect to be visible
        val timestamp: Long = System.currentTimeMillis(),
        var verified: Boolean = false,
        var verificationResult: VerificationResult? = null,
        var verificationScreenshot: File? = null,
        var observedState: String? = null, // What we actually observed
        var observedElements: List<String> = emptyList() // Elements actually visible
    ) {
        /**
         * Check if prediction matches observed state.
         * More lenient matching: if task succeeded, partial matches are acceptable.
         */
        fun matches(observedState: String, observedElements: List<String>, taskSucceeded: Boolean = false): Boolean {
            // Store observed state for reporting
            this.observedState = observedState
            this.observedElements = observedElements
            
            // Exact match
            val stateMatches = observedState.contains(expectedState, ignoreCase = true) ||
                              expectedState.contains(observedState, ignoreCase = true) ||
                              expectedState.isEmpty() || observedState.isEmpty() // "Current screen" predictions
            val elementsMatch = if (expectedElements.isEmpty()) {
                true // No specific elements expected
            } else {
                // At least some elements match (fuzzy matching)
                expectedElements.any { expected ->
                    observedElements.any { it.contains(expected, ignoreCase = true) }
                }
            }
            
            // If task succeeded, be more lenient - partial matches are acceptable
            if (taskSucceeded) {
                return stateMatches || elementsMatch
            }
            
            return stateMatches && elementsMatch
        }
    }
    
    /**
     * Result of verifying a prediction.
     */
    enum class VerificationResult {
        CONFIRMED,      // Prediction matched reality
        PARTIAL,        // Some aspects matched, some didn't
        REJECTED,       // Prediction did not match reality
        PENDING         // Not yet verified
    }
    
    /**
     * Generate a prediction before an action.
     * 
     * @param action Description of action about to be performed
     * @param expectedState What screen/state we expect to see after action
     * @param expectedElements What elements we expect to be visible
     * @return Prediction ID for later verification
     */
    fun generatePrediction(
        action: String,
        expectedState: String,
        expectedElements: List<String> = emptyList()
    ): String {
        val predictionId = "pred_${System.currentTimeMillis()}"
        val prediction = Prediction(
            id = predictionId,
            action = action,
            expectedState = expectedState,
            expectedElements = expectedElements
        )
        
        activePredictions.add(prediction)
        logger.debug("Generated prediction: After '$action', expect '$expectedState' with elements: ${expectedElements.joinToString()}")
        
        return predictionId
    }
    
    /**
     * Verify a prediction against observed state.
     * 
     * @param predictionId ID of prediction to verify
     * @param observedState What we actually observed
     * @param observedElements Elements actually visible
     * @param screenshot Screenshot of observed state
     * @return Verification result
     */
    fun verifyPrediction(
        predictionId: String,
        observedState: String,
        observedElements: List<String>,
        screenshot: File? = null,
        taskSucceeded: Boolean = false // Context: did the overall task succeed?
    ): VerificationResult {
        val prediction = activePredictions.find { it.id == predictionId }
        if (prediction == null) {
            logger.warn("Prediction not found: $predictionId")
            return VerificationResult.REJECTED
        }
        
        // Store observed state in prediction
        prediction.observedState = observedState
        prediction.observedElements = observedElements
        
        val matches = prediction.matches(observedState, observedElements, taskSucceeded)
        val result = when {
            matches -> VerificationResult.CONFIRMED
            // If task succeeded, PARTIAL is acceptable (not a problem)
            taskSucceeded && (observedState.isNotEmpty() || observedElements.isNotEmpty()) -> VerificationResult.PARTIAL
            // If task failed, PARTIAL might indicate an issue
            observedState.isNotEmpty() && prediction.expectedState.isNotEmpty() -> VerificationResult.PARTIAL
            else -> VerificationResult.REJECTED
        }
        
        prediction.verified = true
        prediction.verificationResult = result
        prediction.verificationScreenshot = screenshot
        
        // Move to history
        activePredictions.remove(prediction)
        predictionHistory.add(prediction)
        
        logger.debug("Verified prediction: $result (expected: '${prediction.expectedState}', observed: '$observedState', taskSucceeded: $taskSucceeded)")
        
        return result
    }
    
    /**
     * Get active predictions (not yet verified).
     */
    fun getActivePredictions(): List<Prediction> = activePredictions.toList()
    
    /**
     * Get prediction history.
     */
    fun getPredictionHistory(): List<Prediction> = predictionHistory.toList()
    
    /**
     * Get latest prediction for an action.
     */
    fun getLatestPredictionForAction(action: String): Prediction? {
        return predictionHistory.lastOrNull { it.action == action }
    }
    
    /**
     * Clear active predictions (e.g., when starting new test).
     */
    fun clearActivePredictions() {
        activePredictions.clear()
        logger.debug("Cleared active predictions")
    }
    
    /**
     * Reset prediction manager (for new test run).
     */
    fun reset() {
        activePredictions.clear()
        predictionHistory.clear()
        logger.debug("Prediction manager reset")
    }
}

