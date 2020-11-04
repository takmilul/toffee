package com.banglalink.toffee.ui.common

import androidx.fragment.app.Fragment
import com.banglalink.toffee.data.storage.Preference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment: Fragment() {
    @Inject lateinit var mPref: Preference
}