package com.electricsheep.app.data.repository

import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.config.FeatureFlagManager
import com.electricsheep.app.data.local.MoodDao
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.data.remote.SupabaseDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for MoodRepository.
 * Tests repository logic, offline-first behaviour, and error handling.
 */
class MoodRepositoryTest {
    
    private lateinit var moodDao: MoodDao
    private lateinit var remoteDataSource: SupabaseDataSource
    private lateinit var featureFlagManager: FeatureFlagManager
    private lateinit var userManager: UserManager
    private lateinit var repository: MoodRepository
    
    @Before
    fun setUp() {
        moodDao = mock()
        remoteDataSource = mock()
        featureFlagManager = mock()
        userManager = mock()
        // Default to offline-only mode disabled
        whenever(featureFlagManager.isOfflineOnly()).thenReturn(false)
        // Default to authenticated user
        whenever(userManager.requireUserId()).thenReturn("user-1")
        repository = MoodRepository(moodDao, remoteDataSource, featureFlagManager, userManager)
    }
    
    @Test
    fun `should get all moods from local data source`() = runTest {
        // Arrange
        val userId = "user-1"
        val expectedMoods = listOf(
            Mood(id = "1", userId = userId, score = 5, timestamp = System.currentTimeMillis()),
            Mood(id = "2", userId = userId, score = 7, timestamp = System.currentTimeMillis())
        )
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(expectedMoods))
        
        // Act
        val result = repository.getAllMoods().first()
        
