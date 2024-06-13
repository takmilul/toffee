package com.banglalink.toffee.ui.firework

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.databinding.FragmentFireworkBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.FireworkCardView
import com.firework.common.feed.FeedLayout
import com.firework.common.feed.FeedResource
import com.firework.videofeed.FwLifecycleAwareVideoFeedView
import com.firework.viewoptions.baseOptions
import com.firework.viewoptions.layoutOptions
import com.firework.viewoptions.viewOptions

class FireworkFragment : Fragment() {
    
    private var _binding: FragmentFireworkBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val viewModel by activityViewModels<FireworkViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFireworkBinding.inflate(inflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe(homeViewModel.isFireworkActive) {
            if (it) {
                observeFirework()
                viewModel.getFireworks()
            }
        }
    }
    
    private fun observeFirework() {
        observe(viewModel.fireworkResults) { response ->
            when (response) {
                is Resource.Success -> {
                    val data = response.data.response
                    binding.fireworkContainer.removeAllViews()
                    data?.fireworkModels?.forEach {
                        if (!it.playlistName.isNullOrBlank() && !it.playlistId.isNullOrBlank() && !it.channelId.isNullOrBlank() && it.isActive) {
                            val viewOption = viewOptions {
                                baseOptions {
                                    feedResource(FeedResource.Playlist(it.channelId!!, it.playlistId!!))
                                }
                                layoutOptions {
                                    feedLayout(
                                        FeedLayout.HORIZONTAL
                                    )
                                }
                            }
                            val view = FireworkCardView(requireContext())
                            val titleView = view.findViewById(R.id.fireworkHeader) as TextView
                            val feedView = view.findViewById(R.id.videoFeedView) as FwLifecycleAwareVideoFeedView
                            titleView.text = it.playlistName
                            feedView.init(viewOption, lifecycle)
                            binding.fireworkContainer.addView(view)
                        }
                    }
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.GET_FIREWORK_LIST,
                            FirebaseParams.BROWSER_SCREEN to "Firework Screen",
                            "error_code" to response.error.code,
                            "error_description" to response.error.msg
                        )
                    )
                    Log.d("_fire", response.error.msg)
                }
            }
        }
    }
    
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}