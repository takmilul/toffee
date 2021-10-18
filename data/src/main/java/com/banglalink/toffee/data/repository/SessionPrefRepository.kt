package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.SessionPrefData

interface SessionPrefRepository {
    suspend fun insert(vararg items: SessionPrefData)
    suspend fun deleteAll()
    suspend fun getPrefString(key: String): String?
    suspend fun getPrefInt(key: String): Int?
    suspend fun getPrefBoolean(key: String): Boolean?
    suspend fun getPrefLong(key: String): Long?
}