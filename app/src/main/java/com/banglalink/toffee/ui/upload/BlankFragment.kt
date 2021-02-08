package com.banglalink.toffee.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.util.OnSwipeTouchListener
import com.banglalink.toffee.util.onSwipeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.upload_bottom_sheet.*

@AndroidEntryPoint
class BlankFragment: Fragment(), onSwipeListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.upload_bottom_sheet, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomUp: Animation = AnimationUtils.loadAnimation(context,
            R.anim.bottom_up)
    }

    override fun swipeRight() {

    }

    override fun swipeTop() {


    }
    var data:Int?=0
    override fun swipeBottom() {



    }

    override fun swipeLeft() {

    }


}