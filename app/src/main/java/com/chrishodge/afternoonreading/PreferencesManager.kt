package com.chrishodge.afternoonreading

import android.content.Context

class PreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )

    // Save different types of data
    fun saveStringArray(key: String, values: Array<String>) {
        // Convert array to Set for storage
        val stringSet = values.toSet()
        sharedPreferences.edit().putStringSet(key, stringSet).apply()
    }

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    // Load different types of data
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getStringArray(key: String, defaultValue: Array<String> = emptyArray()): Array<String> {
        // Retrieve the Set and convert back to Array
        val stringSet = sharedPreferences.getStringSet(key, defaultValue.toSet())
        return stringSet?.toTypedArray() ?: defaultValue
    }
}
