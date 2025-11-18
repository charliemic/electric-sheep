package com.electricsheep.app.data.local

import com.electricsheep.app.error.DataError
import com.electricsheep.app.util.Logger

/**
 * Utility for handling Room database exceptions and converting them to DataError.
 * 
 * Room can throw various exceptions that should be converted to appropriate DataError types:
 * - SQLiteConstraintException -> DataError.ConstraintViolation
 * - SQLiteException -> DataError.Corruption (for certain cases)
 * - IllegalStateException -> DataError (for migration/initialization issues)
 */
object RoomErrorHandler {
    
    /**
     * Wrap a Room database operation and convert exceptions to DataError.
     * 
     * @param operation The database operation to execute
     * @param context Additional context for error messages
     * @return Result containing the operation result or DataError
     */
    suspend fun <T> wrapRoomOperation(
        operation: suspend () -> T,
        context: String = "database operation"
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // Constraint violations (duplicate key, foreign key, etc.)
            val constraint = extractConstraintName(e)
            val error = DataError.ConstraintViolation(constraint, e)
            error.log("RoomErrorHandler", context)
            Result.failure(error)
        } catch (e: android.database.sqlite.SQLiteException) {
            // SQLite-specific errors
            val error = when {
                e.message?.contains("corrupt", ignoreCase = true) == true ||
                e.message?.contains("database disk image", ignoreCase = true) == true -> {
                    DataError.Corruption(e)
                }
                e.message?.contains("no such table", ignoreCase = true) == true ||
                e.message?.contains("no such column", ignoreCase = true) == true -> {
                    // Table/column doesn't exist - likely migration issue
                    DataError.Corruption(e)
                }
                else -> {
                    // Generic SQLite error - convert to Unknown DataError
                    DataError.Unknown(e)
                }
            }
            error.log("RoomErrorHandler", context)
            Result.failure(error)
        } catch (e: IllegalStateException) {
            // Room initialization or migration errors
            val error = when {
                e.message?.contains("migration", ignoreCase = true) == true -> {
                    DataError.Corruption(e) // Migration failures indicate corruption
                }
                e.message?.contains("database", ignoreCase = true) == true -> {
                    DataError.Corruption(e)
                }
                else -> {
                    DataError.Unknown(e)
                }
            }
            error.log("RoomErrorHandler", context)
            Result.failure(error)
        } catch (e: Exception) {
            // Other exceptions - log and return as generic DataError
            Logger.error("RoomErrorHandler", "Unexpected error in $context", e)
            val error = DataError.Unknown(e)
            error.log("RoomErrorHandler", context)
            Result.failure(error)
        }
    }
    
    /**
     * Extract constraint name from SQLiteConstraintException.
     * 
     * SQLite constraint exceptions typically have messages like:
     * "UNIQUE constraint failed: moods.id"
     * "FOREIGN KEY constraint failed"
     */
    private fun extractConstraintName(exception: android.database.sqlite.SQLiteConstraintException): String? {
        val message = exception.message ?: return null
        
        // Try to extract constraint name from common patterns
        val uniqueMatch = Regex("UNIQUE constraint failed: (\\w+\\.\\w+)").find(message)
        if (uniqueMatch != null) {
            return uniqueMatch.groupValues[1]
        }
        
        val foreignKeyMatch = Regex("FOREIGN KEY constraint failed").find(message)
        if (foreignKeyMatch != null) {
            return "foreign_key"
        }
        
        val checkMatch = Regex("CHECK constraint failed: (\\w+)").find(message)
        if (checkMatch != null) {
            return checkMatch.groupValues[1]
        }
        
        return null
    }
    
}

