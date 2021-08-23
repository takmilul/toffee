package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.DrmLicenseDao
import com.banglalink.toffee.data.database.entities.DrmLicenseEntity
import com.banglalink.toffee.data.repository.DrmLicenseRepository
import kotlin.random.Random

class DrmLicenseRepositoryImpl(private val dao: DrmLicenseDao): DrmLicenseRepository {
    override suspend fun insert(item: DrmLicenseEntity) {
        val entry = ByteArray(item.license.size + 5)
        Random.Default.nextBytes(entry)
        item.license.reversedArray().copyInto(entry, 2)
        dao.insert(item.copy(license = entry))
    }

    override suspend fun delete(item: DrmLicenseEntity) {
        dao.delete(item)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun getByChannelId(channelId: Long): DrmLicenseEntity? {
        return dao.getByChannelId(channelId)?.let {
            it.copy(license = it.license.sliceArray(2..it.license.size - 4).reversedArray())
        }
    }
}
