package com.smartinvestor.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smartinvestor.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme_mode")
        private val BIOMETRIC_KEY = booleanPreferencesKey("biometric_enabled")
        private val VIRTUAL_BALANCE_KEY = stringPreferencesKey("virtual_cash_balance")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            ThemeMode.DARK.name -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BIOMETRIC_KEY] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BIOMETRIC_KEY] = enabled
        }
    }

    val virtualCashBalance: Flow<Double> = dataStore.data.map { preferences ->
        preferences[VIRTUAL_BALANCE_KEY]?.toDoubleOrNull() ?: 1000000.0 // Default ₹10,00,000 demo capital
    }

    suspend fun setVirtualCashBalance(balance: Double) {
        dataStore.edit { preferences ->
            preferences[VIRTUAL_BALANCE_KEY] = balance.toString()
        }
    }
}
