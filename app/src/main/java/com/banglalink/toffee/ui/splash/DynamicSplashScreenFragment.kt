package com.banglalink.toffee.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType.CENTER_CROP
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import coil.load
import com.banglalink.toffee.databinding.FragmentDynamicSplashScreenBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class DynamicSplashScreenFragment : BaseFragment() {
    
    private val binding get() = _binding!!
    private var _binding: FragmentDynamicSplashScreenBinding? = null
    
    companion object {
        @JvmStatic
        fun newInstance() = DynamicSplashScreenFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDynamicSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSplashConfigData()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }
    
    private fun observeSplashConfigData() {
        observe(mPref.splashConfigLiveData) {
            var waitDuration = 0L
            it?.forEach {
                val isActive = try {
                    it.isActive == 1 && Utils.getDate(it.startDate).before(mPref.getSystemTime()) && Utils.getDate(it.endDate).after(mPref.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                if (isActive) {
                    waitDuration = it.duration * 1_000L
                    if (it.type == "png") {
                        binding.splashLogoImageView.load(it.imagePathLight) {
                            binding.splashLogoImageView.scaleType = CENTER_CROP
                            size(min(360.px, 720), min(202.px, 405))
                        }
                        binding.splashLogoImageView.show()
                    } else {
                        binding.splashGifImageView.load(it.imagePathLight) {
                            binding.splashGifImageView.scaleType = CENTER_CROP
                            size(min(200.px, 200))
                        }
                        binding.splashGifImageView.show()
                    }
                }
            }
            launchHomePage(waitDuration)
        }
    }
    
    private fun launchHomePage(waitDuration: Long) {
        lifecycleScope.launch {
            delay(waitDuration)
            requireActivity().launchActivity<HomeActivity> {
                data = requireActivity().intent.data
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            requireActivity().finishAffinity()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}