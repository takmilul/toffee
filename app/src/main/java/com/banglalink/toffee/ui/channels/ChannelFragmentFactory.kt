package com.banglalink.toffee.ui.channels

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class ChannelFragmentFactory(private val args: Bundle?): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            ChannelFragment::class.java.name -> {
                if(args == null) {
                    ChannelFragment.createInstance("", showSelected = true)
                } else {
                    ChannelFragment.createInstance(
                        args.getInt("sub_category_id", 0),
                        args.getString("sub_category", ""),
                        args.getString("category", ""),
                        args.getBoolean("show_selected", false),
                        args.getBoolean("is_stingray", false)
                    )
                }
            }
            RecentChannelsFragment::class.java.name -> {
                RecentChannelsFragment.newInstance(args?.getBoolean("show_selected", false) ?: true, args?.getBoolean("is_stingray", false) ?: false)
            }
            else -> super.instantiate(classLoader, className)
        }
    }
}