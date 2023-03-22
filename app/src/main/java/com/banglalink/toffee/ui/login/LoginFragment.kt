package com.banglalink.toffee.ui.login

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentLoginDialogBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : DialogFragment(), DefaultLifecycleObserver {
    
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var navController: NavController
    @Inject lateinit var tVChannelRepository: TVChannelRepository
    private val homeViewModel: HomeViewModel by activityViewModels()
    
    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentLoginDialogBinding.inflate(layoutInflater)
        val navHostFragment = childFragmentManager.findFragmentById(R.id.loginFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        ToffeeAnalytics.logEvent(ToffeeEvents.SIGN_IN_DIALOG)
        binding.closeIv.safeClick({
            runCatching {
                dismiss()
            }
        })
        
        return AlertDialog
            .Builder(requireContext())
//            .setCancelable(false)
            .setView(binding.root).create()
            .apply {
                // https://stackoverflow.com/questions/9102074/android-edittext-in-dialog-doesnt-pull-up-soft-keyboard
                setOnShowListener {
                    dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    dialog?.setCancelable(false)
//                    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                }
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
    }
    
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (mPref.isVerifiedUser) {
            requireActivity().showToast(getString(R.string.verify_success), Toast.LENGTH_LONG)
            cacheManager.clearAllCache()
            homeViewModel.postLoginEvent.value = true
            CoroutineScope(IO).launch {
                tVChannelRepository.deleteAllRecentItems()
            }
        }
    }
}