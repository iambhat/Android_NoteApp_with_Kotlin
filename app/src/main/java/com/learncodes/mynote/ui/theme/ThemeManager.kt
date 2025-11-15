package com.learncodes.mynote.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

class ThemeManager(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("selected_theme")
        private val PRIMARY_COLOR_KEY = intPreferencesKey("primary_color")
        private val ACCENT_COLOR_KEY = intPreferencesKey("accent_color")
        private val BACKGROUND_COLOR_KEY = intPreferencesKey("background_color")
    }

    val selectedTheme: Flow<AppTheme> = context.themeDataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: "Default"
        AppTheme.values().find { it.themeName == themeName } ?: AppTheme.DEFAULT
    }

    val customColors: Flow<CustomColors> = context.themeDataStore.data.map { preferences ->
        CustomColors(
            primary = preferences[PRIMARY_COLOR_KEY] ?: 0xFF6200EE.toInt(),
            accent = preferences[ACCENT_COLOR_KEY] ?: 0xFF03DAC5.toInt(),
            background = preferences[BACKGROUND_COLOR_KEY] ?: 0xFFF5F5F5.toInt()
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.themeName
            preferences[PRIMARY_COLOR_KEY] = theme.primaryColor
            preferences[ACCENT_COLOR_KEY] = theme.accentColor
            preferences[BACKGROUND_COLOR_KEY] = theme.backgroundColor
        }
    }

    suspend fun setCustomColors(primary: Int, accent: Int, background: Int) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_KEY] = "Custom"
            preferences[PRIMARY_COLOR_KEY] = primary
            preferences[ACCENT_COLOR_KEY] = accent
            preferences[BACKGROUND_COLOR_KEY] = background
        }
    }
}

