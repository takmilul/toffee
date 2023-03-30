package com.banglalink.toffee.data.repository

import com.banglalink.toffee.model.RamadanScheduled
import com.banglalink.toffee.model.RamadanScheduledResponse

interface RamadanBubbleRepository {
    suspend fun insertAll(vararg ramadanScheduled: RamadanScheduled): LongArray
    suspend fun getAllRamadanItems(currentDate: String): RamadanScheduled?
}