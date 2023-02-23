package com.banglalink.toffee.data.repository

import com.banglalink.toffee.model.ActivePack

interface PremiumPackRepository {
    suspend fun insert(item: ActivePack): Long
    suspend fun insertAll(vararg items: ActivePack): LongArray
    suspend fun getPackById(id: Int): ActivePack?
    suspend fun getAllPacks(): List<ActivePack>?
    suspend fun delete(item: ActivePack)
    suspend fun deleteAll()
}