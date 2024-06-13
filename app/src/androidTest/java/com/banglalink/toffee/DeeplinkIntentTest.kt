package com.banglalink.toffee

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.banglalink.toffee.ui.home.HomeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeeplinkIntentTest {

    @get:Rule
    val activityIntentRule = activityScenarioRule<HomeActivity>(
        Intent(Intent.ACTION_VIEW,
            Uri.parse("https://toffeelive.com?routing=internal&page=categories&catid=1")
        )
    )

    @Test
    fun deepLinkToUserChannel() {
        activityIntentRule.scenario
            .onActivity { activity->
//                on
            }
    }
}