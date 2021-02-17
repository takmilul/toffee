package com.foxpress.toffeekotlin.usecase

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.response.HistoryContentResponse
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ContentBean
import com.banglalink.toffee.usecase.GetHistory
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response


class GetHistoryTest :BaseUseCaseTest(){

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

            val getContents = GetHistory(Preference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.getHistoryContents(any<HistoryContentRequest>())).thenReturn(
                Response.success(HistoryContentResponse(
                    ContentBean(channelInfoList,1,1)
                )))

            //test method
            val resultChannelInfoList = getContents.execute()
            //verify it
            assertEquals(resultChannelInfoList[0].formattedViewCount,"1T")
            assertEquals(resultChannelInfoList[0].formattedDuration,"04:05")
            assertEquals(resultChannelInfoList[0].program_name,"Hello BD")
            assertEquals(resultChannelInfoList[0].content_provider_name,"GSeries")
            assertEquals(getContents.mOffset,1)
            verify(mockToffeeApi).getHistoryContents(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,10)

            })
        }

    }

    @Test
    fun get_contents_success_when_response_null(){

        runBlocking {
            //set up test
            setupPref()

            val getContents = GetHistory(Preference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.getHistoryContents(any<HistoryContentRequest>())).thenReturn(
                Response.success(HistoryContentResponse(
                    ContentBean(null,0,0)
                )))

            //test method
            val resultChannelInfoList = getContents.execute()
            //verify it
            assertEquals(resultChannelInfoList.size,0)
            assertEquals(getContents.mOffset,0)
            verify(mockToffeeApi).getHistoryContents(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,10)
            })
        }

    }
}