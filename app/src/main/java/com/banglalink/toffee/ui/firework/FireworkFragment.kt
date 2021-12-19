package com.banglalink.toffee.ui.firework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentFireworkBinding
import com.banglalink.toffee.ui.widget.FireworkCardView

class FireworkFragment : Fragment() {
    private var _binding: FragmentFireworkBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFireworkBinding.inflate(inflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fireworkContainer.addView(FireworkCardView(requireContext()).apply {
            setConfiguration(
                getString(R.string.fireworks_section_title),
                getString(R.string.firework_channel_id),
                getString(R.string.firework_playlist_id)
            )
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }
    
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}