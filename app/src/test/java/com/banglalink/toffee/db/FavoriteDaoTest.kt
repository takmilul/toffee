package com.banglalink.toffee.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.dao.TVChannelDao
import com.banglalink.toffee.data.database.entities.FavoriteItem
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
class FavoriteDaoTest {

    private lateinit var toffeeDb: ToffeeDatabase
    private lateinit var favoriteDao: FavoriteItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        toffeeDb = Room.inMemoryDatabaseBuilder(context, ToffeeDatabase::class.java).build()
        favoriteDao = toffeeDb.getFavoriteItemsDao()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        toffeeDb.close()
    }

    @Test
    fun testInsertData() = runBlocking {
        favoriteDao.insert(FavoriteItem(channelId = 2233, isFavorite = 1))
        favoriteDao.insert(FavoriteItem(channelId = 2233, isFavorite = 0))

        Assert.assertEquals(1, favoriteDao.isFavorite(2233))
        Assert.assertEquals(null, favoriteDao.isFavorite(2234))
        Assert.assertEquals(0, favoriteDao.isFavorite(2234))
    }
}