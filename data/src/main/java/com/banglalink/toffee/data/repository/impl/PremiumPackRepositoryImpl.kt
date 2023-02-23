package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.PremiumPackDao
import com.banglalink.toffee.data.repository.PremiumPackRepository
import com.banglalink.toffee.model.ActivePack

class PremiumPackRepositoryImpl(private val dao: PremiumPackDao): PremiumPackRepository {
    override suspend fun insert(item: ActivePack): Long {
        return dao.insertItem(item)
    }
    
    override suspend fun insertAll(vararg items: ActivePack): LongArray {
        return dao.insertAll(*items)
    }
    
    override suspend fun getPackById(id: Int): ActivePack? {
        return dao.getPackById(id)
    }
    
    override suspend fun getAllPacks(): List<ActivePack>? {
        return dao.getAllPacks()
    }
    
    override suspend fun delete(item: ActivePack) {
        return dao.delete(item)
    }
    
    override suspend fun deleteAll() {
        return dao.deleteAllPacks()
    }
}