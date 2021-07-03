package com.banglalink.toffee.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentLandingPage2Binding
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.google.android.material.appbar.AppBarLayout
import com.loopnow.fireworklibrary.FwSDK
import com.loopnow.fireworklibrary.SdkStatus
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@AndroidEntryPoint
class LandingPageFragment : HomeBaseFragment(), FwSDK.SdkStatusListener {
    private var appbarOffset = 0
    private var _binding: FragmentLandingPage2Binding ? = null
    @Inject @ApplicationContext lateinit var appContext: Context
    private val binding get() = _binding!!
    private val landingViewModel by activityViewModels<LandingPageViewModel>()

    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mPref.isFireworkActive == "true" && homeViewModel.isFireworkInitialized.value != true) {
            try {
                FwSDK.initialize(appContext, getString(R.string.firework_oauth_id), mPref.getFireworkUserId(), this)
            }
            catch (e: Exception) {
                Log.e("FwSDK", "onCreate: ${e.message}")
            }
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingPage2Binding.inflate(inflater, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {
                    if (appbarOffset != 0) {
                        binding.latestVideoScroller.smoothScrollTo(0, 0, 0)
                        binding.landingAppbar.setExpanded(true, true)
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        })
        return binding.root
    }

    private val offsetListener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
        appbarOffset = verticalOffset
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.app_name_short)
        landingViewModel.categoryId.value = 0
        landingViewModel.pageType.value = Landing
        landingViewModel.isDramaSeries.value = false
        binding.landingAppbar.addOnOffsetChangedListener(offsetListener)
        observe(homeViewModel.isFireworkInitialized) {
            _binding?.fireworkFragment?.isVisible = it
        }
    }
    
    override fun currentStatus(status: SdkStatus, extra: String) {
        when(status){
            SdkStatus.Initialized -> {
                Log.e("FwSDK", "Initialized: $extra")
                FwSDK.setBasePlayerUrl("https://toffeelive.com/")
                homeViewModel.isFireworkInitialized.postValue(true)
                try {
                    val url = requireActivity().intent.data?.fragment?.removePrefix("fwplayer=")
                    url?.let {
                        FwSDK.play(it)
                    }
                }
                catch (e: Exception) {
                    Log.e("FwSDK", "FireworkDeeplinkPlayException")
                }
            }
            SdkStatus.InitializationFailed -> {
                Log.e("FwSDK", "InitializationFailed: $extra")
                homeViewModel.isFireworkInitialized.postValue(false)
            }
            SdkStatus.LoadingContent -> Log.e("FwSDK", "LoadingContent: $extra")
            SdkStatus.ContentLoaded -> Log.e("FwSDK", "ContentLoaded: $extra")
            SdkStatus.LoadingContentFailed -> Log.e("FwSDK", "LoadingContentFailed: $extra")
        }
    }
    
    override fun onDestroyView() {
        binding.landingAppbar.removeOnOffsetChangedListener(offsetListener)
        super.onDestroyView()
        _binding = null
    }
}