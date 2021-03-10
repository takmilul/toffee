package com.banglalink.toffee.ui.splash

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.FragmentSplashScreenBinding
import com.banglalink.toffee.exception.AppDeprecatedError
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.facebook.appevents.AppEventsLogger
import kotlinx.coroutines.launch

class SplashScreenFragment:BaseFragment() {

    lateinit var binding: FragmentSplashScreenBinding
    private val viewModel by viewModels<SplashViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = SplashScreenFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        viewModel.reportAppLaunch()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.splashScreenMotionLayout.onTransitionCompletedListener {
            if (viewModel.isCustomerLoggedIn())
                initApp()
            else {
                lifecycleScope.launch {
                    if(findNavController().currentDestination?.id != R.id.signInFragment && findNavController().currentDestination?.id == R.id.splashScreenFragment) {
                        findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToSigninByPhoneFragment())
                    }
                }
            }
            val appEventsLogger = AppEventsLogger.newLogger(requireContext())
            appEventsLogger.logEvent("app_launch")
            appEventsLogger.flush()
        }
    }

    private fun initApp(skipUpdate:Boolean = false){
        observe(viewModel.init(skipUpdate)){
            when(it){
                is Resource.Success ->{
                    ToffeeAnalytics.updateCustomerId(mPref.customerId)
                    requireActivity().launchActivity<HomeActivity>()
                    requireActivity().finish()
                }
                is Resource.Failure->{
                    when(it.error){
                        is AppDeprecatedError ->{
                            showUpdateDialog(it.error.title,it.error.updateMsg,it.error.forceUpdate)
                        }
                        else->{
                            ToffeeAnalytics.logApiError("apiLogin",it.error.msg)
                            binding.root.snack(it.error.msg){
                                action("Retry") {
                                    initApp(skipUpdate)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton("Update") { _, _ ->
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${requireActivity().packageName}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${requireActivity().packageName}")
                    )
                )
            }
            requireActivity().finish()
        }

        if (!forceUpdate) {
            builder.setNegativeButton("SKIP") { dialogInterface, _ ->
                dialogInterface.dismiss()
                initApp(true)
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}