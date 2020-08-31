package com.banglalink.toffee.ui.points

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentMyPointsBinding
import com.banglalink.toffee.util.unsafeLazy

class MyPointsFragment : Fragment() {
    
    lateinit var binding: FragmentMyPointsBinding
    
    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(MyPointsViewModel::class.java)
    }
    
    companion object {
        fun createInstance() = MyPointsFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_points, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.redeemButton.setOnClickListener {
            fragmentManager?.beginTransaction()
                ?.replace(R.id.content_viewer, RedeemPointsFragment.createInstance())
                ?.addToBackStack(RedeemPointsFragment::class.java.name)
                ?.commit()
        }
        
        
    }
}