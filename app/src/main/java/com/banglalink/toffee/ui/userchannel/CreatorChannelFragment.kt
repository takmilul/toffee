package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentCreatorChannelBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.layout_creator_channel_info.*

class CreatorChannelFragment : Fragment(), OnClickListener {
    
    private lateinit var binding: FragmentCreatorChannelBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var fragmentList: ArrayList<Fragment> = arrayListOf()
    private var fragmentTitleList: ArrayList<String> = arrayListOf()
    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(CreatorChannelViewModel::class.java)
    }
    
    companion object {
        fun newInstance(): CreatorChannelFragment {
            return CreatorChannelFragment()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (fragmentList.isEmpty()) {
            
            fragmentTitleList.add("Videos")
            fragmentTitleList.add("Playlists")
            
            fragmentList.add(ChannelVideosFragment.newInstance(true))
            fragmentList.add(ChannelPlaylistsFragment.newInstance(true))
        }
        
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_creator_channel, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        observeData()
        viewModel.loadData()
        
        binding.viewPager.offscreenPageLimit = 1
        binding.creatorChannelView.addBioButton.setOnClickListener(this)
        binding.creatorChannelView.editButton.setOnClickListener(this)
        
        viewPagerAdapter = ViewPagerAdapter(this, fragmentList)
        binding.viewPager.adapter = viewPagerAdapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()
        
        // set interpolators for both expanding and collapsing animations
        binding.creatorChannelView.channelDescriptionTextView.setInterpolator(OvershootInterpolator())
        
        // toggle the Expand button
        binding.creatorChannelView.expandButton.setOnClickListener(View.OnClickListener {
            binding.creatorChannelView.expandButton.background =
                if (binding.creatorChannelView.channelDescriptionTextView.isExpanded) ContextCompat.getDrawable(requireContext(), R.drawable.ic_down_arrow) else ContextCompat.getDrawable(requireContext(), R.drawable.ic_up_arrow)
            binding.creatorChannelView.channelDescriptionTextView.toggle()
        })
    }
    
    private fun observeData() {
        observe(viewModel.liveData) {
            when (it) {
                is Resource.Success -> {
                    viewModel.channelInfo.postValue(it.data)
                }
                is Resource.Failure -> {
        
                }
            }
        }
    }
    
    override fun onClick(v: View?) {
        when (v) {
            addBioButton -> {
                val action = CreatorChannelFragmentDirections.actionMenuChannelToCreatorChannelEditFragment(viewModel.channelInfo.value)
                findNavController().navigate(action)
            }
            
            editButton -> {
                val action = CreatorChannelFragmentDirections.actionMenuChannelToCreatorChannelEditFragment(viewModel.channelInfo.value)
                findNavController().navigate(action)
            }
        }
    }
}