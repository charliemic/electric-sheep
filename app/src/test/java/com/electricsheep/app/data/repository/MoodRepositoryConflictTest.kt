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
 * Unit tests for MoodRepository conflict resolution.
 * Tests merge logic when local and remote moods conflict.
 */
class MoodRepositoryConflictTest {
    
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
    fun `should resolve conflict with latest edit winning`() = runTest {
        // Arrange
        val moodId = "mood-1"
        val baseTime = System.currentTimeMillis()
        
        val userId = "user-1"
        // Local mood was edited more recently
        val localMood = Mood(
            id = moodId,
            userId = userId,
            score = 8,
            timestamp = baseTime,
            updatedAt = baseTime + 2000L // Edited 2 seconds later
        )
        
        // Remote mood is older
        val remoteMood = Mood(
            id = moodId,
            userId = userId,
            score = 5,
            timestamp = baseTime,
            updatedAt = baseTime + 1000L // Edited 1 second later
        )
        
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(listOf(localMood)))
        whenever(remoteDataSource.getAllMoods(userId)).thenReturn(listOf(remoteMood))
        whenever(remoteDataSource.upsertMoods(any())).thenAnswer { }
        whenever(moodDao.insertMoods(any())).thenAnswer { }
        
        // Act
        val result = repository.syncWithRemote()
        
        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(1, syncResult.conflictsResolved)
        
        // Verify local (newer) mood was saved
        val savedMoods = argumentCaptor<List<Mood>>()
        verify(moodDao).insertMoods(savedMoods.capture())
        val savedMood = savedMoods.firstValue.first()
        assertEquals(moodId, savedMood.id)
        assertEquals(8, savedMood.score) // Local (newer) score should win
    }
    
    @Test
    fun `should resolve conflict with remote winning when remote is newer`() = runTest {
        // Arrange
        val moodId = "mood-1"
        val baseTime = System.currentTimeMillis()
        
        val userId = "user-1"
        // Remote mood was edited more recently
        val remoteMood = Mood(
            id = moodId,
            userId = userId,
            score = 9,
            timestamp = baseTime,
            updatedAt = baseTime + 2000L
        )
        
        // Local mood is older
        val localMood = Mood(
            id = moodId,
            userId = userId,
            score = 6,
            timestamp = baseTime,
            updatedAt = baseTime + 1000L
        )
        
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(listOf(localMood)))
        whenever(remoteDataSource.getAllMoods(userId)).thenReturn(listOf(remoteMood))
        whenever(remoteDataSource.upsertMoods(any())).thenAnswer { }
        whenever(moodDao.insertMoods(any())).thenAnswer { }
        
        // Act
        val result = repository.syncWithRemote()
        
        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(1, syncResult.conflictsResolved)
        
        // Verify remote (newer) mood was saved
        val savedMoods = argumentCaptor<List<Mood>>()
        verify(moodDao).insertMoods(savedMoods.capture())
        val savedMood = savedMoods.firstValue.first()
        assertEquals(moodId, savedMood.id)
        assertEquals(9, savedMood.score) // Remote (newer) score should win
    }
    
    @Test
    fun `should not count as conflict when moods are identical`() = runTest {
        // Arrange
        val userId = "user-1"
        val moodId = "mood-1"
        val baseTime = System.currentTimeMillis()
        
        val localMood = Mood(
            id = moodId,
            userId = userId,
            score = 5,
            timestamp = baseTime,
            updatedAt = baseTime
        )
        
        val remoteMood = Mood(
            id = moodId,
            userId = userId,
            score = 5,
            timestamp = baseTime,
            updatedAt = baseTime
        )
        
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(listOf(localMood)))
        whenever(remoteDataSource.getAllMoods(userId)).thenReturn(listOf(remoteMood))
        whenever(remoteDataSource.upsertMoods(any())).thenAnswer { }
        whenever(moodDao.insertMoods(any())).thenAnswer { }
        
        // Act
        val result = repository.syncWithRemote()
        
        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(0, syncResult.conflictsResolved) // No conflict if identical
    }
    
    @Test
    fun `should merge new remote moods not in local`() = runTest {
        // Arrange
        val userId = "user-1"
        val newRemoteMood = Mood(
            id = "new-remote",
            userId = userId,
            score = 7,
            timestamp = System.currentTimeMillis()
        )
        
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(emptyList()))
        whenever(remoteDataSource.getAllMoods(userId)).thenReturn(listOf(newRemoteMood))
        whenever(remoteDataSource.upsertMoods(any())).thenAnswer { }
        whenever(moodDao.insertMoods(any())).thenAnswer { }
        
        // Act
        val result = repository.syncWithRemote()
        
        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrNull()!!
        assertEquals(1, syncResult.pulledCount)
        assertEquals(0, syncResult.conflictsResolved)
        
        // Verify new remote mood was saved locally
        val savedMoods = argumentCaptor<List<Mood>>()
        verify(moodDao).insertMoods(savedMoods.capture())
        assertEquals(1, savedMoods.firstValue.size)
        assertEquals("new-remote", savedMoods.firstValue.first().id)
    }
    
    @Test
    fun `should keep local-only moods`() = runTest {
        // Arrange
        val userId = "user-1"
        val localOnlyMood = Mood(
            id = "local-only",
            userId = userId,
            score = 6,
            timestamp = System.currentTimeMillis()
        )
        
        whenever(userManager.requireUserId()).thenReturn(userId)
        whenever(moodDao.getAllMoods(userId)).thenReturn(flowOf(listOf(localOnlyMood)))
        whenever(remoteDataSource.getAllMoods(userId)).thenReturn(emptyList())
        whenever(remoteDataSource.upsertMoods(any())).thenAnswer { }
        whenever(moodDao.insertMoods(any())).thenAnswer { }
        
        // Act
        val result = repository.syncWithRemote()
        
        // Assert
        assertTrue(result.isSuccess)
        
        // Verify local-only mood was kept
        val savedMoods = argumentCaptor<List<Mood>>()
        verify(moodDao).insertMoods(savedMoods.capture())
        assertEquals(1, savedMoods.firstValue.size)
        assertEquals("local-only", savedMoods.firstValue.first().id)
    }
}

