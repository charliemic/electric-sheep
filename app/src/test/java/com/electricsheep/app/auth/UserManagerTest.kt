package com.electricsheep.app.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for UserManager.
 * Tests user state management and authentication operations.
 */
class UserManagerTest {
    
    private lateinit var authProvider: AuthProvider
    private lateinit var userManager: UserManager
    
    @Before
    fun setUp() {
        authProvider = mock()
        userManager = UserManager(authProvider)
    }
    
    @Test
    fun `should initialise with no user when auth provider has no user`() = runTest {
        // Arrange
        whenever(authProvider.getCurrentUser()).thenReturn(null)
        
        // Act
        userManager.initialise()
        
        // Assert
        assertFalse(userManager.isAuthenticated)
        assertNull(userManager.currentUser.first())
        verify(authProvider).getCurrentUser()
    }
    
    @Test
    fun `should initialise with user when auth provider has user`() = runTest {
        // Arrange
        val user = User(id = "user-1", email = "test@example.com")
        whenever(authProvider.getCurrentUser()).thenReturn(user)
        
        // Act
        userManager.initialise()
        
        // Assert
        assertTrue(userManager.isAuthenticated)
        assertEquals(user, userManager.currentUser.first())
        verify(authProvider).getCurrentUser()
    }
    
    @Test
    fun `should sign in user and update state`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password"
        val user = User(id = "user-1", email = email)
        whenever(authProvider.signIn(email, password)).thenReturn(Result.success(user))
        
        // Act
        val result = userManager.signIn(email, password)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        assertEquals(user, userManager.currentUser.first())
        assertTrue(userManager.isAuthenticated)
        verify(authProvider).signIn(email, password)
    }
    
    @Test
    fun `should handle sign in failure gracefully`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrong"
        val error = Exception("Invalid credentials")
        whenever(authProvider.signIn(email, password)).thenReturn(Result.failure(error))
        
        // Act
        val result = userManager.signIn(email, password)
        
        // Assert
        assertTrue(result.isFailure)
        assertFalse(userManager.isAuthenticated)
        assertNull(userManager.currentUser.first())
    }
    
    @Test
    fun `should sign up user and update state`() = runTest {
        // Arrange
        val email = "new@example.com"
        val password = "password"
        val displayName = "New User"
        val user = User(id = "user-1", email = email, displayName = displayName)
        whenever(authProvider.signUp(email, password, displayName)).thenReturn(Result.success(user))
        
        // Act
        val result = userManager.signUp(email, password, displayName)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        assertEquals(user, userManager.currentUser.first())
        verify(authProvider).signUp(email, password, displayName)
    }
    
    @Test
    fun `should sign out user and clear state`() = runTest {
        // Arrange
        val user = User(id = "user-1", email = "test@example.com")
        whenever(authProvider.getCurrentUser()).thenReturn(user)
        userManager.initialise()
        whenever(authProvider.signOut()).thenReturn(Result.success(Unit))
        
        // Act
        val result = userManager.signOut()
        
        // Assert
        assertTrue(result.isSuccess)
        assertFalse(userManager.isAuthenticated)
        assertNull(userManager.currentUser.first())
        verify(authProvider).signOut()
    }
    
    @Test
    fun `should throw exception when requiring userId without authentication`() {
        // Act & Assert
        // require() throws IllegalArgumentException, not IllegalStateException
        assertThrows(IllegalArgumentException::class.java) {
            userManager.requireUserId()
        }
    }
    
    @Test
    fun `should return userId when authenticated`() = runTest {
        // Arrange
        val user = User(id = "user-1", email = "test@example.com")
        whenever(authProvider.getCurrentUser()).thenReturn(user)
        userManager.initialise()
        
        // Act
        val userId = userManager.requireUserId()
        
        // Assert
        assertEquals("user-1", userId)
    }
    
    @Test
    fun `should return null userId when not authenticated`() {
        // Act
        val userId = userManager.getUserIdOrNull()
        
        // Assert
        assertNull(userId)
    }
    
    @Test
    fun `should return userId when authenticated using getUserIdOrNull`() = runTest {
        // Arrange
        val user = User(id = "user-1", email = "test@example.com")
        whenever(authProvider.getCurrentUser()).thenReturn(user)
        userManager.initialise()
        
        // Act
        val userId = userManager.getUserIdOrNull()
        
        // Assert
        assertEquals("user-1", userId)
    }
}

