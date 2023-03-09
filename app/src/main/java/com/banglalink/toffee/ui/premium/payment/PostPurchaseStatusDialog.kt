package com.banglalink.toffee.ui.premium.payment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.DialogPostPurchaseStatusBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostPurchaseStatusDialog : DialogFragment() {
    
    private var title: String? = null
    private var statusCode: Int? = null
    private var statusMessage: String? = null
    private var isHideBackIcon: Boolean = true
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cPref: CommonPreference
    private var _binding: DialogPostPurchaseStatusBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        const val SUCCESS = 200
        const val UN_SUCCESS = 0
        const val DataPackPurchase_FAILED = 6070
        const val GetRequestStatus_FAILED = 6075
        const val CheckAllDataPack_Status = 6080
        const val GetRequestStatus_REQUESTED = 6085
        const val BKASH_PAYMENT_FAILED = -1
        
        const val ARG_STATUS_CODE = "statusCode"
        const val ARG_STATUS_MESSAGE = "statusMessage"
        const val ARG_IS_HIDE_BACK_BUTTON = "isHideBackIcon"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogPostPurchaseStatusBinding.inflate(layoutInflater)
        
        title = "Payment Confirmation"
        isHideBackIcon = arguments?.getBoolean(ARG_IS_HIDE_BACK_BUTTON, false) ?: false
        statusCode = arguments?.getInt(ARG_STATUS_CODE, 0) ?: 0
        statusMessage = arguments?.getString(ARG_STATUS_MESSAGE, null)
        
        binding.titleTv.text = title
        
        observeTopBarBackground()
        
        viewLifecycleOwner.lifecycleScope.launch {
            observeErrorLogic(statusCode)
        }
        
        runCatching {
            binding.backIcon.setOnClickListener {
                dialog?.dismiss()
            }
        }
        
        binding.goToHomePageBtn.setOnClickListener {
            requireActivity().launchActivity<HomeActivity>()
        }
        binding.tryAgainBtn.setOnClickListener {
            dialog?.dismiss()
        }
        return binding.root
    }
    
    private suspend fun observeErrorLogic(errorCode: Int?) {
        when (errorCode) {
            UN_SUCCESS -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_warning)
                binding.titleMsg.text = getString(R.string.technical_issue_occured)
                binding.subTitleMsg.text = getString(R.string.due_some_technical_issue)
                binding.tryAgainBtn.hide()
                binding.goToHomePageBtn.hide()
            }
            SUCCESS -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_success)
                binding.titleMsg.text = getString(R.string.payment_successful)
                binding.subTitleMsg.text = getText(R.string.please_wait_you_will_be_redirected)
                binding.tryAgainBtn.hide()
                binding.goToHomePageBtn.hide()
                binding.backIcon.hide()
                dismissDialog()
            }
            DataPackPurchase_FAILED -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_purchase_failed)
                binding.subTitleMsg.text = getString(R.string.this_might_be_insufficient)
                binding.tryAgainBtn.show()
                binding.goToHomePageBtn.hide()
             //   dismissDialog()
            }
            GetRequestStatus_FAILED -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_purchase_failed)
                binding.subTitleMsg.text = getString(R.string.your_pack_purchase_failed)
                binding.tryAgainBtn.show()
                binding.goToHomePageBtn.hide()
            }
            CheckAllDataPack_Status -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_activation_failed)
                binding.subTitleMsg.text = getString(R.string.your_pack_expiration_date_could_not)
                binding.tryAgainBtn.hide()
                binding.goToHomePageBtn.show()
            }
            GetRequestStatus_REQUESTED -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.your_request_is_under_process)
                binding.subTitleMsg.text = getString(R.string.please_wait_for_confirmation_message)
                binding.tryAgainBtn.hide()
                binding.goToHomePageBtn.show()
            }
            BKASH_PAYMENT_FAILED -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_purchase_failed)
                binding.subTitleMsg.text = statusMessage ?: getString(R.string.this_might_be_insufficient)
                binding.tryAgainBtn.show()
                binding.goToHomePageBtn.hide()
            }
        }
    }
    
    private suspend fun dismissDialog() {
        coroutineScope {
            delay(3000)
            mPref.packDetailsPageRefreshRequired.value = true
            dialog?.dismiss()
        }
    }
    
    private fun observeTopBarBackground() {
        val isActive = try {
            mPref.isTopBarActive && Utils.getDate(mPref.topBarStartDate)
                .before(mPref.getSystemTime()) && Utils.getDate(mPref.topBarEndDate)
                .after(mPref.getSystemTime())
        } catch (e: Exception) {
            false
        }
        if (isActive) {
            if (mPref.topBarType == "png") {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val imagePath =
                            if (cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO) mPref.topBarImagePathLight else mPref.topBarImagePathDark
                        if (!imagePath.isNullOrBlank()) {
                            binding.toolbarImageView.load(imagePath)
                        }
                    } catch (e: Exception) {
                        ToffeeAnalytics.logException(e)
                    }
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.setLayout(width, height)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}