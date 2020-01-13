package com.foxpress.toffeekotlin.usecase

import com.banglalink.toffee.data.network.request.FavoriteRequest
import com.banglalink.toffee.data.network.response.FavoriteResponse
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.UpdateFavorite
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response


class UpdateFavoriteTest :BaseUseCaseTest(){

    @Test
    fun get_contents_success_when_favorite_null(){

        runBlocking {
            //set up test
            setupPref()

            val updateFavorite = UpdateFavorite(Preference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.updateFavorite(any<FavoriteRequest>())).thenReturn(
                Response.success(FavoriteResponse(
                )))

            //test method
            val channelInfo = ChannelInfo().apply {
                id="1729"
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
            }

            val favorite= channelInfo.favorite == null || channelInfo.favorite == "0"
            val resultChannelInfo = updateFavorite.execute(channelInfo,favorite)
            //verify it
            assertEquals(resultChannelInfo.program_name,"Hello BD")
            assertEquals(resultChannelInfo.content_provider_name,"GSeries")
            verify(mockToffeeApi).updateFavorite(check {
                assertEquals(it.contentId,1729)
                assertEquals(it.isFavorite,1)
            })
        }

    }

    @Test
    fun get_contents_success_when_unfavorite(){

        runBlocking {
            //set up test
            setupPref()

            val updateFavorite = UpdateFavorite(Preference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.updateFavorite(any<FavoriteRequest>())).thenReturn(
                Response.success(FavoriteResponse(
                )))

            //test method
            val channelInfo = ChannelInfo().apply {
                id="1729"
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
                favorite = "1"
            }

            val favorite= channelInfo.favorite == null || channelInfo.favorite == "0"
            val resultChannelInfo = updateFavorite.execute(channelInfo,favorite)
            //verify it
            assertEquals(resultChannelInfo.program_name,"Hello BD")
            assertEquals(resultChannelInfo.content_provider_name,"GSeries")
            verify(mockToffeeApi).updateFavorite(check {
                assertEquals(it.contentId,1729)
                assertEquals(it.isFavorite,0)
            })
        }

    }

    @Test
    fun get_contents_success_when_favorite(){

        runBlocking {
            //set up test
            setupPref()

            val updateFavorite = UpdateFavorite(Preference.getInstance(),mockToffeeApi)
            Mockito.`when`(mockToffeeApi.updateFavorite(any<FavoriteRequest>())).thenReturn(
                Response.success(FavoriteResponse(
                )))

            //test method
            val channelInfo = ChannelInfo().apply {
                id="1729"
                program_name="Hello BD"
                content_provider_name = "GSeries"
                duration = "00:04:05"
                view_count = "1000000000009"
                favorite = "0"
            }

            val favorite= channelInfo.favorite == null || channelInfo.favorite == "0"
            val resultChannelInfo = updateFavorite.execute(channelInfo,favorite)
            //verify it
            assertEquals(resultChannelInfo.program_name,"Hello BD")
            assertEquals(resultChannelInfo.content_provider_name,"GSeries")
            verify(mockToffeeApi).updateFavorite(check {
                assertEquals(it.contentId,1729)
                assertEquals(it.isFavorite,1)
            })
        }

    }
}