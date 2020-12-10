package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.common.BaseFragment

class MoviesThrillerFragment: BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = MoviesThrillerFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_horizontal_content_small, container, false)
    }
}