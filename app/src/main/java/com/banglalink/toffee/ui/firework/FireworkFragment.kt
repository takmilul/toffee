package com.banglalink.toffee.ui.firework

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.banglalink.toffee.databinding.FragmentFireworkBinding
import com.banglalink.toffee.extension.px
import kotlin.math.ceil
import kotlin.math.roundToInt

class FireworkFragment : Fragment() {
    private var _binding: FragmentFireworkBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val visibleItemCount = 4.5
        
        @JvmStatic
        fun newInstance() = FireworkFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFireworkBinding.inflate(inflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spaceBeforeItems = 8.px * ceil(visibleItemCount)
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val paddingHorizontal = binding.feedFrameView.paddingLeft + binding.feedFrameView.paddingRight
        val calculatedWidth = (screenWidth - paddingHorizontal - spaceBeforeItems) / visibleItemCount
        val calculatedHeight = ((calculatedWidth / 9) * 16).roundToInt()  // video item ratio -> 9:16
        binding.feedFrameView.layoutParams.height = calculatedHeight
    }
    
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}