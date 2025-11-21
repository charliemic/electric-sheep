package com.electricsheep.app.data.remote

import com.electricsheep.app.data.model.InspirationalQuote
import com.electricsheep.app.error.NetworkError
import com.electricsheep.app.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Simple HTTP client for fetching inspirational quotes from a public API.
 *
 * Uses the ZenQuotes API to retrieve a random inspirational quote:
 * https://zenquotes.io/api/random
 */
object QuoteApi {

    private const val TAG = "QuoteApi"
    private const val QUOTE_URL = "https://zenquotes.io/api/random"

    /**
     * Fetch a random inspirational quote from the remote API.
     *
     * @return Result containing InspirationalQuote on success, or NetworkError on failure
     */
    suspend fun fetchRandomQuote(): Result<InspirationalQuote> = withContext(Dispatchers.IO) {
        val url = URL(QUOTE_URL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val error = NetworkError.fromException(
                    IOException("Unexpected response code: $responseCode")
                )
                error.log(TAG, "Failed to fetch quote - HTTP $responseCode")
                return@withContext Result.failure(error)
            }

            val body = connection.inputStream.bufferedReader().use(BufferedReader::readText)

            // ZenQuotes returns a JSON array; take the first element:
            // [ { "q": "<quote>", "a": "<author>", "h": "<html>" } ]
            val jsonArray = org.json.JSONArray(body)
            if (jsonArray.length() == 0) {
                val error = NetworkError.Unknown(
                    IOException("Empty quote array")
                )
                error.log(TAG, "Empty response from quote API")
                return@withContext Result.failure(error)
            }

            val first = jsonArray.getJSONObject(0)
            val quote = first.optString("q").trim()
            val author = first.optString("a").trim()

            if (quote.isEmpty()) {
                val error = NetworkError.Unknown(
                    IOException("Quote content is empty")
                )
                error.log(TAG, "Quote content is empty")
                return@withContext Result.failure(error)
            }

            val combined = if (author.isNotEmpty()) {
                "$quote — $author"
            } else {
                quote
            }

            Result.success(InspirationalQuote(text = combined))
        } catch (e: Exception) {
            val networkError = NetworkError.fromException(e)
            networkError.log(TAG, "Failed to fetch inspirational quote")
            Result.failure(networkError)
        } finally {
            connection.disconnect()
        }
    }
}
