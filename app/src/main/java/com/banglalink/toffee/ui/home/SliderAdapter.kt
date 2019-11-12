package com.banglalink.toffee.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import coil.api.load
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.player.ChannelInfo

class SliderAdapter(val context: Context, private val channelList: List<ChannelInfo>):PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val sliderView = layoutInflater.inflate(R.layout.slider_content, container, false)
        val sliderImageView = sliderView.findViewById<ImageView>(R.id.slider_image)
        sliderImageView.load(channelList[position].landscape_ratio_1280_720){
            size(720,405)
        }
        container.addView(sliderView)
        return sliderView
    }
    override fun getCount(): Int {
       return channelList.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val view = obj as View
        container.removeView(view)
    }
}