package com.banglalink.toffee.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType.CENTER_CROP
import androidx.lifecycle.lifecycleScope
import coil.load
import com.banglalink.toffee.databinding.FragmentDynamicSplashScreenBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class DynamicSplashScreenFragment : BaseFragment() {
    
    private val binding get() = _binding!!
    @Inject lateinit var bindingUtil: BindingUtil
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
        
//        val displayMetrics = resources.displayMetrics
//        val height = displayMetrics.heightPixels
//        val width = displayMetrics.widthPixels
//        
//        binding.splashGifImageView.layoutParams.height = height / 2
//        binding.splashGifImageView.layoutParams.width = width / 2
//        
//        binding.splashLogoImageView.layoutParams.height = height / 2
//        binding.splashLogoImageView.layoutParams.width = width / 2
        observeSplashConfigData()
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
                        binding.splashLogoImageView.load(it.imagePath) {
                            binding.splashLogoImageView.scaleType = CENTER_CROP
                            size(min(360.px, 720), min(202.px, 405))
                        }
                        binding.splashLogoImageView.show()
                    } else {
                        binding.splashGifImageView.load(it.imagePath) {
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
            requireActivity().launchActivity<HomeActivity>()
            requireActivity().finish()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}