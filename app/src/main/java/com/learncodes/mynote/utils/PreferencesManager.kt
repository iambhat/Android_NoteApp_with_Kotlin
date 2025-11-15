package com.learncodes.mynote.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val APP_PASSWORD = stringPreferencesKey("app_password")
        private val BIOMETRIC_ENABLED = stringPreferencesKey("biometric_enabled")
    }

    val appPassword: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_PASSWORD]
    }

    suspend fun setAppPassword(password: String?) {
        context.dataStore.edit { preferences ->
            if (password != null) {
                preferences[APP_PASSWORD] = password
            } else {
                preferences.remove(APP_PASSWORD)
            }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled.toString()
        }
    }
}