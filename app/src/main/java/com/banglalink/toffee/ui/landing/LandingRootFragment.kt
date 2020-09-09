package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.home.LandingPageFragment

class LandingRootFragment: Fragment() {

    companion object {
        fun newInstance(): LandingRootFragment {
            return LandingRootFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_container_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragmentManager.beginTransaction()
            .replace(R.id.landing_content_viewer, LandingPageFragment.newInstance(), LandingPageFragment::class.java.simpleName)
//            .addToBackStack(LandingPageFragment::class.java.simpleName)
            .commit()
    }
}