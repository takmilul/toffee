package com.banglalink.toffee.ui.fragments

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.launchFragmentInHiltContainer
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.landing.PopularTVChannelsFragment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.core.AllOf
import org.hamcrest.core.Is
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PopularTvChannelsTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testFragment() {
        launchFragmentInHiltContainer<PopularTVChannelsFragment>()
        onView(withId(R.id.channel_tv)).check(matches(isDisplayed()))
//        onView(withId(R.id.channel_list)).check()
//        onData(AllOf.allOf(Is.`is`(IsInstanceOf.instanceOf(ChannelInfo::class.java))))
    }
}