package com.banglalink.toffee.ui.channels

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class ChannelFragmentFractory(private val args: Bundle?): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            ChannelFragment::class.java.name -> {
                if(args == null) {
                    ChannelFragment.createInstance("", showSelected = true)
                } else {
                    ChannelFragment.createInstance(
                        args.getInt("sub-category-id", 0),
                        args.getString("sub-category", ""),
                        args.getString("category", ""),
                        args.getBoolean("show_selected", false)
                    )
                }
            }
            else -> super.instantiate(classLoader, className)
        }
    }
}