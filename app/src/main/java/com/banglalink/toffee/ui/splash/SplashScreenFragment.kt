package com.banglalink.toffee.ui.splash

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.ads.identifier.AdvertisingIdClient
import androidx.ads.identifier.AdvertisingIdInfo
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.banglalink.toffee.Constants
import com.banglalink.toffee.R
import com.banglalink.toffee.R.drawable
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.data.exception.AppDeprecatedError
import com.banglalink.toffee.data.exception.CustomerNotFoundError
import com.banglalink.toffee.data.exception.Error
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.databinding.FragmentSplashScreenBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.DecorationConfig
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.today
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures.addCallback
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.*
import javax.inject.Inject
import javax.net.ssl.SSLContext

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : BaseFragment() {
    private var isDynamicSplashActive: Boolean = false
    @Inject lateinit var commonPreference: CommonPreference
    private var _binding: FragmentSplashScreenBinding? = null
    @Inject lateinit var connectionWatcher: ConnectionWatcher
    @Inject lateinit var countDownloadService: DownloadService
    @Inject @ApplicationContext lateinit var appContext: Context
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<SplashViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.reportAppLaunch()
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        observeLoadingProgress()
        
        binding.splashScreenMotionLayout.onTransitionCompletedListener {
            when (it) {
                R.id.firstEnd -> {
                    onFirstTransitionCompletion()
                }
                R.id.secondEnd -> {
                    onSecondTransitionCompletion()
                }
            }
        }
        observe(mPref.viewCountDbUrlLiveData) {
            if (it.isNotEmpty()) {
                countDownloadService.populateViewCountDb(it)
            }
        }
        observe(mPref.reactionStatusDbUrlLiveData) {
            if (it.isNotEmpty()) {
                countDownloadService.populateReactionStatusDb(it)
            }
        }
        observe(mPref.subscriberStatusDbUrlLiveData) {
            if (it.isNotEmpty()) {
                countDownloadService.populateSubscriptionCountDb(it)
            }
        }
        observe(mPref.shareCountDbUrlLiveData) {
            if (it.isNotEmpty()) {
                countDownloadService.populateShareCountDb(it)
            }
        }
        sendAdIdLog()
        detectTlsVersion()
        observeHeaderEnrichment()
        requestHeaderEnrichment()
    }
    
    private fun observeLoadingProgress() {
        observe(viewModel.apiLoadingProgress) {
            runCatching {
                val progressValue = if (connectionWatcher.isOnline) it else 1
                ObjectAnimator.ofInt(_binding?.progressBar, "progress", progressValue).start()
            }
        }
    }
    
    private fun onFirstTransitionCompletion() {
        lifecycleScope.launch {
            delay(800)
            runCatching {
                with(binding.splashScreenMotionLayout) {
                    setTransition(R.id.firstEnd, R.id.secondEnd)
                    transitionToEnd()
                }
            }
        }
    }
    
    private fun onSecondTransitionCompletion() {
        binding.splashLogoImageView.load(drawable.ic_splash_logo_gif)
        lifecycleScope.launch {
            delay(100)
            _binding?.progressBar?.show()
            observeApiLogin()
            observeCheckForUpdateStatus()
            viewModel.checkForUpdateStatus(mPref.customerId == 0 || mPref.password.isBlank())
        }
    }
    
    private fun requestAppLaunch() {
        if (mPref.customerId == 0 || mPref.password.isBlank()) {
            viewModel.getCredential()
        } else {
            viewModel.getAppLaunchConfig()
        }
    }
    
    private fun observeCheckForUpdateStatus() {
        observe(viewModel.updateStatusLiveData) { it ->
            when (it) {
                is Success -> {
                    val data = it.data as? DecorationConfig
                    mPref.splashConfigLiveData.value = data?.splashScreen
                    data?.topBar?.getOrNull(0)?.let {
                        mPref.isTopBarActive = it.isActive == 1
                        mPref.topBarImagePathLight = it.imagePathLight
                        mPref.topBarImagePathDark = it.imagePathDark
                        mPref.topBarStartDate = it.startDate
                        mPref.topBarEndDate = it.endDate
                        mPref.topBarType = it.type
                    }
                    isDynamicSplashActive = data?.splashScreen?.any {
                        try { 
                            it.isActive == 1 && Utils.getDate(it.startDate).before(mPref.getSystemTime()) && Utils.getDate(it.endDate).after(mPref.getSystemTime())
                        } catch (e: Exception) {
                            ToffeeAnalytics.logException(e)
                            false
                        }
                    } ?: false
                    
                    requestAppLaunch()
                }
                is Failure -> {
                    onResponseFailure(it.error)
                }
            }
        }
    }
    
    private fun observeApiLogin() {
        observe(viewModel.appLaunchConfigLiveData) {
            when (it) {
                is Success -> {
                    viewModel.sendLoginLogData()
                    viewModel.sendDrmUnavailableLogData()
                    forwardToNextScreen()
                }
                is Failure -> {
                    onResponseFailure(it.error)
                }
            }
        }
    }
    
    private fun onResponseFailure(error: Error) {
        ToffeeAnalytics.logEvent(
            ToffeeEvents.EXCEPTION, bundleOf(
                "api_name" to ApiNames.API_LOGIN_V2,
                FirebaseParams.BROWSER_SCREEN to "Splash Screen",
                "error_code" to error.code,
                "error_description" to error.msg
            )
        )
        when (error) {
            is AppDeprecatedError -> {
                showUpdateDialog(error.title, error.updateMsg, error.forceUpdate)
            }
            is CustomerNotFoundError -> {
                mPref.clear()
                requestAppLaunch()
            }
            else -> {
                ToffeeAnalytics.logApiError("apiLoginV2", error.msg)
                if (error.code == Constants.ACCOUNT_DELETED_ERROR_CODE) {
                    mPref.clear()
                    requestAppLaunch()
                } else {
                    binding.root.snack(error.msg) {
                        action("Retry", ContextCompat.getColor(requireContext(), R.color.colorAccent2)) {
                            requestAppLaunch()
                        }
                    }
                }
            }
        }
    }
    
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun forwardToNextScreen() {
        ToffeeAnalytics.updateCustomerId(mPref.customerId)
        lifecycleScope.launch(Main) {
            delay(200)
            if (isDynamicSplashActive) {
                findNavController().navigate(R.id.dynamicSplashScreenFragment)
            } else {
                requireActivity().launchActivity<HomeActivity> {
                    data = requireActivity().intent.data
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                requireActivity().finishAffinity()
            }
        }
    }
    
    private fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(false)
            setPositiveButton("Update") { _, _ ->
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireActivity().packageName}")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${requireActivity().packageName}")
                    )
                    
                    if (intent.resolveActivity(requireContext().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        requireContext().showToast("Please open Play Store app and update Toffee")
                    }
                }
                requireActivity().finish()
            }
            if (!forceUpdate) {
                setNegativeButton("SKIP") { dialog, _ ->
                    dialog.dismiss()
                    requestAppLaunch()
                }
            }
        }
        val alert = builder.create()
        alert.show()
        val updateButton: Button = alert.getButton(DialogInterface.BUTTON_POSITIVE)
        updateButton.setTextColor(Color.parseColor("#FF3988"))
    }
    
    private fun sendAdIdLog() {
        if (mPref.adIdUpdateDate != today) {
            lifecycleScope.launch(IO + Job()) {
                runCatching {
                    var adId: String?
                    
                    if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(appContext)) {
                        val adIdInfoCallback = AdvertisingIdClient.getAdvertisingIdInfo(appContext)
                        addCallback(
                            adIdInfoCallback, object : FutureCallback<AdvertisingIdInfo> {
                                override fun onSuccess(adInfo: AdvertisingIdInfo?) {
                                    adId = adInfo?.id
                                    Log.i("AD_ID", "adId: $adId")
                                    adId?.takeUnless { it.startsWith("0000") }?.let {
                                        viewModel.sendAdvertisingIdLogData(AdvertisingIdLogData(adId).also {
                                            it.phoneNumber = mPref.phoneNumber
                                            it.isBlNumber = mPref.isBanglalinkNumber
                                        })
                                        mPref.adId = it
                                        mPref.adIdUpdateDate = today
                                    } ?: run {
                                        ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED)
                                    }
                                }
                                
                                override fun onFailure(t: Throwable) {
                                    Log.e("AD_ID", "Failed to connect to Advertising ID provider.")
                                    ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED)
                                }
                            }, Executors.newSingleThreadExecutor()
                        )
                    } else {
                        adId = com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(appContext).id
                        Log.i("AD_ID", "adId: $adId")
                        adId?.takeUnless { it.startsWith("0000") }?.let {
                            viewModel.sendAdvertisingIdLogData(AdvertisingIdLogData(adId).also {
                                it.phoneNumber = mPref.phoneNumber
                                it.isBlNumber = mPref.isBanglalinkNumber
                            })
                            mPref.adId = it
                            mPref.adIdUpdateDate = today
                        } ?: run {
                            ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED)
                        }
                    }
                }.onFailure {
                    ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED)
                }
            }
        }
    }
    
    private fun requestHeaderEnrichment() {
        runCatching {
            if (mPref.heUpdateDate != today && connectionWatcher.isOverCellular) {
                viewModel.getHeaderEnrichment()
            }
        }
    }
    
    private fun observeHeaderEnrichment() {
        observe(viewModel.headerEnrichmentLiveData) { response ->
            when (response) {
                is Success -> {
                    val data = response.data
                    mPref.heUpdateDate = today
                    if (data.isBanglalinkNumber && data.phoneNumber.isNotBlank()) {
                        mPref.latitude = data.lat ?: ""
                        mPref.longitude = data.lon ?: ""
                        mPref.userIp = data.userIp ?: ""
                        mPref.geoCity = data.geoCity ?: ""
                        mPref.geoLocation = data.geoLocation ?: ""
                        mPref.hePhoneNumber = data.phoneNumber
                        mPref.isHeBanglalinkNumber = data.isBanglalinkNumber
                        viewModel.sendHeLogData(HeaderEnrichmentLogData().also {
                            it.phoneNumber = mPref.hePhoneNumber
                            it.isBlNumber = mPref.isHeBanglalinkNumber.toString()
                        })
                    }
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION, bundleOf(
                            "api_name" to "HeaderEnrichment",
                            FirebaseParams.BROWSER_SCREEN to "Splash Screen",
                            "error_code" to response.error.code,
                            "error_description" to response.error.msg
                        )
                    )
                    mPref.hePhoneNumber = ""
                    mPref.isHeBanglalinkNumber = false
                }
            }
        }
    }
    
    private fun detectTlsVersion() {
        runCatching {
            val protocols = SSLContext.getDefault().defaultSSLParameters.protocols
            protocols?.let {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.SUPPORTED_TLS, bundleOf(
                        "TLS_version" to protocols.contentToString()
                    )
                )
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}