package com.banglalink.toffee.ui.trendingchannels

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.R

class TrendingChannelsFragment : Fragment() {

    companion object {
        fun newInstance() = TrendingChannelsFragment()
    }

    private lateinit var viewModel: TrendingChannelsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_trending_channels, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TrendingChannelsViewModel::class.java)
        
    }

}