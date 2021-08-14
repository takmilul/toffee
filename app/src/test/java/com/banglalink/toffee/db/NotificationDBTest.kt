package com.banglalink.toffee.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.NotificationDao
import com.banglalink.toffee.data.database.entities.NotificationInfo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class NotificationDBTest {

    private lateinit var toffeeDb: ToffeeDatabase
    private lateinit var notificationDao: NotificationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        toffeeDb = Room.inMemoryDatabaseBuilder(context, ToffeeDatabase::class.java).build()
        notificationDao = toffeeDb.getNotificationDao()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        toffeeDb.close()
    }

    @Test
    fun testInsertData() = runBlocking {
        val id = notificationDao.insert(NotificationInfo(null, 0, null, null, 0, 0, null))
        val newId = notificationDao.insert(NotificationInfo(null, 0, null, null, 0, 0, null))

//        Assert.assertEquals(1, notificationDao.isSeen(id))
//        Assert.assertEquals(null, notificationDao.isSeen(id))
        Assert.assertEquals(1, id)
        Assert.assertEquals(2, newId)
    }
}