package com.banglalink.toffee.ui.points

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentAboutPointsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.unsafeLazy

class AboutPointsFragment : Fragment() {
    
    private val viewModel by viewModels<AboutPointsViewModel>()
    
    private var _binding: FragmentAboutPointsBinding ? = null
    private val binding get() = _binding!!
    
    companion object {
        fun createInstance() = AboutPointsFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAboutPointsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        val adapter = AboutPointsAdapter()
        binding.listView.adapter = adapter
        binding.listView.setHasFixedSize(true)
        
        observe(viewModel.setAboutPoints()!!){
            when(it){
                is Resource.Success ->{
                    adapter.addAll(it.data.aboutPoints)
                }
                is Resource.Failure->{
                    //showToast(it.error.msg)
                }
            }
        }
    }
    
} 