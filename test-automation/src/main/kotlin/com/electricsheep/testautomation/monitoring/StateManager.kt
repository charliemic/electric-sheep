package com.electricsheep.testautomation.monitoring

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

/**
 * Manages screen state and coordinates between Screen Monitor and Action Executor.
 * 
 * Provides:
 * - Current state tracking
 * - State change detection
 * - State queries for AI Planner
 * - Coordination between observer and actor
 */
class StateManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var currentState: ScreenState? = null
    private val stateLock = Mutex()
    private val stateChangeChannel = Channel<ScreenState>(Channel.UNLIMITED)
    private var stateChangeListeners = mutableListOf<(ScreenState) -> Unit>()
    
    /**
     * Called by Screen Monitor when state changes.
     */
    suspend fun onStateChanged(newState: ScreenState, oldState: ScreenState?) {
        stateLock.withLock {
            val changed = !newState.hasChangedFrom(currentState)
            if (changed) {
                val previous = currentState
                currentState = newState
                
                // Notify listeners
                stateChangeChannel.trySend(newState)
                stateChangeListeners.forEach { 
                    try {
                        it(newState)
                    } catch (e: Exception) {
                        logger.warn("State change listener error", e)
                    }
                }
                
                logger.debug("State changed: ${previous?.screenName ?: "unknown"} → ${newState.screenName ?: "unknown"}")
            }
        }
    }
    
    /**
     * Get current state (thread-safe).
     */
    suspend fun getCurrentState(): ScreenState? {
        return stateLock.withLock { currentState }
    }
    
    /**
     * Wait for state to match a predicate.
     * 
     * @param predicate Function that returns true when desired state is reached
     * @param timeoutMs Maximum time to wait
     * @return The state that matched, or null if timeout
     */
    suspend fun waitForState(
        predicate: (ScreenState) -> Boolean,
        timeoutMs: Long = 10000
    ): ScreenState? {
        // Check current state first
        val current = getCurrentState()
        if (current != null && predicate(current)) {
            return current
        }
        
        // Wait for state change
        return try {
            withTimeout(timeoutMs) {
                var result: ScreenState? = null
                while (result == null) {
                    val newState = stateChangeChannel.receive()
                    if (predicate(newState)) {
                        result = newState
                    }
                }
                result
            }
        } catch (e: Exception) {
            logger.debug("Timeout waiting for state: ${e.message}")
            null
        }
    }
    
    /**
     * Wait for state to stabilize (remain unchanged for a duration).
     * 
     * @param stableDurationMs How long state must remain unchanged
     * @param timeoutMs Maximum time to wait
     * @return Stable state, or null if timeout
     */
    suspend fun waitForStateStable(
        stableDurationMs: Long = 2000,
        timeoutMs: Long = 10000
    ): ScreenState? {
        var lastState = getCurrentState()
        var lastChangeTime = System.currentTimeMillis()
        val startTime = System.currentTimeMillis()
        
        // Check if already stable
        if (lastState != null) {
            delay(stableDurationMs)
            val current = getCurrentState()
            if (current == lastState) {
                return lastState
            }
        }
        
        // Wait for state to stabilize
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                val newState = withTimeout(1000) {
                    stateChangeChannel.receive()
                }
                
                if (newState != lastState) {
                    lastState = newState
                    lastChangeTime = System.currentTimeMillis()
                } else if (System.currentTimeMillis() - lastChangeTime >= stableDurationMs) {
                    return lastState // State is stable
                }
            } catch (e: Exception) {
                // Timeout or channel closed - check if current state is stable
                val current = getCurrentState()
                if (current == lastState && System.currentTimeMillis() - lastChangeTime >= stableDurationMs) {
                    return lastState
                }
            }
        }
        
        return lastState
    }
    
    /**
     * Check if state has changed since last check.
     */
    fun hasStateChanged(): Boolean {
        return stateChangeChannel.tryReceive().isSuccess
    }
    
    /**
     * Add a listener for state changes.
     */
    fun addStateChangeListener(listener: (ScreenState) -> Unit) {
        stateChangeListeners.add(listener)
    }
    
    /**
     * Remove a state change listener.
     */
    fun removeStateChangeListener(listener: (ScreenState) -> Unit) {
        stateChangeListeners.remove(listener)
    }
    
    /**
     * Reset state (for new test run).
     */
    suspend fun reset() {
        stateLock.withLock {
            currentState = null
            // Clear channel
            while (stateChangeChannel.tryReceive().isSuccess) {
                // Drain channel
            }
        }
    }
}

