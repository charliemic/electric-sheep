package com.electricsheep.testautomation.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Service for interacting with Ollama (self-hosted LLM).
 * 
 * **Pattern**: Follows the framework's service abstraction pattern:
 * - Uses `Result<T>` for error handling
 * - Handles network errors gracefully
 * - Provides fallback behavior
 * - Logs errors appropriately
 * 
 * **Self-Hosted**: Uses local Ollama instance (no cloud services).
 */
class OllamaService(
    private val baseUrl: String = System.getenv("OLLAMA_BASE_URL") ?: "http://localhost:11434",
    private val defaultModel: String = System.getenv("OLLAMA_MODEL") ?: "llama3.2",
    private val connectTimeout: Long = 30,
    private val readTimeout: Long = 60
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
        .readTimeout(readTimeout, TimeUnit.SECONDS)
        .build()
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }
    
    /**
     * Generate text using Ollama.
     * 
     * @param prompt The prompt to send to the model
     * @param model Optional model name (defaults to configured model)
     * @return Result containing generated text or error
     */
    suspend fun generate(
        prompt: String,
        model: String = defaultModel
    ): Result<String> {
        return try {
            val requestBody = """
            {
                "model": ${objectMapper.writeValueAsString(model)},
                "prompt": ${objectMapper.writeValueAsString(prompt)},
                "stream": false,
                "options": {
                    "temperature": 0.8,
                    "num_predict": 200
                }
            }
            """.trimIndent()
            
            val request = Request.Builder()
                .url("$baseUrl/api/generate")
                .header("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: return Result.failure(
                Exception("Empty response from Ollama")
            )
            
            if (!response.isSuccessful) {
                logger.warn("Ollama API error: ${response.code} - $responseBody")
                return Result.failure(
                    Exception("Ollama API error: ${response.code} - $responseBody")
                )
            }
            
            // Parse JSON response
            val jsonNode = objectMapper.readTree(responseBody)
            val generatedText = jsonNode.get("response")
                ?.asText()
                ?.trim()
                ?: return Result.failure(
                    Exception("Could not parse Ollama response: $responseBody")
                )
            
            logger.debug("✅ Ollama generated text (model: $model, length: ${generatedText.length})")
            Result.success(generatedText)
            
        } catch (e: Exception) {
            logger.warn("Ollama generation failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Generate text with images (for vision models like LLaVA).
     * 
     * @param prompt The prompt to send to the model
     * @param images List of image files to include
     * @param model Optional model name (defaults to configured model)
     * @return Result containing generated text or error
     */
    suspend fun generateWithImages(
        prompt: String,
        images: List<java.io.File>,
        model: String = defaultModel
    ): Result<String> {
        return try {
            // Encode images to base64
            val imageData = images.map { file ->
                val bytes = file.readBytes()
                val base64 = java.util.Base64.getEncoder().encodeToString(bytes)
                base64
            }
            
            val requestBody = """
            {
                "model": ${objectMapper.writeValueAsString(model)},
                "prompt": ${objectMapper.writeValueAsString(prompt)},
                "images": ${objectMapper.writeValueAsString(imageData)},
                "stream": false,
                "options": {
                    "temperature": 0.8,
                    "num_predict": 200
                }
            }
            """.trimIndent()
            
            val request = Request.Builder()
                .url("$baseUrl/api/generate")
                .header("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: return Result.failure(
                Exception("Empty response from Ollama")
            )
            
            if (!response.isSuccessful) {
                logger.warn("Ollama API error: ${response.code} - $responseBody")
                return Result.failure(
                    Exception("Ollama API error: ${response.code} - $responseBody")
                )
            }
            
            // Parse JSON response
            val jsonNode = objectMapper.readTree(responseBody)
            val generatedText = jsonNode.get("response")
                ?.asText()
                ?.trim()
                ?: return Result.failure(
                    Exception("Could not parse Ollama response: $responseBody")
                )
            
            logger.debug("✅ Ollama generated text with images (model: $model, images: ${images.size}, length: ${generatedText.length})")
            Result.success(generatedText)
            
        } catch (e: Exception) {
            logger.warn("Ollama generation with images failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if Ollama is available and responding.
     * 
     * @return true if Ollama is available, false otherwise
     */
    suspend fun isAvailable(): Boolean {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/api/tags")
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            logger.debug("Ollama not available: ${e.message}")
            false
        }
    }
    
    /**
     * List available models.
     * 
     * @return Result containing list of model names or error
     */
    suspend fun listModels(): Result<List<String>> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/api/tags")
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: return Result.failure(
                Exception("Empty response from Ollama")
            )
            
            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Ollama API error: ${response.code} - $responseBody")
                )
            }
            
            val jsonNode = objectMapper.readTree(responseBody)
            val models = jsonNode.get("models")
                ?.map { it.get("name").asText() }
                ?: emptyList()
            
            Result.success(models)
        } catch (e: Exception) {
            logger.warn("Failed to list Ollama models: ${e.message}", e)
            Result.failure(e)
        }
    }
}

