# Error Conversion Strategies

## Overview

This document explains when and how to convert between different error types in the Electric Sheep app.

## Error Type Hierarchy

```
AppError (base)
├── NetworkError (network operations)
├── DataError (database/storage operations)
└── SystemError (system-level issues)

AuthError (separate, for authentication)
```

## Conversion Patterns

### 1. NetworkError → AuthError

**When**: Authentication operations that fail due to network issues

**Pattern**:
```kotlin
catch (e: NetworkError) {
    e.log("ComponentName", "Context")
    val authError = AuthError.NetworkError(e)
    Result.failure(authError)
}
```

**Rationale**: 
- Maintains backward compatibility with existing `AuthError` structure
- Provides consistent error handling in auth layer
- Preserves original `NetworkError` in `cause` property

**Example**: `SupabaseAuthProvider.signIn()`

### 2. Exception → NetworkError

**When**: Network operations throw generic exceptions

**Pattern**:
```kotlin
catch (e: Exception) {
    val networkError = NetworkError.fromException(e)
    networkError.log("ComponentName", "Context")
    throw networkError
}
```

**Rationale**:
- Centralized parsing logic in `NetworkError.fromException()`
- Consistent error classification
- Priority-based parsing (exception types → HTTP codes → message patterns)

**Example**: `SupabaseDataSource` operations

### 3. Exception → DataError

**When**: Database operations throw generic exceptions

**Pattern**:
```kotlin
// Using RoomErrorHandler
val result = RoomErrorHandler.wrapRoomOperation(
    operation = { moodDao.insertMood(mood) },
    context = "insertMood"
)
result.getOrThrow() // Throws DataError if operation fails
```

**Rationale**:
- Room-specific exception handling
- Converts SQLite exceptions to appropriate `DataError` types
- Provides context for debugging

**Example**: `MoodRepository.saveMood()`

### 4. Exception → SystemError

**When**: System-level failures (initialization, configuration)

**Pattern**:
```kotlin
catch (e: Exception) {
    val systemError = SystemError.ConfigurationError(
        config = "ComponentName",
        errorCause = e
    )
    systemError.log("ComponentName", "Context")
    // Handle gracefully
}
```

**Rationale**:
- System errors are typically non-recoverable
- Provides context about what failed
- Logs appropriately (ERROR level)

**Example**: `ElectricSheepApplication.onCreate()`

### 5. IllegalArgumentException → DataError

**When**: Validation errors in data operations

**Pattern**:
```kotlin
catch (e: IllegalArgumentException) {
    val dataError = DataError.InvalidData(
        field = "fieldName",
        errorCause = e
    )
    dataError.log("ComponentName", "Context")
    Result.failure(dataError)
}
```

**Rationale**:
- Validation errors are user-recoverable
- Provides field-specific error messages
- Consistent with data error handling

**Example**: `MoodRepository.saveMood()`

## When NOT to Convert

### 1. Preserve Original Error Types

**Don't convert** if the error type is already appropriate:
```kotlin
// Good - NetworkError is already appropriate
catch (e: NetworkError) {
    e.log("ComponentName", "Context")
    Result.failure(e)
}

// Bad - Unnecessary conversion
catch (e: NetworkError) {
    val networkError = NetworkError.fromException(e) // Redundant
    Result.failure(networkError)
}
```

### 2. Don't Convert for Logging Only

**Don't convert** just to log - use the error's `.log()` method:
```kotlin
// Good - Use built-in logging
catch (e: NetworkError) {
    e.log("ComponentName", "Context")
    Result.failure(e)
}

// Bad - Convert just for logging
catch (e: NetworkError) {
    Logger.error("ComponentName", "Error", e) // Loses structured logging
    Result.failure(e)
}
```

## Best Practices

### 1. Convert at Boundaries

Convert errors at layer boundaries:
- **Network layer**: Exception → NetworkError
- **Data layer**: Exception → DataError
- **System layer**: Exception → SystemError
- **Auth layer**: NetworkError → AuthError (if needed)

### 2. Preserve Original Exception

Always preserve the original exception in the `cause` property:
```kotlin
// Good - Preserves original exception
val networkError = NetworkError.NoConnection(originalException)

// Bad - Loses original exception
val networkError = NetworkError.NoConnection() // No cause
```

### 3. Log Before Converting

Log the error before converting (if needed):
```kotlin
catch (e: NetworkError) {
    e.log("ComponentName", "Context") // Log original
    val authError = AuthError.NetworkError(e) // Convert
    Result.failure(authError)
}
```

### 4. Use Factory Methods

Use factory methods (`fromException()`) when available:
```kotlin
// Good - Uses factory method
val networkError = NetworkError.fromException(exception)

// Bad - Manual conversion
val networkError = when {
    exception is UnknownHostException -> NetworkError.NoConnection(exception)
    // ... many more cases
}
```

## Future Considerations

### AuthError Integration

**Current**: `AuthError` is separate from `AppError`

**Consideration**: Make `AuthError` extend `AppError`

**Pros**:
- Unified error hierarchy
- Consistent error handling
- No conversion needed

**Cons**:
- Breaking change for existing code
- `AuthError` has different structure (message-based)

**Recommendation**: Document current approach, consider migration in future major version

## Examples

### Example 1: Network Operation

```kotlin
suspend fun fetchData(): Result<Data> {
    return try {
        val data = apiClient.getData()
        Result.success(data)
    } catch (e: Exception) {
        // Convert to NetworkError
        val networkError = NetworkError.fromException(e)
        networkError.log("ApiClient", "fetchData")
        Result.failure(networkError)
    }
}
```

### Example 2: Database Operation

```kotlin
suspend fun saveMood(mood: Mood): Result<Mood> {
    return try {
        // Wrap Room operation
        val result = RoomErrorHandler.wrapRoomOperation(
            operation = { moodDao.insertMood(mood) },
            context = "saveMood"
        )
        result.getOrThrow() // Throws DataError if fails
        Result.success(mood)
    } catch (e: DataError) {
        // Already a DataError - just log
        e.log("MoodRepository", "saveMood")
        Result.failure(e)
    }
}
```

### Example 3: Authentication with Network Error

```kotlin
suspend fun signIn(email: String, password: String): Result<User> {
    return try {
        supabaseClient.auth.signInWith(Email) { ... }
        Result.success(getCurrentUser()!!)
    } catch (e: NetworkError) {
        // Convert NetworkError to AuthError for consistency
        e.log("SupabaseAuthProvider", "signIn")
        val authError = AuthError.NetworkError(e)
        Result.failure(authError)
    } catch (e: Exception) {
        // Parse generic exception to AuthError
        val authError = AuthError.fromException(e)
        Result.failure(authError)
    }
}
```

## Summary

- **Convert at layer boundaries**: Network → NetworkError, Database → DataError, System → SystemError
- **Preserve original exceptions**: Always include in `cause` property
- **Use factory methods**: `fromException()` when available
- **Log before converting**: Use `.log()` method for structured logging
- **Don't convert unnecessarily**: If error type is already appropriate, use it directly

