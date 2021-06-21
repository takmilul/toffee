package com.banglalink.toffee.ui.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import com.banglalink.toffee.ui.landing.PopularTVChannelsFragment
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PopularTvChannelsTest {

    @Test
    fun testFragment() {
        val scenario = launchFragmentInContainer<PopularTVChannelsFragment>()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { frag ->
            print(frag)
        }
    }
}