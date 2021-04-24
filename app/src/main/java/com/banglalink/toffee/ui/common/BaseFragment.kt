package com.banglalink.toffee.ui.common

import androidx.fragment.app.Fragment
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment: Fragment() {
    @Inject lateinit var cPref: CommonPreference
    @Inject lateinit var mPref: SessionPreference
}