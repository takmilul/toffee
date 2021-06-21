package com.banglalink.toffee.ugc.viewmodels

import com.banglalink.toffee.MainCoroutineRule
import com.banglalink.toffee.apiservice.GetFavoriteContents
import com.banglalink.toffee.dummyChannelList
import com.banglalink.toffee.ui.favorite.FavoriteViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FavoriteViewModelTest {
    private lateinit var viewmodel: FavoriteViewModel
    private lateinit var mockFavoriteApi: GetFavoriteContents

    @get:Rule val mainCoRule = MainCoroutineRule()

    @Before
    fun setup() {
        mockFavoriteApi = mockk()
        coEvery { mockFavoriteApi.loadData(0, any()) } returns dummyChannelList.subList(0, 10)
        coEvery { mockFavoriteApi.loadData(10, any()) } returns dummyChannelList.subList(10, dummyChannelList.size)
        coEvery { mockFavoriteApi.loadData(20, any()) } returns emptyList()
        viewmodel = FavoriteViewModel(mockFavoriteApi)
    }

    @Test
    fun getRepo() = runBlocking {
        assertTrue(viewmodel.enableToolbar)
        val apiResp = mockFavoriteApi.loadData(0, 10)
        assertEquals(10, apiResp.size)
        val repoList = viewmodel.getListData.first()
        print(repoList)
//        val repoList = viewmodel.getListData.collect {
//            print(it)
//        }
    }
}