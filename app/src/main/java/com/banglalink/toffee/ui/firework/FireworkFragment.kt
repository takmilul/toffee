package com.banglalink.toffee.ui.firework

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentFireworkBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.widget.FireworkCardView

class FireworkFragment : Fragment() {
    private var _binding: FragmentFireworkBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<FireworkViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFireworkBinding.inflate(inflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFirework()
        viewModel.getFireworks()

    }

    private fun observeFirework() {
        observe(viewModel.fireworkResults) {

            when (it) {
                is Resource.Success -> {
                    val data = it.data.response
                    data.fireworkModels?.forEach {
                        if (!it.playlistName.isNullOrBlank()
                            && !it.playlistId.isNullOrBlank()
                            && !it.channelId.isNullOrBlank()
                            && it.isEnabled == 1
                        ) {
                            binding.fireworkContainer.addView(FireworkCardView(requireContext()).apply {
                                setConfiguration(
                                    it.playlistName!!,
                                    it.channelId!!,
                                    it.playlistId!!
                                )
                            })
                        }

                    }

                }
                is Resource.Failure -> {
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