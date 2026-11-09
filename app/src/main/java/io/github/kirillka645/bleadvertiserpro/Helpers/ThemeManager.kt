package io.github.kirillka645.bleadvertiserpro.Helpers

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import io.github.kirillka645.bleadvertiserpro.R

class ThemeManager private constructor() {

    companion object {
        const val THEME_MODE_KEY = "theme_mode"
        private const val THEME_MODE_DEFAULT = "default"
        private const val THEME_MODE_LIGHT = "light"
        private const val THEME_MODE_DARK = "dark"

        private var instance: ThemeManager? = null

        fun getInstance(): ThemeManager {
            if (instance == null) {
                instance = ThemeManager()
            }
            return instance!!
        }
    }

    fun applyTheme(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val themeMode = preferences.getString(THEME_MODE_KEY, THEME_MODE_DEFAULT)
        val mode = when (themeMode) {
            THEME_MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    fun setTheme(context: Context, themeMode: String) {
        val normalizedMode = when (themeMode) {
            THEME_MODE_LIGHT, THEME_MODE_DARK -> themeMode
            else -> THEME_MODE_DEFAULT
        }
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
            .edit()
            .putString(THEME_MODE_KEY, normalizedMode)
            .apply()
        applyTheme(context)
    }

    fun getTheme(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        return preferences.getString(THEME_MODE_KEY, THEME_MODE_DEFAULT) ?: THEME_MODE_DEFAULT
    }

    fun getThemeString(context: Context): String {
        val resId = when (getTheme(context)) {
            THEME_MODE_LIGHT -> R.string.preference_theme_mode_light
            THEME_MODE_DARK -> R.string.preference_theme_mode_dark
            else -> R.string.preference_theme_mode_follow_system
        }
        return context.getString(resId)
    }
}