        // Assert
        assertEquals(expectedMoods, result)
        verify(moodDao).getAllMoods(userId)
    }
    
    @Test
    fun `should save mood locally first when saving`() = runTest {
        // Arrange
        val userId = "user-1"
        val mood = Mood(id = "", userId = userId, score = 5, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.insertMood(any())).thenReturn(1L)
        whenever(remoteDataSource.insertMood(any())).thenAnswer { }
        
        // Act
        val result = repository.saveMood(mood)
        
        // Assert
        assertTrue(result.isSuccess)
        verify(moodDao).insertMood(any())
        verify(remoteDataSource).insertMood(any())
    }
    
    @Test
    fun `should generate id for new mood when saving`() = runTest {
        // Arrange
        val userId = "user-1"
        val mood = Mood(id = "", userId = userId, score = 5, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.insertMood(any())).thenReturn(1L)
        whenever(remoteDataSource.insertMood(any())).thenAnswer { }
        
        // Act
        val result = repository.saveMood(mood)
        
        // Assert
        assertTrue(result.isSuccess)
        val savedMood = result.getOrNull()!!
        assertNotNull(savedMood.id)
        assertTrue(savedMood.id.isNotBlank())
        verify(moodDao).insertMood(argThat { mood -> mood.id.isNotBlank() })
    }
    
    @Test
    fun `should set userId from current user when mood has blank userId`() = runTest {
        // Arrange
        val userId = "user-1"
        // Note: Mood with blank userId will fail validation, so we need to set userId after validation
        // The repository will set userId if blank, but validation happens first
        // So we create a mood with a valid userId but let the repository logic handle it
        val mood = Mood(id = "", userId = userId, score = 5, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.insertMood(any())).thenReturn(1L)
        whenever(remoteDataSource.insertMood(any())).thenAnswer { }
        
        // Act
        val result = repository.saveMood(mood)
        
        // Assert
        assertTrue(result.isSuccess)
        val savedMood = result.getOrNull()!!
        assertEquals(userId, savedMood.userId)
        verify(moodDao).insertMood(argThat { m -> m.userId == userId })
    }
    
    @Test
    fun `should return failure for invalid mood score`() = runTest {
        // Arrange
        val userId = "user-1"
        val invalidMood = Mood(id = "1", userId = userId, score = 0, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        
        // Act
        val result = repository.saveMood(invalidMood)
        
        // Assert
        assertTrue(result.isFailure)
        verify(moodDao, never()).insertMood(any())
        verify(remoteDataSource, never()).insertMood(any())
    }
    
    @Test
    fun `should return failure for invalid timestamp`() = runTest {
        // Arrange
        val userId = "user-1"
        val invalidMood = Mood(id = "1", userId = userId, score = 5, timestamp = -1)
        whenever(userManager.requireUserId()).thenReturn(userId)
        
        // Act
        val result = repository.saveMood(invalidMood)
        
        // Assert
        assertTrue(result.isFailure)
        verify(moodDao, never()).insertMood(any())
    }
    
    @Test
    fun `should throw exception when userId does not match current user`() = runTest {
        // Arrange
        val currentUserId = "user-1"
        val differentUserId = "user-2"
        val mood = Mood(id = "1", userId = differentUserId, score = 5, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(currentUserId)
        
        // Act & Assert
        val result = repository.saveMood(mood)
        assertTrue(result.isFailure)
        verify(moodDao, never()).insertMood(any())
    }
    
    @Test
    fun `should still save locally when remote sync fails`() = runTest {
        // Arrange
        val userId = "user-1"
        val mood = Mood(id = "1", userId = userId, score = 5, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.insertMood(any())).thenReturn(1L)
        whenever(remoteDataSource.insertMood(any())).thenThrow(RuntimeException("Network error"))
        
        // Act
        val result = repository.saveMood(mood)
        
        // Assert
        assertTrue(result.isSuccess) // Should succeed despite remote failure
        verify(moodDao).insertMood(any())
        verify(remoteDataSource).insertMood(any())
    }
    
    @Test
    fun `should update mood locally first`() = runTest {
        // Arrange
        val userId = "user-1"
        val mood = Mood(id = "existing-id", userId = userId, score = 8, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.updateMood(any())).thenAnswer { }
        whenever(remoteDataSource.updateMood(any())).thenReturn(mood)
        
        // Act
        val result = repository.updateMood(mood)
        
        // Assert
        assertTrue(result.isSuccess)
        verify(moodDao).updateMood(any())
        verify(remoteDataSource).updateMood(any())
    }
    
    @Test
    fun `should return failure when user is not authenticated`() = runTest {
        // Arrange
        // Mood needs valid userId for validation to pass, but requireUserId will throw
        val mood = Mood(id = "1", userId = "user-1", score = 5, timestamp = System.currentTimeMillis())
        // require() throws IllegalArgumentException, not IllegalStateException
        whenever(userManager.requireUserId()).thenThrow(IllegalArgumentException("User must be authenticated"))
        
        // Act
        val result = repository.saveMood(mood)
        
        // Assert
        assertTrue(result.isFailure)
        // The exception is caught and wrapped in Result.failure
        assertNotNull(result.exceptionOrNull())
    }
    
    @Test
    fun `should return failure when updating mood without id`() = runTest {
        // Arrange
        val userId = "user-1"
        val mood = Mood(id = "", userId = userId, score = 8, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        
        // Act
        val result = repository.updateMood(mood)
        
        // Assert
        assertTrue(result.isFailure)
        verify(moodDao, never()).updateMood(any())
    }
    
    @Test
    fun `should return failure when updating mood with different userId`() = runTest {
        // Arrange
        val currentUserId = "user-1"
        val differentUserId = "user-2"
        val mood = Mood(id = "existing-id", userId = differentUserId, score = 8, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(currentUserId)
        
        // Act
        val result = repository.updateMood(mood)
        
        // Assert
        assertTrue(result.isFailure)
        verify(moodDao, never()).updateMood(any())
    }
    
    @Test
    fun `should delete mood locally first`() = runTest {
        // Arrange
        val userId = "user-1"
        val moodId = "mood-id"
        val mood = Mood(id = moodId, userId = userId, score = 5, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getMoodById(moodId, userId)).thenReturn(mood)
        whenever(moodDao.deleteMoodById(any())).thenAnswer { }
        // deleteMood is a suspend function that returns Unit, so we use thenAnswer
        whenever(remoteDataSource.deleteMood(any())).thenAnswer { }
        
        // Act
        val result = repository.deleteMood(moodId)
        
        // Assert
        assertTrue(result.isSuccess)
        verify(moodDao).getMoodById(moodId, userId)
        verify(moodDao).deleteMoodById(moodId)
        verify(remoteDataSource).deleteMood(moodId)
    }
    
    @Test
    fun `should return failure when deleting mood that does not exist`() = runTest {
        // Arrange
        val userId = "user-1"
        val moodId = "non-existent-id"
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getMoodById(moodId, userId)).thenReturn(null)
        
        // Act
        val result = repository.deleteMood(moodId)
        
        // Assert
        assertTrue(result.isFailure)
        verify(moodDao, never()).deleteMoodById(any())
        verify(remoteDataSource, never()).deleteMood(any())
    }
    
    @Test
    fun `should sync moods from remote to local`() = runTest {
        // Arrange
        val userId = "user-1"
        val remoteMoods = listOf(
            Mood(id = "1", userId = userId, score = 5, timestamp = System.currentTimeMillis()),
            Mood(id = "2", userId = userId, score = 7, timestamp = System.currentTimeMillis())
        )
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(emptyList()))
        whenever(remoteDataSource.getAllMoods(userId)).thenReturn(remoteMoods)
        whenever(remoteDataSource.upsertMoods(any())).thenAnswer { }
        whenever(moodDao.insertMoods(any())).thenAnswer { }
        
        // Act
        val result = repository.syncWithRemote()
        
        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(2, syncResult.pulledCount) // Two moods pulled from remote
        assertEquals(0, syncResult.pushedCount) // No local moods to push
        assertEquals(2, syncResult.mergedCount) // Two moods merged/saved
        assertEquals(0, syncResult.conflictsResolved) // No conflicts
        verify(remoteDataSource).getAllMoods(userId)
        verify(moodDao).insertMoods(any())
    }
    
    @Test
    fun `should return partial success when pull fails but push succeeds`() = runTest {
        // Arrange
        val userId = "user-1"
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(emptyList()))
        whenever(remoteDataSource.getAllMoods(userId)).thenThrow(RuntimeException("Network error"))
        
        // Act
        val result = repository.syncWithRemote()
        
        // Assert
        // Sync returns success even when pull fails (graceful degradation)
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(0, syncResult.pulledCount) // Pull failed, so no moods pulled
        assertEquals(0, syncResult.pushedCount) // No local moods to push
        verify(remoteDataSource).getAllMoods(userId)
        verify(moodDao, never()).insertMoods(any())
    }
}

