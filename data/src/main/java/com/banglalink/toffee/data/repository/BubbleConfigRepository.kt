package com.banglalink.toffee.data.repository

import com.banglalink.toffee.model.BubbleConfig

interface BubbleConfigRepository {
    suspend fun insert(bubbleConfig: BubbleConfig): Long
    suspend fun delete(bubbleConfig: BubbleConfig)
    suspend fun getLatestConfig(): BubbleConfig?
    suspend fun getConfigById(id: Long): BubbleConfig?
}