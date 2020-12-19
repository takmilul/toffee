package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.ContinueWatchingItem
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContinueWatchingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(item: ContinueWatchingItem)

    @Query("DELETE FROM ContinueWatchingItem WHERE categoryId=:catId AND customerId=:customerId AND id NOT IN " +
            "(SELECT id from ContinueWatchingItem WHERE categoryId=:catId AND customerId=:customerId ORDER BY updateTime DESC LIMIT 10)")
    abstract suspend fun deleteExtraRecents(catId: Int, customerId: Int)

    @Transaction
    open suspend fun insertItem(item: ContinueWatchingItem) {
        insert(item)
        deleteExtraRecents(item.categoryId, item.customerId)
    }

    @Query("SELECT * FROM ContinueWatchingItem WHERE categoryId=:catId AND customerId=:customerId ORDER BY updateTime DESC LIMIT 10")
    abstract fun getAllItemsByCategory(catId: Int, customerId: Int): Flow<List<ContinueWatchingItem>>
}