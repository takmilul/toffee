package com.banglalink.toffee.ugc.viewmodels

import androidx.paging.PagingData
import com.banglalink.toffee.MainCoroutineRule
import com.banglalink.toffee.apiservice.GetFavoriteContents
import com.banglalink.toffee.getDummyChannelList
import com.banglalink.toffee.ui.favorite.FavoriteViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.AllOf
import org.hamcrest.core.Is
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FavoriteViewModelTest {
    private lateinit var viewmodel: FavoriteViewModel
    private lateinit var mockFavoriteApi: GetFavoriteContents
    private val channelList = getDummyChannelList()

    @ExperimentalCoroutinesApi
    @get:Rule val mainCoRule = MainCoroutineRule()
//    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        mockFavoriteApi = mockk()
        coEvery { mockFavoriteApi.loadData(0, any()) } returns channelList.subList(0, 10)
        coEvery { mockFavoriteApi.loadData(10, any()) } returns channelList.subList(10, channelList.size)
        coEvery { mockFavoriteApi.loadData(20, any()) } returns emptyList()
        viewmodel = FavoriteViewModel(mockFavoriteApi)
    }

    @Test
    fun getRepo() = runBlocking {
        assertThat(true, Is.`is`(viewmodel.enableToolbar))
        val apiResp = mockFavoriteApi.loadData(0, 10)
        assertThat(apiResp.size, IsEqual.equalTo(10))
        val repoList = viewmodel.getListData.first()
        assertThat(repoList, AllOf.allOf(Is.`is`(IsInstanceOf.instanceOf(PagingData::class.java))))
    }
}