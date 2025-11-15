package com.learncodes.mynote.utils

import com.learncodes.mynote.data.ChecklistItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ChecklistUtils {
    private val gson = Gson()

    fun toJson(items: List<ChecklistItem>): String {
        return gson.toJson(items)
    }

    fun fromJson(json: String): List<ChecklistItem> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<ChecklistItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}