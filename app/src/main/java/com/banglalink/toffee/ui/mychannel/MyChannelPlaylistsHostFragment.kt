package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R

class MyChannelPlaylistsHostFragment : Fragment() {
    
    private var channelOwnerId: Int = 0
    private var isMyChannel: Boolean = false
    private var navController: NavController? = null
    
    companion object {
        @JvmStatic
        fun newInstance(channelOwnerId: Int, isMyChannel: Boolean): MyChannelPlaylistsHostFragment {
            return MyChannelPlaylistsHostFragment().apply {
                arguments = bundleOf(
                    MyChannelPlaylistsFragment.CHANNEL_OWNER_ID to channelOwnerId,
                    MyChannelPlaylistsFragment.IS_MY_CHANNEL to isMyChannel
                )
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isMyChannel = it.getBoolean(MyChannelPlaylistsFragment.IS_MY_CHANNEL)
            channelOwnerId = it.getInt(MyChannelPlaylistsFragment.CHANNEL_OWNER_ID)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_channel_playlists_host, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment = childFragmentManager.findFragmentById(R.id.playlistNavHostFragment) as? NavHostFragment
        navController = navHostFragment?.navController
        navController?.setGraph(
            R.navigation.my_channel_playlist_navigation,
            bundleOf(
                MyChannelPlaylistsFragment.CHANNEL_OWNER_ID to channelOwnerId,
                MyChannelPlaylistsFragment.IS_MY_CHANNEL to isMyChannel
            )
        )
        listenBackStack()
    }
    
    private fun listenBackStack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentViewpagerPosition = (parentFragment as? MyChannelHomeFragment)?.getViewPagerPosition()
                if (navController?.currentDestination?.id == R.id.myChannelPlaylistVideosFragment && currentViewpagerPosition == 1) {
                    navController?.popBackStack()
                } else {
                    OnBackPressedCallback@ this.isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}