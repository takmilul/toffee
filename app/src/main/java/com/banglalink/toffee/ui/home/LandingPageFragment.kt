package com.banglalink.toffee.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentLandingPage2Binding
import com.banglalink.toffee.enums.PageType.Landing
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
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
    @Inject @ApplicationContext lateinit var appContext: Context
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    private var _binding: FragmentLandingPage2Binding ? = null
    private val binding get() = _binding!!

    companion object {
        private const val CLIENT_ID = "9e320da50f69212461fd9528a6b3e6f6758537187097720fe71cf0b3f867415d"
        
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mPref.isFireworkActive == "true" && !mPref.isFireworkInitialized) {
            try {
                FwSDK.initialize(appContext, CLIENT_ID, null, this)
            }
            catch (e: Exception) {
                mPref.isFireworkInitialized = false
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

    override fun onDestroyView() {
        binding.landingAppbar.removeOnOffsetChangedListener(offsetListener)
        super.onDestroyView()
        _binding = null
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
    }
    
    override fun currentStatus(status: SdkStatus, extra: String) {
        when(status){
            SdkStatus.Initialized -> {
                mPref.isFireworkInitialized = true
                binding.fireworkFragment.show()
                Log.d("FwSDK", "Initialized: $extra")
            }
            SdkStatus.InitializationFailed -> mPref.isFireworkInitialized = false
            SdkStatus.LoadingContent -> Log.d("FwSDK", "LoadingContent: $extra")
            SdkStatus.ContentLoaded -> {
                if (extra.toInt() <= 0 && _binding != null) {
                    binding.fireworkFragment.hide()
                }
                Log.d("FwSDK", "ContentLoaded: $extra")
            }
            SdkStatus.LoadingContentFailed -> {
                _binding?.let { 
                    binding.fireworkFragment.hide()
                }
                Log.d("FwSDK", "LoadingContentFailed: $extra")
            }
        }
    }
    
    fun onBackPressed(): Boolean {
        return false
    }
}