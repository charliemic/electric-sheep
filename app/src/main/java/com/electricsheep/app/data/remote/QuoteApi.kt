package com.electricsheep.app.data.remote

import com.electricsheep.app.data.model.InspirationalQuote
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
     * @throws IOException if the network request fails or the response is invalid.
     */
    suspend fun fetchRandomQuote(): InspirationalQuote = withContext(Dispatchers.IO) {
        val url = URL(QUOTE_URL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Unexpected response code: $responseCode")
            }

            val body = connection.inputStream.bufferedReader().use(BufferedReader::readText)

            // ZenQuotes returns a JSON array; take the first element:
            // [ { "q": "<quote>", "a": "<author>", "h": "<html>" } ]
            val jsonArray = org.json.JSONArray(body)
            if (jsonArray.length() == 0) {
                throw IOException("Empty quote array")
            }

            val first = jsonArray.getJSONObject(0)
            val quote = first.optString("q").trim()
            val author = first.optString("a").trim()

            if (quote.isEmpty()) {
                throw IOException("Quote content is empty")
            }

            val combined = if (author.isNotEmpty()) {
                "$quote — $author"
            } else {
                quote
            }

            InspirationalQuote(text = combined)
        } catch (e: Exception) {
            Logger.warn(TAG, "Failed to fetch inspirational quote", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }
}


