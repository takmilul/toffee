package com.banglalink.toffee.data.repository.impl

import androidx.room.withTransaction
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.ShareCountDao
import com.banglalink.toffee.data.database.entities.ShareCount
import com.banglalink.toffee.data.repository.ShareCountRepository

class ShareCountRepositoryImpl(private val db: ToffeeDatabase, private val dao: ShareCountDao) : ShareCountRepository {
    override suspend fun insert(item: ShareCount): Long {
        return dao.insert(item)
    }
    
    override suspend fun insertAll(vararg items: ShareCount): LongArray {
        return dao.insertAll(*items)
    }

    override suspend fun getShareCountByContentId(contentId: Int): Long? {
        return dao.getShareCountByContentId(contentId)
    }
    
    override suspend fun updateShareCount(shareStatusList: ArrayList<ShareCount>) {
        db.withTransaction {
            val dbList = mutableListOf<ShareCount>()
            val map: MutableMap<Int, ShareCount> = mutableMapOf()
            shareStatusList.forEach {
                val item = map[it.contentId]
                if (item == null){
                    map[it.contentId] = it
                }
                else {
                    item.count++
                }
            }
            val ids = map.keys.toList()
            for (id in ids.indices step 999) {
                val subList = ids.subList(id, minOf(ids.size, id + 999))
                val dbShareCounts = dao.getShareCountListByContentIds(subList)
                dbList.addAll(dbShareCounts)
            }
            val updateList = dbList.map { item ->
                item.count = map[item.contentId]?.count?.plus(item.count) ?: 0
                map.remove(item.contentId)
                item
            }.toMutableList()
            updateList.addAll(map.values)
            dao.insertAll(*updateList.toTypedArray())
        }
    }
    
    override suspend fun updateShareCount(contentId: Int, status: Int): Int {
        val count = getShareCountByContentId(contentId)
        
        return if(count == null){
            insert(ShareCount(contentId, status.toLong())).toInt()
        }
        else{
            dao.updateShareCount(contentId, count + status)
        }
    }
}