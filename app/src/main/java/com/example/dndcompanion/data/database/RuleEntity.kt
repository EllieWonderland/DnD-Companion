package com.example.dndcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val content: String,
    val tags: List<String>
)

class StringListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        if (data == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, listType)
    }
}
