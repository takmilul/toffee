package com.banglalink.toffee.ui.firework

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.databinding.FragmentFireworkBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.FireworkCardView

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
        observe(viewModel.fireworkResults) {
            when (it) {
                is Resource.Success -> {
                    val data = it.data.response
                    data.fireworkModels?.forEach {
                        if (!it.playlistName.isNullOrBlank() && !it.playlistId.isNullOrBlank() && !it.channelId.isNullOrBlank() && it.isEnabled == 1) {
                            binding.fireworkContainer.addView(FireworkCardView(requireContext()).apply {
                                setConfiguration(it.playlistName!!, it.channelId!!, it.playlistId!!)
                            })
                        }
                    }
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.GET_FIREWORK_LIST,
                            "browser_screen" to "Firework Screen",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg
                        )
                    )
                    Log.d("_fire", it.error.msg)
                }
            }
        }
    }
    
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}