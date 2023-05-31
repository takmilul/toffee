package com.banglalink.toffee.usecase

import com.banglalink.toffee.model.ChannelInfo
import kotlinx.coroutines.runBlocking
import org.junit.Test


class GetContentServiceTest :BaseUseCaseTest(){

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

//            val getContentService = GetContentService( mockToffeeApi, SessionPreference.getInstance(), mock())
//            Mockito.`when`(mockToffeeApi.getContents(any(), any(), any(), any(), any(), any(), any<ContentRequest>())).thenReturn(
//                Response.success(ContentResponse(
//                    ContentBean(channelInfoList,1,1)
//                )).body())
//
//            //test method
//            val resultChannelInfoList = getContentService.loadData(0, 0)
//            //verify it
//            assertEquals(resultChannelInfoList[0].formattedViewCount(),"1T")
//            assertEquals(resultChannelInfoList[0].formattedDuration(),"04:05")
//            assertEquals(resultChannelInfoList[0].program_name,"Hello BD")
//            assertEquals(resultChannelInfoList[0].content_provider_name,"GSeries")
//            verify(mockToffeeApi).getContents(any(), any(), any(), any(), any(), any(), check {
////                assertEquals(it.offset,0)
////                assertEquals(it.limit,10)
////                assertEquals(it.type,"VOD")
////                assertEquals(it.categoryId,0)
////                assertEquals(it.subCategoryId,0)
//                assertTrue(it != null)
//            })
        }

    }

    @Test
    fun get_contents_success_offset_check(){

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

//            val getContentService = GetContentService(SessionPreference.getInstance(), mockToffeeApi, mock())
//            Mockito.`when`(mockToffeeApi.getContents(any(), any(), any(), any(), any(), any(), any<ContentRequest>())).thenReturn(
//                Response.success(ContentResponse(
//                    ContentBean(channelInfoList,1,1)
//                )).body())
//
//            //test method
//            getContentService.loadData(0, 0)
//            getContentService.loadData(0, 0)
//            //verify that offset calculation is OK
////            assertEquals(getContents.mOffset,2)
        }

    }

    @Test
    fun get_contents_success_when_response_null(){

        runBlocking {
            //set up test
            setupPref()

//            val getContentService = GetContentService(SessionPreference.getInstance(), mockToffeeApi, mock())
//            Mockito.`when`(mockToffeeApi.getContents(any(), any(), any(), any(), any(), any(), any<ContentRequest>())).thenReturn(
//                Response.success(ContentResponse(
//                    ContentBean(null,0,10)
//                )).body())
//
//            //test method
//            val resultChannelInfoList = getContentService.loadData(0, 0)
//            //verify it
//            assertEquals(resultChannelInfoList.size,0)
//            verify(mockToffeeApi).getContents(any(), any(), any(), any(), any(), any(), check {
////                assertEquals(it.offset,0)
////                assertEquals(it.limit,10)
////                assertEquals(it.type,"VOD")
////                assertEquals(it.categoryId,0)
////                assertEquals(it.subCategoryId,0)
//
//            })
        }

    }
}