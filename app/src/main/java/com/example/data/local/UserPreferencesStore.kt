package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vinTradeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vintrade_preferences"
)

class UserPreferencesStore(private val context: Context) {
    private object Keys {
        val VAULT_POINTS = intPreferencesKey("vault_points")
        val LIFETIME_POINTS = intPreferencesKey("lifetime_points")
        val DISPLAY_CURRENCY = stringPreferencesKey("display_currency")
    }

    val vaultPointsFlow: Flow<Int> = context.vinTradeDataStore.data.map { prefs ->
        prefs[Keys.VAULT_POINTS] ?: 1650
    }

    val lifetimePointsFlow: Flow<Int> = context.vinTradeDataStore.data.map { prefs ->
        prefs[Keys.LIFETIME_POINTS] ?: 1650
    }

    val displayCurrencyFlow: Flow<String> = context.vinTradeDataStore.data.map { prefs ->
        prefs[Keys.DISPLAY_CURRENCY] ?: "USD"
    }

    suspend fun addVaultPoints(delta: Int) {
        context.vinTradeDataStore.edit { prefs ->
            val current = prefs[Keys.VAULT_POINTS] ?: 1650
            val lifetime = prefs[Keys.LIFETIME_POINTS] ?: 1650
            prefs[Keys.VAULT_POINTS] = (current + delta).coerceAtLeast(0)
            if (delta > 0) {
                prefs[Keys.LIFETIME_POINTS] = lifetime + delta
            }
        }
    }

    suspend fun spendVaultPoints(cost: Int): Boolean {
        var success = false
        context.vinTradeDataStore.edit { prefs ->
            val current = prefs[Keys.VAULT_POINTS] ?: 1650
            if (current >= cost) {
                prefs[Keys.VAULT_POINTS] = current - cost
                success = true
            }
        }
        return success
    }

    suspend fun setDisplayCurrency(currency: String) {
        context.vinTradeDataStore.edit { prefs ->
            prefs[Keys.DISPLAY_CURRENCY] = currency
        }
    }
}
