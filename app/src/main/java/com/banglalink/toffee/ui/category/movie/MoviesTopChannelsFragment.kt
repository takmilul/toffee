package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.R

class MoviesTopChannelsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = MoviesTopChannelsFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        return inflater.inflate(R.layout.layout_horizontal_content_container, container, false)
    }
}