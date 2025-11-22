package com.electricsheep.app.auth

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Helper for generating TOTP codes in tests.
 * 
 * **WARNING**: This is for TESTING ONLY. Never use in production code.
 * 
 * This allows automated testing of MFA flows without requiring a physical device
 * with an authenticator app. The TOTP algorithm matches Google Authenticator.
 * 
 * **Security Note**: 
 * - Only available in test builds
 * - Uses standard TOTP algorithm (RFC 6238)
 * - Same algorithm used by authenticator apps
 * - Allows testing without bypassing security
 */
object TotpTestHelper {
    
    /**
     * Generate a TOTP code from a secret key.
     * 
     * @param secret Base32-encoded secret key
     * @param timeStep Time step (defaults to current time / 30 seconds)
     * @return 6-digit TOTP code as string
     * 
     * @throws IllegalArgumentException if secret is invalid
     */
    fun generateTotpCode(secret: String, timeStep: Long = System.currentTimeMillis() / 1000 / 30): String {
        try {
            // Decode Base32 secret
            val secretBytes = base32Decode(secret)
            
            // Generate HMAC-SHA1
            val mac = Mac.getInstance("HmacSHA1")
            val secretKeySpec = SecretKeySpec(secretBytes, "HmacSHA1")
            mac.init(secretKeySpec)
            
            // Generate hash for current time step
            val timeStepBytes = ByteArray(8)
            for (i in 7 downTo 0) {
                timeStepBytes[i] = (timeStep and 0xFF).toByte()
                timeStep = timeStep shr 8
            }
            
            val hash = mac.doFinal(timeStepBytes)
            
            // Dynamic truncation (RFC 6238)
            val offset = hash[hash.size - 1].toInt() and 0x0F
            val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                        ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                        ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                        (hash[offset + 3].toInt() and 0xFF)
            
            val otp = binary % 1_000_000
            
            // Format as 6-digit string with leading zeros
            return String.format("%06d", otp)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid secret or TOTP generation failed: ${e.message}", e)
        }
    }
    
    /**
     * Generate TOTP code for a specific time (useful for testing expired codes).
     * 
     * @param secret Base32-encoded secret key
     * @param secondsSinceEpoch Time in seconds since epoch
     * @return 6-digit TOTP code as string
     */
    fun generateTotpCodeForTime(secret: String, secondsSinceEpoch: Long): String {
        val timeStep = secondsSinceEpoch / 30
        return generateTotpCode(secret, timeStep)
    }
    
    /**
     * Generate TOTP code for previous time step (useful for testing expired codes).
     * 
     * @param secret Base32-encoded secret key
     * @return 6-digit TOTP code for previous 30-second window
     */
    fun generatePreviousTotpCode(secret: String): String {
        val currentTimeStep = System.currentTimeMillis() / 1000 / 30
        return generateTotpCode(secret, currentTimeStep - 1)
    }
    
    /**
     * Generate TOTP code for next time step (useful for testing clock skew).
     * 
     * @param secret Base32-encoded secret key
     * @return 6-digit TOTP code for next 30-second window
     */
    fun generateNextTotpCode(secret: String): String {
        val currentTimeStep = System.currentTimeMillis() / 1000 / 30
        return generateTotpCode(secret, currentTimeStep + 1)
    }
    
    /**
     * Decode Base32 string to byte array.
     * 
     * @param base32 Base32-encoded string
     * @return Decoded byte array
     */
    private fun base32Decode(base32: String): ByteArray {
        val base32Upper = base32.uppercase().replace(" ", "")
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        
        var bits = 0
        var value = 0
        var index = 0
        val output = mutableListOf<Byte>()
        
        for (char in base32Upper) {
            val charIndex = base32Chars.indexOf(char)
            if (charIndex == -1) continue
            
            value = (value shl 5) or charIndex
            bits += 5
            
            if (bits >= 8) {
                output.add((value shr (bits - 8)).toByte())
                bits -= 8
            }
        }
        
        return output.toByteArray()
    }
    
    /**
     * Validate TOTP code format (6 digits).
     * 
     * @param code Code to validate
     * @return true if code is valid format, false otherwise
     */
    fun isValidTotpFormat(code: String): Boolean {
        return code.matches(Regex("\\d{6}"))
    }
}

