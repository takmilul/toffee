package com.foxpress.toffeekotlin.usecase

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.response.ContentResponse
import com.banglalink.toffee.data.network.response.RelativeContentResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ContentBean
import com.banglalink.toffee.usecase.GetContents
import com.banglalink.toffee.usecase.GetRelativeContents
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Response


class GetRelativeContentsTest :BaseUseCaseTest(){

    @Test
    fun get_relative_contents_filter_success(){

        runBlocking {
            //set up test
            setupPref()
            //following item will be filtered
            val channelInfo = ChannelInfo().apply {
                id="1729"
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
                video_tags="tag"
            }

            val channelInfoList = mutableListOf<ChannelInfo>()
            channelInfoList.add(ChannelInfo().apply {
                id="1729"
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
            })

            val getContents = GetRelativeContents(Preference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.getRelativeContents(any<RelativeContentRequest>())).thenReturn(
                Response.success(RelativeContentResponse(
                    ContentBean(channelInfoList,1,1)
                )))

            //test method
            val resultChannelInfoList = getContents.execute(channelInfo)
            //verify it
            assertEquals(resultChannelInfoList.size,0)
        }

    }

    @Test
    fun get_relative_contents_filter_success_2(){

        runBlocking {
            //set up test
            setupPref()
            //following item will be filtered
            val channelInfo = ChannelInfo().apply {
                id="1729"
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
                video_tags="tag"
            }

            val channelInfoList = mutableListOf<ChannelInfo>()
            channelInfoList.add(ChannelInfo().apply {
                id="1730"
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
            })
            channelInfoList.add(ChannelInfo().apply {
                id="1739"
                program_name="Hello BD2"
                content_provider_name = "GSeries2"
                duration = "00:04:05"
                view_count = "1009"
            })

            val getContents = GetRelativeContents(Preference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.getRelativeContents(any<RelativeContentRequest>())).thenReturn(
                Response.success(RelativeContentResponse(
                    ContentBean(channelInfoList,2,2)
                )))

            //test method
            val resultChannelInfoList = getContents.execute(channelInfo)
            //verify it
            assertEquals(resultChannelInfoList.size,1)
            assertEquals(resultChannelInfoList[0].id,"1739")
            assertEquals(resultChannelInfoList[0].formatted_view_count,"1K")
            assertEquals(getContents.mOffset,2)
        }

    }

    @Test
    fun get_contents_success_when_response_null(){

        runBlocking {
            //set up test
            setupPref()

            val channelInfo = ChannelInfo().apply {
                id="1729"
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
                video_tags="tag"
            }

            val getContents = GetRelativeContents(Preference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.getRelativeContents(any<RelativeContentRequest>())).thenReturn(
                Response.success(RelativeContentResponse(
                    ContentBean(null,0,10)
                )))

            //test method
            val resultChannelInfoList = getContents.execute(channelInfo)
            //verify it
            assertEquals(resultChannelInfoList.size,0)
            verify(mockToffeeApi).getRelativeContents(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,10)

            })
        }

    }
}