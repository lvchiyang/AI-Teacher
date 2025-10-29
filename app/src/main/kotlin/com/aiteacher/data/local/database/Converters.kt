package com.aiteacher.data.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.aiteacher.domain.model.KnowledgeItem
import com.aiteacher.domain.model.LearningResource
import java.util.Date



/**
 * 类型转换器
 * 用于Room数据库中List和Map类型的字段转换
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toStringList(data: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, listType)
    }
    
    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }
    
    @TypeConverter
    fun toStringMap(data: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(data, mapType)
    }
    
    @TypeConverter
    fun fromKnowledgeItemList(list: List<KnowledgeItem>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toKnowledgeItemList(data: String): List<KnowledgeItem> {
        val listType = object : TypeToken<List<KnowledgeItem>>() {}.type
        return gson.fromJson(data, listType)
    }
    
    @TypeConverter
    fun fromLearningResourceList(list: List<LearningResource>): String {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toLearningResourceList(data: String): List<LearningResource> {
        val listType = object : TypeToken<List<LearningResource>>() {}.type
        return gson.fromJson(data, listType)
    }
}