package com.banglalink.toffee.usecase

import com.banglalink.toffee.apiservice.CatchupParams
import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.response.RelativeContentResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ContentBean
import com.banglalink.toffee.apiservice.GetRelativeContents
import com.banglalink.toffee.data.database.LocalSync
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Response


class GetRelativeContentsTest :BaseUseCaseTest(){

    @Mock
    val localSync: LocalSync = mock()

    @Mock
    val param: CatchupParams = mock()

    @Test
    fun get_relative_contents_filter_success(){

        runBlocking {
            //set up test
            setupPref()
            //following item will be filtered
            val channelInfo = ChannelInfo(
                id="1729",
                program_name="Hello BD",
                content_provider_name = "GSeries",
                duration = "00:04:05",
                view_count = "1000000000009",
                video_tags="tag",
            )

            val channelInfoList = mutableListOf<ChannelInfo>()
            channelInfoList.add(ChannelInfo(
                id="1729",
                program_name="Hello BD",
                content_provider_name = "GSeries",
                duration = "00:04:05",
                view_count = "1000000000009"
            ))

            val getContents = GetRelativeContents(
                SessionPreference.getInstance(),
                mockToffeeApi,
                localSync,
                param)
            Mockito.`when`(mockToffeeApi.getRelativeContents(any<RelativeContentRequest>())).thenReturn(
                Response.success(RelativeContentResponse(
                    ContentBean(channelInfoList,1,1)
                )).body())

            //test method
            val resultChannelInfoList = getContents.loadData(0, 0)
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
            val channelInfo = ChannelInfo(
                id="1729",
                program_name="Hello BD",
                content_provider_name = "GSeries",
                duration = "00:04:05",
                view_count = "1000000000009",
                video_tags="tag"
            )

            val channelInfoList = mutableListOf<ChannelInfo>()
            channelInfoList.add(ChannelInfo(
                id="1730",
                program_name="Hello BD",
                content_provider_name = "GSeries",
                duration = "00:04:05",
                view_count = "1000000000009",
            ))
            channelInfoList.add(ChannelInfo(
                id="1739",
                program_name="Hello BD2",
                content_provider_name = "GSeries2",
                duration = "00:04:05",
                view_count = "1009"
            ))

            val getContents = GetRelativeContents(SessionPreference.getInstance(),mockToffeeApi,localSync, param)
            Mockito.`when`(mockToffeeApi.getRelativeContents(any<RelativeContentRequest>())).thenReturn(
                Response.success(RelativeContentResponse(
                    ContentBean(channelInfoList,2,2)
                )).body())

            //test method
            val resultChannelInfoList = getContents.loadData(0, 0)
            //verify it
            assertEquals(resultChannelInfoList.size,1)
            assertEquals(resultChannelInfoList[0].id,"1739")
            assertEquals(resultChannelInfoList[0].formattedViewCount(),"1K")
//            assertEquals(getContents,2)
        }

    }

    @Test
    fun get_contents_success_when_response_null(){

        runBlocking {
            //set up test
            setupPref()

            val channelInfo = ChannelInfo(
                id="1729",
                program_name="Hello BD",
                content_provider_name = "GSeries",
                duration = "00:04:05",
                view_count = "1000000000009",
                video_tags="tag"
            )

            val getContents = GetRelativeContents(SessionPreference.getInstance(),mockToffeeApi, localSync, param)
            Mockito.`when`(mockToffeeApi.getRelativeContents(any<RelativeContentRequest>())).thenReturn(
                Response.success(RelativeContentResponse(
                    ContentBean(null,0,10)
                )).body())

            //test method
            val resultChannelInfoList = getContents.loadData(0, 0)
            //verify it
            assertEquals(resultChannelInfoList.size,0)
            verify(mockToffeeApi).getRelativeContents(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,10)

            })
        }

    }
}