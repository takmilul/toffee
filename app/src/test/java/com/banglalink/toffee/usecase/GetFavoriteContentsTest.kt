package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.FavoriteContentRequest
import com.banglalink.toffee.data.network.response.FavoriteContentResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ContentBean
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response


class GetFavoriteContentsTest :BaseUseCaseTest(){

    @Test
    fun get_contents_success(){

        runBlocking {
            //set up test
            setupPref()
            val channelInfoList = mutableListOf<ChannelInfo>()
            channelInfoList.add(ChannelInfo().apply {
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
            })

            val getContents = GetFavoriteContents(SessionPreference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.getFavoriteContents(any<FavoriteContentRequest>())).thenReturn(
                Response.success(FavoriteContentResponse(
                    ContentBean(channelInfoList,1,1)
                )))

            //test method
            val resultChannelInfoList = getContents.execute()
            //verify it
            assertEquals(resultChannelInfoList[0].formattedViewCount,"1T")
            assertEquals(resultChannelInfoList[0].formattedDuration,"04:05")
            assertEquals(resultChannelInfoList[0].program_name,"Hello BD")
            assertEquals(resultChannelInfoList[0].content_provider_name,"GSeries")
            verify(mockToffeeApi).getFavoriteContents(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,30)

            })
            //verify that offset calculation is OK
            assertEquals(getContents.mOffset,1)
        }

    }
}