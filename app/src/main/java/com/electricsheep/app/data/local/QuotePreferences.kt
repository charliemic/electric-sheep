package com.electricsheep.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.electricsheep.app.data.model.InspirationalQuote

/**
 * Simple SharedPreferences-backed storage for inspirational quote settings.
 *
 * - Persists whether quotes are enabled (on by default)
 * - Persists the last successfully retrieved quote for offline fallback
 */
class QuotePreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "inspirational_quote_prefs"
        private const val KEY_QUOTES_ENABLED = "quotes_enabled"
        private const val KEY_LAST_QUOTE_TEXT = "last_quote_text"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Whether inspirational quotes are enabled for the user.
     * Defaults to true when the user has not explicitly changed it.
     */
    fun areQuotesEnabled(): Boolean {
        return prefs.getBoolean(KEY_QUOTES_ENABLED, true)
    }

    /**
     * Persist the user preference for showing or hiding inspirational quotes.
     */
    fun setQuotesEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_QUOTES_ENABLED, enabled)
            .apply()
    }

    /**
     * Get the last successfully retrieved quote, or null if none is stored.
     */
    fun getLastQuote(): InspirationalQuote? {
        val text = prefs.getString(KEY_LAST_QUOTE_TEXT, null) ?: return null
        return InspirationalQuote(text = text)
    }

    /**
     * Persist the last successfully retrieved quote for offline fallback.
     */
    fun setLastQuote(quote: InspirationalQuote) {
        prefs.edit()
            .putString(KEY_LAST_QUOTE_TEXT, quote.text)
            .apply()
    }
}


