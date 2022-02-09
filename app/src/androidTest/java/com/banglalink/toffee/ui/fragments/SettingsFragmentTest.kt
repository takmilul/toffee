package com.banglalink.toffee.ui.fragments

import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.TestSessionPreference
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SettingsFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    @TestSessionPreference
    lateinit var mPref: SessionPreference

    @Before
    fun setup() {
        hiltRule.inject()

//        every { mPref.getDBVersionByApiName(any()) } returns 5
    }

    @Test
    fun getSettings() {
        println(mPref)
        Assert.assertEquals(5, mPref.getDBVersionByApiName("test_db"))
    }
}