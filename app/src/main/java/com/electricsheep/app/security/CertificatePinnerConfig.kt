package com.electricsheep.app.security

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate

/**
 * Certificate pinning configuration for Supabase API calls.
 * 
 * Prevents man-in-the-middle (MITM) attacks by ensuring API calls
 * only connect to legitimate Supabase servers.
 * 
 * **Security Benefits:**
 * - Prevents MITM attacks on API calls
 * - Ensures data integrity in transit
 * - Protects authentication tokens
 * 
 * **Certificate Pins:**
 * - Pins are SHA-256 hashes of public keys
 * - Multiple pins support certificate rotation
 * - Backup pins ensure continuity
 * 
 * **How to Get Certificate Pins:**
 * 1. Connect to Supabase API: `openssl s_client -connect <project>.supabase.co:443 -showcerts`
 * 2. Extract certificate chain
 * 3. Calculate SHA-256 hash: `openssl x509 -in cert.pem -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64`
 * 4. Add pins to this configuration
 * 
 * **Note**: Certificate pins must be updated when Supabase rotates certificates.
 * Check Supabase status page or documentation for certificate updates.
 */
object CertificatePinnerConfig {
    
    /**
     * Create OkHttp CertificatePinner with Supabase certificate pins.
     * 
     * **Current Pins**: Placeholder pins - must be replaced with actual Supabase pins
     * 
     * **Format**: `sha256/<base64-encoded-hash>`
     * 
     * **Multiple Pins**: Support certificate rotation by pinning multiple keys
     * 
     * @return CertificatePinner configured for Supabase domains
     */
    fun createCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            // Supabase main domain
            .add("*.supabase.co", "sha256/PLACEHOLDER_PIN_1")
            .add("*.supabase.co", "sha256/PLACEHOLDER_PIN_2") // Backup pin for rotation
            // Supabase alternative domain (if used)
            .add("*.supabase.in", "sha256/PLACEHOLDER_PIN_1")
            .add("*.supabase.in", "sha256/PLACEHOLDER_PIN_2")
            .build()
    }
    
    /**
     * Create OkHttpClient with certificate pinning enabled.
     * 
     * This client can be used as the engine for Ktor HTTP client.
     * 
     * @return OkHttpClient with certificate pinning configured
     */
    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .certificatePinner(createCertificatePinner())
            // Additional security configurations
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }
    
    /**
     * Create Ktor HttpClient with certificate pinning enabled.
     * 
     * This client can be passed to Supabase client creation.
     * 
     * @return HttpClient with OkHttp engine and certificate pinning
     */
    fun createKtorHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                preconfigured = createOkHttpClient()
            }
        }
    }
    
    /**
     * Extract certificate pin from X509Certificate.
     * 
     * Useful for extracting pins from certificates during development/testing.
     * 
     * @param certificate X509Certificate to extract pin from
     * @return SHA-256 pin in format "sha256/<base64-hash>"
     */
    fun extractPin(certificate: X509Certificate): String {
        val publicKey = certificate.publicKey
        val publicKeyBytes = publicKey.encoded
        val sha256 = java.security.MessageDigest.getInstance("SHA-256")
        val hash = sha256.digest(publicKeyBytes)
        val base64Hash = android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
        return "sha256/$base64Hash"
    }
    
    /**
     * Validate that certificate pinning is enabled.
     * 
     * @param client OkHttpClient to validate
     * @return true if certificate pinning is configured
     */
    fun isPinningEnabled(client: OkHttpClient): Boolean {
        return client.certificatePinner.pins.isNotEmpty()
    }
}

