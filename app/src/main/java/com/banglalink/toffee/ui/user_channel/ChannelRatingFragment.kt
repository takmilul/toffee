package com.banglalink.toffee.ui.user_channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R

class ChannelRatingFragment : Fragment() {
    
    companion object {
        
        fun createInstance(): ChannelRatingFragment {
            val instance = ChannelRatingFragment()
            return instance
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_channel_rating, container, false)
    }
}