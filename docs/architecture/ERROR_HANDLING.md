# Error Handling System

## Overview

The Electric Sheep app uses a comprehensive error handling system that classifies errors by type (Network, Data, System) and recoverability. This ensures consistent error handling, user-friendly messages, and appropriate retry logic throughout the application.

## Architecture

### Error Hierarchy

```
AppError (base sealed class)
├── NetworkError (network-related errors)
│   ├── NoConnection
│   ├── Timeout
│   ├── ServerError (5xx)
│   ├── ClientError (4xx)
│   ├── Unauthorized
│   ├── RateLimited
│   └── Unknown
├── DataError (database/storage errors)
│   ├── ConstraintViolation
│   ├── NotFound
│   ├── InvalidData
│   └── Corruption
└── SystemError (system-level errors)
    ├── OutOfMemory
    ├── ConfigurationError
    └── Unknown
```

### Error Properties

All errors have:
- `isRecoverable`: Whether the user can retry or take action
- `userMessage`: User-friendly error message
- `shouldRetry`: Whether to automatically retry
- `retryDelayMillis`: Delay before retry (for retryable errors)
- `log(tag, context)`: Built-in logging method

## Usage Patterns

### Creating Errors

```kotlin
// Network error
val error = NetworkError.NoConnection(exception)

// Data error
val error = DataError.InvalidData(field = "email", errorCause = exception)

// System error
val error = SystemError.ConfigurationError(config = "Supabase URL", errorCause = exception)
```

### Parsing Exceptions

```kotlin
// Parse generic exception to NetworkError
val networkError = NetworkError.fromException(exception)

// Parse generic exception to AuthError (for auth layer)
val authError = AuthError.fromException(exception)
```

### Handling Errors

```kotlin
try {
    // Operation that may fail
    remoteDataSource.insertMood(mood)
} catch (e: NetworkError) {
    e.log("ComponentName", "Operation context")
    if (e.isRecoverable && e.shouldRetry) {
        // Retry logic
    } else {
        // Show user message
        showError(e.userMessage)
    }
} catch (e: DataError) {
    e.log("ComponentName", "Operation context")
    // Handle data error
} catch (e: Exception) {
    // Convert to appropriate error type
    val systemError = SystemError.Unknown(e)
    systemError.log("ComponentName", "Operation context")
}
```

### Logging Errors

```kotlin
// Automatic logging with context
error.log("ComponentName", "Additional context")

// Recoverable errors log as WARN
// Non-recoverable errors log as ERROR
```

## Error Types

### NetworkError

**Recoverable**: Yes (typically)  
**Auto-retry**: Yes (typically)

- **NoConnection**: No internet connection
  - Retry delay: 3 seconds
  - User message: "No internet connection. Please check your network and try again."

- **Timeout**: Request timed out
  - Retry delay: 5 seconds
  - User message: "Request took too long. Please check your connection and try again."

- **ServerError**: Server error (5xx)
  - Retry delay: 10 seconds
  - User message: "Server is temporarily unavailable. Please try again in a moment."

- **ClientError**: Client error (4xx)
  - Auto-retry: No (user needs to fix input)
  - User message: Uses server message or default

- **Unauthorized**: Authentication failed
  - Auto-retry: No (requires user action)
  - User message: "Your session has expired. Please sign in again."

- **RateLimited**: Too many requests
  - Retry delay: Based on `retryAfterSeconds`
  - User message: "Too many requests. Please wait a moment and try again."

### DataError

**Recoverable**: Varies by type  
**Auto-retry**: No

- **ConstraintViolation**: Database constraint violation
  - Recoverable: Yes
  - User message: "This data already exists. Please check and try again."

- **NotFound**: Data not found
  - Recoverable: No
  - User message: "The requested item could not be found."

- **InvalidData**: Invalid data format
  - Recoverable: Yes
  - User message: "Invalid {field}. Please check your input."

- **Corruption**: Database corruption
  - Recoverable: No
  - User message: "Data integrity issue detected. The app may need to be reset."

### SystemError

**Recoverable**: No  
**Auto-retry**: No

- **OutOfMemory**: Out of memory
  - User message: "The app is running low on memory. Please close other apps and try again."

- **ConfigurationError**: App misconfigured
  - User message: "App configuration error. Please contact support."

- **Unknown**: Unknown system error
  - User message: "An unexpected error occurred. Please restart the app."

## Layer-Specific Patterns

### Network Layer (SupabaseDataSource)

All network operations wrap exceptions in `NetworkError`:

```kotlin
suspend fun insertMood(mood: Mood): Mood {
    return try {
        // Network operation
        supabaseClient.from(tableName).insert(mood)
    } catch (e: Exception) {
        handleException("insertMood", e) // Converts to NetworkError
    }
}
```

