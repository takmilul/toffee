package com.banglalink.toffee.usecase

import android.content.Context
import android.content.SharedPreferences
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.SessionPreference
import com.nhaarman.mockitokotlin2.mock
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito

open class BaseUseCaseTest {
    @Rule
    @JvmField
    var expectedException = ExpectedException.none()

    @Mock
    var mockPrefs: SharedPreferences = mock()
    @Mock
    var mockEditor: SharedPreferences.Editor = mock()
    @Mock
    var mockContext: Context = mock()

    @Mock
    val mockToffeeApi: ToffeeApi = mock()


    fun setupPref(){
        Mockito.`when`(
            mockContext.getSharedPreferences(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(mockPrefs)

        Mockito.`when`(
            mockPrefs.edit()
        ).thenReturn(mockEditor)

        Mockito.`when`(
            mockEditor.putString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        ).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.putInt(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenReturn(mockEditor)

        SessionPreference.init(mockContext)
    }
}