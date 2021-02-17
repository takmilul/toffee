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
import com.banglalink.toffee.extension.action
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.snack
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
        fun newInstance() =
            SplashScreenFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.splashScreenMotionLayout.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                println("Transition started")
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                println("Transition changed")
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                if (viewModel.isCustomerLoggedIn())
                    initApp()
                else {
                    lifecycleScope.launch {
                        findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToSigninByPhoneFragment())
                    }
                }
                val appEventsLogger = AppEventsLogger.newLogger(requireContext())
                appEventsLogger.logEvent("app_launch")
                appEventsLogger.flush()
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
                println("Transition triggered")
            }
        })

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
//                            ToffeeAnalytics.apiLoginFailed(it.error.msg)
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

        builder.setPositiveButton(
            "Update"
        ) { _, _ ->
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
            builder.setNegativeButton(
                "SKIP"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
                initApp(true)
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }
}