package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.model.ActivePack

@Dao
interface PremiumPackDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ActivePack): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg items: ActivePack): LongArray
    
    @Query("SELECT * FROM PremiumPackItem WHERE packId=:id LIMIT 1")
    suspend fun getPackById(id: Int): ActivePack?
    
    @Query("SELECT * FROM PremiumPackItem")
    suspend fun getAllPacks(): List<ActivePack>?
    
    @Query("DELETE FROM PremiumPackItem")
    suspend fun deleteAllPacks()
    
    @Delete
    suspend fun delete(activePack: ActivePack)
}