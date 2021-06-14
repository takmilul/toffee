package com.banglalink.toffee.usecase

import com.banglalink.toffee.apiservice.SearchContentService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.SearchContentRequest
import com.banglalink.toffee.data.network.response.SearchContentResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ContentBean
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Response


class SearchContentTest :BaseUseCaseTest(){
    @Mock
    val localSync: LocalSync = mock()

    @Test
    fun get_contents_success(){

        runBlocking {
            //set up test
            setupPref()
            val channelInfoList = mutableListOf<ChannelInfo>()
            channelInfoList.add(ChannelInfo(
                id = "1",
                program_name="Hello BD",
                content_provider_name = "GSeries",
                duration = "00:04:05",
                view_count = "1000000000009",
            ))

            val getContents = SearchContentService(SessionPreference.getInstance(),mockToffeeApi, localSync, "search")
            Mockito.`when`(mockToffeeApi.searchContent(any<SearchContentRequest>())).thenReturn(
                Response.success(SearchContentResponse(
                    ContentBean(channelInfoList,1,1)
                )).body())

            //test method
            val resultChannelInfoList = getContents.loadData(0, 0)
            //verify it
            assertEquals(resultChannelInfoList[0].formattedViewCount(),"1T")
            assertEquals(resultChannelInfoList[0].formattedDuration(),"04:05")
            assertEquals(resultChannelInfoList[0].program_name,"Hello BD")
            assertEquals(resultChannelInfoList[0].content_provider_name,"GSeries")
//            assertEquals(getContents.off,1)
            verify(mockToffeeApi).searchContent(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,30)
                assertEquals(it.keyword,"search")

            })
        }

    }

    @Test
    fun get_contents_success_when_response_null(){

        runBlocking {
            //set up test
            setupPref()

            val getContents = SearchContentService(SessionPreference.getInstance(), mockToffeeApi, localSync, "search")
            Mockito.`when`(mockToffeeApi.searchContent(any<SearchContentRequest>())).thenReturn(
                Response.success(SearchContentResponse(
                    ContentBean(null,0,0)
                )).body())

            //test method
            val resultChannelInfoList = getContents.loadData(0, 0)
            //verify it
            assertEquals(resultChannelInfoList.size,0)
            assertEquals(getContents,0)
            verify(mockToffeeApi).searchContent(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,30)
                assertEquals(it.keyword,"search")
            })
        }

    }
}