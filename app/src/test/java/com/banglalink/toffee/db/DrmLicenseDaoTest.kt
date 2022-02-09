package com.banglalink.toffee.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.DrmLicenseDao
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.dao.TVChannelDao
import com.banglalink.toffee.data.database.entities.DrmLicenseEntity
import com.banglalink.toffee.data.database.entities.FavoriteItem
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.repository.DrmLicenseRepository
import com.banglalink.toffee.data.repository.impl.DrmLicenseRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
class DrmLicenseDaoTest {

    private lateinit var toffeeDb: ToffeeDatabase
    private lateinit var drmLicenseDao: DrmLicenseDao
    private lateinit var drmRepo: DrmLicenseRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        toffeeDb = Room.inMemoryDatabaseBuilder(context, ToffeeDatabase::class.java).build()
        drmLicenseDao = toffeeDb.getDrmLicenseDao()
        drmRepo = DrmLicenseRepositoryImpl(drmLicenseDao)
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        toffeeDb.close()
    }

    @Test
    fun testInsertData() = runBlocking {
        val byteArray = ByteArray(12) {
            it.toByte()
        }
        val drmInfo = DrmLicenseEntity(5, "hello", byteArray, System.currentTimeMillis())
        drmRepo.insert(drmInfo)
        val newInfo = drmRepo.getByChannelId(5)
        Assert.assertArrayEquals(newInfo?.license, byteArray)

        val b2 = ByteArray(49)
        Random.Default.nextBytes(b2)
        val d2 = DrmLicenseEntity(6, "hello", b2, System.currentTimeMillis())
        drmRepo.insert(d2)
        Assert.assertEquals(54, drmLicenseDao.getByChannelId(6)?.license?.size)
        val dn2 = drmRepo.getByChannelId(6)
        Assert.assertArrayEquals(dn2?.license, b2)
    }
}