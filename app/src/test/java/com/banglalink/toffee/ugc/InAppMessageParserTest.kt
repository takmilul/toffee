package com.banglalink.toffee.ugc

import android.net.Uri
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.GetCategories
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.util.InAppMessageParser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InAppMessageParserTest {
    private lateinit var categoryApi: GetCategories
    private lateinit var inAppMessageParser: InAppMessageParser

    @Before
    fun setup() {
        categoryApi = mockk()
        coEvery {
            categoryApi.loadData(0, 0)
        } returns listOf(
            Category(id = 1L, categoryName = "Movie"),
            Category(id = 9L, categoryName = "Drama Series"),
            Category(id = 2L, categoryName = "Music Videos"),
        )
        inAppMessageParser = InAppMessageParser(categoryApi)

        mockkObject(ToffeeAnalytics)
        every { ToffeeAnalytics.logBreadCrumb(any()) } returns mockk()
        every { ToffeeAnalytics.logException(any()) } returns mockk()
    }

    @Test
    fun testRouteFromUrl() = runBlocking {
        val ret = inAppMessageParser.parseUrlV2("https://toffeelive.com?routing=internal&page=categories&catid=1")
        Assert.assertNotNull(ret)
        Assert.assertTrue(ret is InAppMessageParser.RouteV2)
        Assert.assertEquals("Category (Movie)", ret?.name)

        val ret2 = inAppMessageParser.parseUrlV2("https://toffeelive.com?routing=internal&page=settings")
        Assert.assertNotNull(ret2)
        Assert.assertTrue(ret2 is InAppMessageParser.RouteV2)
        Assert.assertEquals("Settings", ret2?.name)

        val ret3 = inAppMessageParser.parseUrlV2("https://toffeelive.com?routing=internal&page=ugc_channel&owner_id=2233")
        Assert.assertNotNull(ret3)
        Assert.assertTrue(ret3?.destId is Uri)
        Assert.assertEquals("app.toffee://ugc_channel/2233", (ret3?.destId as Uri).toString())

        val ret4 = inAppMessageParser.parseUrlV2("https://toffeelive.com?routing=internal&page=search&keyword=natok")
        Assert.assertNotNull(ret4)
        Assert.assertTrue(ret4?.destId is Uri)
        Assert.assertEquals("app.toffee://search/natok", (ret4?.destId as Uri).toString())
    }
}