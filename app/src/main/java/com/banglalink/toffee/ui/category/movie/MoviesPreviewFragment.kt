package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.R

class MoviesPreviewFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = MoviesPreviewFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        return inflater.inflate(R.layout.fragment_movies_preview, container, false)
    }
}