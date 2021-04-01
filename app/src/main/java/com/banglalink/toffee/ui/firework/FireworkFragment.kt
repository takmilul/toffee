package com.banglalink.toffee.ui.firework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.banglalink.toffee.databinding.FragmentFireworkBinding

class FireworkFragment : Fragment() {
    private var _binding: FragmentFireworkBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        @JvmStatic
        fun newInstance() = FireworkFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFireworkBinding.inflate(inflater)
        return binding.root
    }
    
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}