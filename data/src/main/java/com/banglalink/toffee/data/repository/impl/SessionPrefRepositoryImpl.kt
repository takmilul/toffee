package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.SessionPrefDao
import com.banglalink.toffee.data.database.entities.SessionPrefData
import com.banglalink.toffee.data.repository.SessionPrefRepository

class SessionPrefRepositoryImpl(private val dao: SessionPrefDao): SessionPrefRepository {
    override suspend fun insert(vararg items: SessionPrefData) {
        dao.insert(*items)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun getPrefString(key: String): String? {
        return dao.getPrefString(key)
    }

    override suspend fun getPrefInt(key: String): Int? {
        return dao.getPrefString(key)?.toInt()
    }

    override suspend fun getPrefBoolean(key: String): Boolean? {
        return dao.getPrefString(key)?.toBoolean()
    }

    override suspend fun getPrefLong(key: String): Long? {
        return dao.getPrefString(key)?.toLong()
    }
}