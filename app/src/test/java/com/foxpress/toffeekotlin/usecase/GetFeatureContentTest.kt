package com.foxpress.toffeekotlin.usecase

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.response.ContentResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ContentBean
import com.banglalink.toffee.usecase.GetContents
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Response


class GetFeatureContentTest :BaseUseCaseTest(){

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

            val getContents = GetContents(mockToffeeApi)
            Mockito.`when`(mockToffeeApi.getContents(any<ContentRequest>())).thenReturn(
                Response.success(ContentResponse(
                    ContentBean(channelInfoList,1,1)
                )))

            //test method
            val resultChannelInfoList = getContents.execute("",0,"",0,"VOD")
            //verify it
            assertEquals(resultChannelInfoList[0].formatted_view_count,"1T")
            assertEquals(resultChannelInfoList[0].formattedDuration,"04:05")
            assertEquals(resultChannelInfoList[0].program_name,"Hello BD")
            assertEquals(resultChannelInfoList[0].content_provider_name,"GSeries")
            verify(mockToffeeApi).getContents(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,10)
                assertEquals(it.type,"VOD")
                assertEquals(it.categoryId,0)
                assertEquals(it.subCategoryId,0)

            })
        }

    }

    @Test
    fun get_contents_success_when_response_null(){

        runBlocking {
            //set up test
            setupPref()

            val getContents = GetContents(mockToffeeApi)
            Mockito.`when`(mockToffeeApi.getContents(any<ContentRequest>())).thenReturn(
                Response.success(ContentResponse(
                    ContentBean(null,0,10)
                )))

            //test method
            val resultChannelInfoList = getContents.execute("",0,"",0,"VOD")
            //verify it
            assertEquals(resultChannelInfoList.size,0)
            verify(mockToffeeApi).getContents(check {
                assertEquals(it.offset,0)
                assertEquals(it.limit,10)
                assertEquals(it.type,"VOD")
                assertEquals(it.categoryId,0)
                assertEquals(it.subCategoryId,0)

            })
        }

    }
}