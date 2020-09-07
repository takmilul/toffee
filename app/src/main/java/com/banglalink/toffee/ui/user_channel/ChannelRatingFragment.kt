package com.banglalink.toffee.ui.user_channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentChannelRatingBinding
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.ui.points.AboutPointsFragment
import com.banglalink.toffee.ui.points.MyPointsFragment
import com.google.android.material.tabs.TabLayoutMediator

class ChannelRatingFragment : Fragment() {
    
    private lateinit var binding: FragmentChannelRatingBinding
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var fragmentList: ArrayList<Fragment> = arrayListOf()
    private var fragmentTitleList: ArrayList<String> = arrayListOf()
    /*private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(RedeemPointsViewModel::class.java)
    }*/
    
    companion object {
        
        fun createInstance(): ChannelRatingFragment {
            return ChannelRatingFragment()
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel_rating, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.viewPager.offscreenPageLimit = 1
        binding.channelRatingView.subscriptionButton.setSubscriptionInfo(false, "৳50")
        
        fragmentList.add(MyPointsFragment.createInstance())
        fragmentList.add(AboutPointsFragment.createInstance())
        
        fragmentTitleList.add("Videos")
        fragmentTitleList.add("Playlists")
        
        viewPagerAdapter = ViewPagerAdapter(this, fragmentList)
        binding.viewPager.adapter = viewPagerAdapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()
        
        // set interpolators for both expanding and collapsing animations
        binding.channelRatingView.channelDescriptionTextView.setInterpolator(OvershootInterpolator())
        
        // toggle the Expand button
        binding.channelRatingView.expandButton.setOnClickListener(View.OnClickListener {
            binding.channelRatingView.expandButton.background =
                if (binding.channelRatingView.channelDescriptionTextView.isExpanded) ContextCompat.getDrawable(context !!, R.drawable.ic_down_arrow) else ContextCompat.getDrawable(context !!, R.drawable.ic_up_arrow)
            binding.channelRatingView.channelDescriptionTextView.toggle()
        })
    }
}