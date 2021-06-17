package com.banglalink.toffee.ui.splash

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.databinding.FragmentSplashScreen2Binding
import com.banglalink.toffee.exception.AppDeprecatedError
import com.banglalink.toffee.exception.CustomerNotFoundError
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifDrawable
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenFragment2 : BaseFragment() {
    private var logoGifDrawable: GifDrawable? = null
    private var isOperationCompleted: Boolean = false
    @Inject lateinit var commonPreference: CommonPreference
    private var _binding: FragmentSplashScreen2Binding? = null
    private val binding get() = _binding !!
    private val viewModel by activityViewModels<SplashViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() = SplashScreenFragment2()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.reportAppLaunch()
        _binding = FragmentSplashScreen2Binding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gif = binding.splashLogoImageView.drawable ?: binding.splashLogoImageView.background
        if (gif != null && gif is GifDrawable) {
            logoGifDrawable = gif.apply {
                stop()
                seekToFrame(0)
            }
        }
        
        observeApiLogin()
        requestAppLaunch()
        binding.splashScreenMotionLayout.onTransitionCompletedListener {
            if (it == R.id.firstEnd) {
                lifecycleScope.launch {
                    delay(1000)
                    with(binding.splashScreenMotionLayout) {
                        setTransition(R.id.firstEnd, R.id.secondEnd)
                        transitionToEnd()
                    }
                }
            }
            if (it == R.id.secondEnd) {
                logoGifDrawable?.start()
                if (isOperationCompleted) {
                    lifecycleScope.launch { 
                        delay(500)
                        launchHomePage()
                    }
                }
                isOperationCompleted = true
            }
        }
        if (!mPref.isPreviousDbDeleted){
            viewModel.deletePreviousDatabase()
        }
        AppEventsLogger.newLogger(requireContext()).run { 
            logEvent("app_launch")
            flush()
        }
    }
    
    private fun requestAppLaunch() {
        if (mPref.customerId != 0 && mPref.password.isNotEmpty()) {
            viewModel.loginResponse()
        }
        else {
            viewModel.credentialResponse()
        }
    }
    
    private fun observeApiLogin() {
        observe(viewModel.apiLoginResponse) {
            when (it) {
                is Resource.Success -> {
                    viewModel.sendLoginLogData()
                    if (isOperationCompleted) {
                        launchHomePage()
                    }
                    isOperationCompleted = true
                }
                is Resource.Failure -> {
                    when (it.error) {
                        is AppDeprecatedError -> {
                            showUpdateDialog(it.error.title, it.error.updateMsg, it.error.forceUpdate)
                        }
                        is CustomerNotFoundError -> {
                            mPref.clear()
                            requestAppLaunch()
                        }
                        else -> {
                            ToffeeAnalytics.logApiError("apiLoginV2", it.error.msg)
                            binding.root.snack(it.error.msg) {
                                action("Retry") {
                                    requestAppLaunch()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun launchHomePage() {
        ToffeeAnalytics.updateCustomerId(mPref.customerId)
        requireActivity().launchActivity<HomeActivity>()
        requireActivity().finish()
    }
    
    private fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(false)
            setPositiveButton("Update") { _, _ ->
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=${requireActivity().packageName}")
                        )
                    )
                }
                catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${requireActivity().packageName}")
                        )
                    )
                }
                requireActivity().finish()
            }
            if (! forceUpdate) {
                setNegativeButton("SKIP") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    viewModel.loginResponse(true)
                }
            }
        }.create().show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}