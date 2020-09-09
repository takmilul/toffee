package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentCreatorChannelEditBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.unsafeLazy

class CreatorChannelEditFragment : Fragment() {
    
    private lateinit var binding: FragmentCreatorChannelEditBinding
    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(CreatorChannelEditViewModel::class.java)
    }
    
    companion object {
        fun newInstance() = CreatorChannelEditFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_creator_channel_edit, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe(viewModel.getEditInfo()){
            when (it) {
                is Resource.Success -> {
                    Log.d("TAG", "onViewCreated: Item found")
                }
                is Resource.Failure -> {
                    context?.showToast(it.error.msg)
                }
            }
        }
    }
}