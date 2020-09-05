package com.banglalink.toffee.data.storage

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import java.util.*

interface BaseDAO<T> where T: BaseModel {


    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(modelData: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(modelData: T)

}