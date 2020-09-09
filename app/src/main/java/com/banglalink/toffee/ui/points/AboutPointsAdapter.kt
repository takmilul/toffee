package com.banglalink.toffee.ui.points

import com.banglalink.toffee.R
import com.banglalink.toffee.model.AboutPoints
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter

class AboutPointsAdapter() : MyBaseAdapter<AboutPoints>(){
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_points
    }
}