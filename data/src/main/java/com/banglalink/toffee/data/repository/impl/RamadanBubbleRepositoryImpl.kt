package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.RamadanBubbleDao
import com.banglalink.toffee.data.repository.RamadanBubbleRepository
import com.banglalink.toffee.model.RamadanScheduled
import com.banglalink.toffee.model.RamadanScheduledResponse

class RamadanBubbleRepositoryImpl (private val dao: RamadanBubbleDao) : RamadanBubbleRepository {
    override suspend fun insertAll(vararg ramadanScheduled: RamadanScheduled): LongArray {
        deleteAllRows()
        return dao.insertAll(*ramadanScheduled)
    }
    override suspend fun getAllRamadanItems(currentDate: String): RamadanScheduled? {
        return dao.getRamadanBubbleItem(currentDate)
    }
    override suspend fun deleteAllRows() {
        return dao.deleteAllRows()
    }
}