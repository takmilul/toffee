package com.banglalink.toffee.ui.fragments

import com.banglalink.toffee.CustomTestRunner
import com.banglalink.toffee.data.storage.SessionPreference
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
    lateinit var mPref: SessionPreference

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun getSettings() {
        println(mPref)
//        Assert.assertEquals(false, true)
    }
}