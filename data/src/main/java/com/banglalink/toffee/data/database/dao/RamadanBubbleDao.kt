package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.model.RamadanScheduled
import com.banglalink.toffee.model.RamadanScheduledResponse

@Dao
interface  RamadanBubbleDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg ramadanScheduled: RamadanScheduled): LongArray

    @Query("SELECT * FROM RamadanScheduled WHERE date(sehriStart) = :currentDate")
    suspend fun getRamadanBubbleItem(currentDate: String): RamadanScheduled?
}