package com.banglalink.toffee.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.TVChannelDao
import com.banglalink.toffee.data.database.entities.TVChannelItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class TVChannelDBTest {

    private lateinit var toffeeDb: ToffeeDatabase
    private lateinit var tvChannelDao: TVChannelDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        toffeeDb = Room.inMemoryDatabaseBuilder(context, ToffeeDatabase::class.java).build()
        tvChannelDao = toffeeDb.getTVChannelsDao()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        toffeeDb.close()
    }

    @Test
    fun testInsertData() = runBlocking {
        tvChannelDao.insert(TVChannelItem(
            1L, "LIVE", 1,"Movies", "Hello world", 1234L
        ))

        Assert.assertEquals(1, tvChannelDao.getAllItems().first().size)
    }
}