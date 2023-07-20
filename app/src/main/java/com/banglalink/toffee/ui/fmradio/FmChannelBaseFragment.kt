package com.banglalink.toffee.ui.fmradio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.databinding.FragmentFmChannelsBaseBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FmChannelBaseFragment : HomeBaseFragment() {
    
    @Inject lateinit var bindingUtil: BindingUtil
    private var _binding: FragmentFmChannelsBaseBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFmChannelsBaseBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Updating banner Img
        observe(mPref.radioBannerImgUrl) {
            bindingUtil.bindImageFromUrl(binding.packBannerImageView, it)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}