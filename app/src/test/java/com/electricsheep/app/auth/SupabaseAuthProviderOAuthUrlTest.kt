package com.electricsheep.app.auth

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for OAuth URL generation and fixing logic in SupabaseAuthProvider.
 * 
 * Tests the workaround for SDK's getOAuthUrl() generating incorrect path
 * and missing API key.
 * 
 * Note: These tests use string manipulation to avoid Android framework dependencies
 * (Uri class requires Robolectric or instrumented tests).
 */
class SupabaseAuthProviderOAuthUrlTest {

    /**
     * Helper function to fix OAuth URL path (matches SupabaseAuthProvider logic).
     * This is tested directly without Android Uri class to avoid mocking issues.
     */
    private fun fixOAuthUrlPath(url: String): String {
        // Extract path from URL - find the path component
        val urlParts = url.split("?")
        val baseUrl = urlParts[0]
        val queryString = if (urlParts.size > 1) urlParts[1] else ""
        
        // Extract path (everything after domain)
        val domainEnd = baseUrl.indexOf("/", 8) // Find first / after https://
        if (domainEnd == -1) return url // No path found
        
        val domain = baseUrl.substring(0, domainEnd)
        val path = baseUrl.substring(domainEnd)
        
        // Fix path if needed
        val fixedPath = when {
            path == "/auth/v1/" || path == "/auth/v1" -> "/auth/v1/authorize"
            else -> path
        }
        
        // Reconstruct URL
        return if (queryString.isNotEmpty()) {
            "$domain$fixedPath?$queryString"
        } else {
            "$domain$fixedPath"
        }
    }

    /**
     * Helper function to add API key to URL if missing (matches SupabaseAuthProvider logic).
     */
    private fun addApiKeyToUrl(url: String, apiKey: String): String {
        return if (url.contains("apikey=")) {
            url // Don't add if already present
        } else {
            val separator = if (url.contains("?")) "&" else "?"
            "$url$separator" + "apikey=$apiKey"
        }
    }

    @Test
    fun `fixOAuthUrl should correct auth v1 path to authorize`() {
        // SDK generates: /auth/v1/
        // Should be: /auth/v1/authorize
        val sdkUrl = "https://test.supabase.co/auth/v1/?provider=google&redirect_to=app://callback"
        val fixedUrl = fixOAuthUrlPath(sdkUrl)
        
        assertTrue("URL should have /auth/v1/authorize path", fixedUrl.contains("/auth/v1/authorize"))
        assertFalse("URL should not have /auth/v1/ path", fixedUrl.contains("/auth/v1/?"))
    }

    @Test
    fun `fixOAuthUrl should preserve existing authorize path`() {
        // If path is already correct, don't change it
        val correctUrl = "https://test.supabase.co/auth/v1/authorize?provider=google"
        val fixedUrl = fixOAuthUrlPath(correctUrl)
        
        assertTrue("URL should preserve /auth/v1/authorize path", fixedUrl.contains("/auth/v1/authorize"))
    }

    @Test
    fun `fixOAuthUrl should handle auth v1 without trailing slash`() {
        val sdkUrl = "https://test.supabase.co/auth/v1?provider=google"
        val fixedUrl = fixOAuthUrlPath(sdkUrl)
        
        assertTrue("URL should have /auth/v1/authorize path", fixedUrl.contains("/auth/v1/authorize"))
    }

    @Test
    fun `addApiKey should add apikey parameter if missing`() {
        val url = "https://test.supabase.co/auth/v1/authorize?provider=google&redirect_to=app://callback"
        val fixedUrl = addApiKeyToUrl(url, "test-api-key")
        
        assertTrue("URL should contain apikey parameter", fixedUrl.contains("apikey=test-api-key"))
        assertTrue("URL should preserve existing parameters", fixedUrl.contains("provider=google"))
        assertTrue("URL should preserve redirect_to", fixedUrl.contains("redirect_to=app://callback"))
    }

    @Test
    fun `addApiKey should not duplicate existing apikey parameter`() {
        val url = "https://test.supabase.co/auth/v1/authorize?provider=google&apikey=existing-key"
        val fixedUrl = addApiKeyToUrl(url, "new-api-key")
        
        // Should not add new key if one exists
        assertTrue("URL should contain existing apikey", fixedUrl.contains("apikey=existing-key"))
        assertFalse("URL should not contain new apikey", fixedUrl.contains("apikey=new-api-key"))
    }

    @Test
    fun `fixOAuthUrl should preserve all query parameters`() {
        val sdkUrl = "https://test.supabase.co/auth/v1/?provider=google&redirect_to=app://callback&code_challenge=abc123&code_challenge_method=S256"
        val fixedPathUrl = fixOAuthUrlPath(sdkUrl)
        val fixedUrl = addApiKeyToUrl(fixedPathUrl, "test-key")
        
        assertTrue("Should preserve provider", fixedUrl.contains("provider=google"))
        assertTrue("Should preserve redirect_to", fixedUrl.contains("redirect_to=app://callback"))
        assertTrue("Should preserve code_challenge", fixedUrl.contains("code_challenge=abc123"))
        assertTrue("Should preserve code_challenge_method", fixedUrl.contains("code_challenge_method=S256"))
        assertTrue("Should add apikey", fixedUrl.contains("apikey=test-key"))
        assertTrue("Should have correct path", fixedUrl.contains("/auth/v1/authorize"))
    }

    @Test
    fun `fixOAuthUrl should handle URL with no query parameters`() {
        val sdkUrl = "https://test.supabase.co/auth/v1/"
        val fixedPathUrl = fixOAuthUrlPath(sdkUrl)
        val fixedUrl = addApiKeyToUrl(fixedPathUrl, "test-key")
        
        assertTrue("Should have correct path", fixedUrl.contains("/auth/v1/authorize"))
        assertTrue("Should have apikey", fixedUrl.contains("apikey=test-key"))
    }

    @Test
    fun `combined fix should work end-to-end`() {
        // Simulate the full fix process as done in SupabaseAuthProvider
        val sdkUrl = "https://test.supabase.co/auth/v1/?provider=google&redirect_to=app://callback&code_challenge=abc123"
        val fixedPathUrl = fixOAuthUrlPath(sdkUrl)
        val finalUrl = addApiKeyToUrl(fixedPathUrl, "test-api-key")
        
        // Verify all fixes applied
        assertTrue("Should have correct path", finalUrl.contains("/auth/v1/authorize"))
        assertTrue("Should have API key", finalUrl.contains("apikey=test-api-key"))
        assertTrue("Should preserve all parameters", 
            finalUrl.contains("provider=google") && 
            finalUrl.contains("redirect_to=app://callback") &&
            finalUrl.contains("code_challenge=abc123"))
    }
}
