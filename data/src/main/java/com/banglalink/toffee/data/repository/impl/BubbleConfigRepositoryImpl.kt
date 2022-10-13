package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.BubbleConfigDao
import com.banglalink.toffee.data.repository.BubbleConfigRepository
import com.banglalink.toffee.model.BubbleConfig

class BubbleConfigRepositoryImpl(private val bubbleConfigDao: BubbleConfigDao): BubbleConfigRepository {
    
    override suspend fun insert(bubbleConfig: BubbleConfig): Long {
        return bubbleConfigDao.insert(bubbleConfig)
    }

    override suspend fun delete(bubbleConfig: BubbleConfig) {
        bubbleConfigDao.delete(bubbleConfig)
    }

    override suspend fun getLatestConfig(): BubbleConfig? {
        return bubbleConfigDao.getLatestConfig()
    }
    
    override suspend fun getConfigById(id: Long): BubbleConfig? {
        return bubbleConfigDao.getConfigById(id)
    }
}