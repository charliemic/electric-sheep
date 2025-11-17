package com.electricsheep.app.data.repository

import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.config.FeatureFlagManager
import com.electricsheep.app.data.local.MoodDao
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.data.remote.SupabaseDataSource
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for MoodRepository offline-only mode behaviour.
 * Tests that remote operations are skipped when offline-only mode is enabled.
 */
class MoodRepositoryOfflineOnlyTest {
    
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
        // Enable offline-only mode for these tests
        whenever(featureFlagManager.isOfflineOnly()).thenReturn(true)
        // Default to authenticated user
        whenever(userManager.requireUserId()).thenReturn("user-1")
        repository = MoodRepository(moodDao, remoteDataSource, featureFlagManager, userManager)
    }
    
    @Test
    fun `should skip remote sync when saving mood in offline-only mode`() = runTest {
        // Arrange
        val userId = "user-1"
        val mood = Mood(id = "1", userId = userId, score = 5, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.insertMood(any())).thenReturn(1L)
        
        // Act
        val result = repository.saveMood(mood)
        
        // Assert
        assertTrue(result.isSuccess)
        verify(moodDao).insertMood(any())
        verify(remoteDataSource, never()).insertMood(any())
    }
    
    @Test
    fun `should skip remote sync when updating mood in offline-only mode`() = runTest {
        // Arrange
        val userId = "user-1"
        val mood = Mood(id = "test-id", userId = userId, score = 8, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.updateMood(any())).thenAnswer { }
        
        // Act
        val result = repository.updateMood(mood)
        
        // Assert
        assertTrue(result.isSuccess)
        verify(moodDao).updateMood(any())
        verify(remoteDataSource, never()).updateMood(any())
    }
    
    @Test
    fun `should skip remote sync when deleting mood in offline-only mode`() = runTest {
        // Arrange
        val userId = "user-1"
        val moodId = "test-id"
        val mood = Mood(id = moodId, userId = userId, score = 5, timestamp = System.currentTimeMillis())
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getMoodById(moodId, userId)).thenReturn(mood)
        whenever(moodDao.deleteMoodById(any())).thenAnswer { }
        
        // Act
        val result = repository.deleteMood(moodId)
        
        // Assert
        assertTrue(result.isSuccess)
        verify(moodDao).deleteMoodById(moodId)
        verify(remoteDataSource, never()).deleteMood(any())
    }
    
    @Test
    fun `should return empty sync result when syncing in offline-only mode`() = runTest {
        // Arrange
        val userId = "user-1"
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(emptyList()))
        
        // Act
        val result = repository.syncWithRemote()
        
        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(0, syncResult.pushedCount)
        assertEquals(0, syncResult.pulledCount)
        assertEquals(0, syncResult.mergedCount)
        assertEquals(0, syncResult.conflictsResolved)
        
        // Verify no remote operations were attempted
        verify(remoteDataSource, never()).getAllMoods(any())
        verify(remoteDataSource, never()).upsertMoods(any())
    }
}