### Repository Layer (MoodRepository)

Handles both `NetworkError` and `DataError`:

```kotlin
suspend fun saveMood(mood: Mood): Result<Mood> {
    return try {
        // Save locally first (offline-first)
        moodDao.insertMood(mood)
        
        // Try remote sync (non-blocking)
        try {
            remoteDataSource.insertMood(mood)
        } catch (e: NetworkError) {
            e.log("MoodRepository", "Will retry later")
            // Mood saved locally, sync later
        }
        
        Result.success(mood)
    } catch (e: IllegalArgumentException) {
        val dataError = DataError.InvalidData(field = "mood", errorCause = e)
        dataError.log("MoodRepository", "Invalid mood data")
        Result.failure(dataError)
    }
}
```

### Background Worker (MoodSyncWorker)

Handles errors with appropriate retry logic:

```kotlin
override suspend fun doWork(): Result {
    return try {
        val syncResult = moodRepository.syncWithRemote()
        syncResult.fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                when (error) {
                    is NetworkError -> {
                        if (error.isRecoverable && error.shouldRetry) {
                            Result.retry() // WorkManager handles backoff
                        } else {
                            Result.failure()
                        }
                    }
                    is SystemError -> Result.failure() // Don't retry
                    else -> Result.retry() // Unknown - retry once
                }
            }
        )
    } catch (e: NetworkError) {
        // Handle network errors
    }
}
```

### UI Layer (ViewModel)

Shows user-friendly messages based on error type:

```kotlin
moodRepository.saveMood(mood)
    .onFailure { error ->
        val userMessage = when (error) {
            is NetworkError -> {
                if (error.isRecoverable) {
                    "Mood saved locally. Will sync when connection is available."
                } else {
                    error.userMessage
                }
            }
            is DataError -> error.userMessage
            is SystemError -> error.userMessage
            else -> "Failed to save mood: ${error.message ?: "Unknown error"}"
        }
        _errorMessage.value = userMessage
        error.log("MoodManagementViewModel", "Failed to save mood")
    }
```

## Best Practices

### 1. Always Log Errors

```kotlin
// Good
error.log("ComponentName", "Operation context")

// Bad
// No logging
```

### 2. Use Appropriate Error Types

```kotlin
// Good - Network error for network issues
catch (e: Exception) {
    val networkError = NetworkError.fromException(e)
    throw networkError
}

// Bad - Generic exception
catch (e: Exception) {
    throw e // Loses error classification
}
```

### 3. Handle Errors at Appropriate Layers

- **Network layer**: Convert to `NetworkError`
- **Repository layer**: Handle `NetworkError` gracefully (offline-first)
- **UI layer**: Show user-friendly messages

### 4. Preserve Error Context

```kotlin
// Good - Preserve original exception
val error = NetworkError.NoConnection(originalException)

// Bad - Lose original exception
val error = NetworkError.NoConnection() // No cause
```

### 5. Use Retry Logic Appropriately

```kotlin
// Good - Check retry flags
if (error.isRecoverable && error.shouldRetry) {
    retryAfter(error.retryDelayMillis)
}

// Bad - Always retry
retry() // May retry non-retryable errors
```

## Testing

See test files:
- `NetworkErrorTest.kt`: Tests error parsing and properties
- `DataErrorTest.kt`: Tests data error types
- `SystemErrorTest.kt`: Tests system error types
- `AppErrorTest.kt`: Tests base error functionality

## Migration Guide

### Converting Existing Code

1. **Replace generic exceptions**:
   ```kotlin
   // Before
   catch (e: Exception) {
       Logger.error("Tag", "Error", e)
       throw e
   }
   
   // After
   catch (e: Exception) {
       val networkError = NetworkError.fromException(e)
       networkError.log("Tag", "Context")
       throw networkError
   }
   ```

2. **Update Result.failure()**:
   ```kotlin
   // Before
   Result.failure(Exception("Error message"))
   
   // After
   Result.failure(DataError.InvalidData(field = "mood"))
   ```

3. **Update error handling**:
   ```kotlin
   // Before
   .onFailure { error ->
       showError(error.message ?: "Unknown error")
   }
   
   // After
   .onFailure { error ->
       val userMessage = when (error) {
           is AppError -> error.userMessage
           else -> error.message ?: "Unknown error"
       }
       showError(userMessage)
       error.log("Component", "Context")
   }
   ```

## Future Enhancements

1. **Error Metrics**: Track error frequencies and types
2. **Error Reporting**: Integrate with crash reporting services
3. **Retry Strategies**: More sophisticated retry logic (exponential backoff, jitter)
4. **Error Recovery**: Automatic recovery strategies for specific error types
5. **AuthError Integration**: Consider making `AuthError` extend `AppError`

