package com.banglalink.toffee.ui.premium.payment

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.DialogPaymentStatusBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class PaymentStatusDialog : DialogFragment() {
    
    private var title: String? = null
    private var statusCode: Int? = null
    private var statusTitle: String? = null
    private var statusMessage: String? = null
    private var isHideBackIcon: Boolean = true
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cPref: CommonPreference
    private var _binding: DialogPaymentStatusBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    companion object {
        const val SUCCESS = 200
        const val UN_SUCCESS = 0
        const val DataPackPurchaseFailedBalanceInsufficient_ERROR = 6070
        const val DataPackPurchaseFailedTechnical_ERROR = 6071
        const val DataPackPurchaseWrongPackSelection_ERROR = 6072
        const val DataPackPurchaseFailedPackNotExist_ERROR = 6073
        const val GetRequestStatus_FAILED = 6075
        const val CheckAllDataPack_Status = 6080
        const val GetRequestStatus_REQUESTED = 6085
        const val BKASH_PAYMENT_FAILED = -1
        
        const val ARG_STATUS_CODE = "statusCode"
        const val ARG_STATUS_MESSAGE = "statusMessage"
        const val ARG_STATUS_TITLE = "statusTitle"
        const val ARG_IS_HIDE_BACK_BUTTON = "isHideBackIcon"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogPaymentStatusBinding.inflate(layoutInflater)
        
        title = "Payment Confirmation"
        isHideBackIcon = arguments?.getBoolean(ARG_IS_HIDE_BACK_BUTTON, false) ?: false
        statusCode = arguments?.getInt(ARG_STATUS_CODE, 0) ?: 0
        statusTitle = arguments?.getString(ARG_STATUS_TITLE, null)
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
        binding.callBtn.setOnClickListener {
            val phoneNo = "121"
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNo, null))
            startActivity(intent)
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
                binding.titleMsg.text = statusTitle ?: getString(R.string.technical_issue_occured)
                binding.subTitleMsg.text = statusMessage ?: getString(R.string.due_some_technical_issue)
                binding.tryAgainBtn.show()
                binding.callBtn.hide()
                binding.goToHomePageBtn.hide()
            }
            SUCCESS -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_success)
                binding.titleMsg.text = getString(R.string.payment_successful)
                binding.subTitleMsg.text = getText(R.string.please_wait_you_will_be_redirected)
                binding.tryAgainBtn.hide()
                binding.goToHomePageBtn.hide()
                binding.backIcon.hide()
                binding.callBtn.hide()
                dismissDialog()
            }
            DataPackPurchaseFailedBalanceInsufficient_ERROR -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_purchase_failed_insufficient_balance)
                binding.subTitleMsg.text = getString(R.string.this_might_be_insufficient)
                binding.tryAgainBtn.show()
                binding.callBtn.hide()
                binding.goToHomePageBtn.hide()
            }
            DataPackPurchaseFailedTechnical_ERROR -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_purchase_failed_technical_error)
                binding.subTitleMsg.text = getString(R.string.this_might_be_Technical)
                binding.tryAgainBtn.show()
                binding.callBtn.hide()
                binding.goToHomePageBtn.hide()
            }
            DataPackPurchaseWrongPackSelection_ERROR -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_purchase_failed_wrong_data_plan)
                binding.subTitleMsg.text = getString(R.string.this_might_be_wrong_pack)
                binding.tryAgainBtn.show()
                binding.callBtn.hide()
                binding.goToHomePageBtn.hide()
            }
            DataPackPurchaseFailedPackNotExist_ERROR -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_purchase_failed_pack_not_exist)
                binding.subTitleMsg.text = getString(R.string.this_might_be_pack_not_exist)
                binding.tryAgainBtn.hide()
                binding.callBtn.hide()
                binding.goToHomePageBtn.show()
            }
            GetRequestStatus_FAILED -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_purchase_failed_wrong_pack)
                binding.subTitleMsg.text = getString(R.string.your_pack_purchase_failed)
                binding.tryAgainBtn.hide()
                binding.callBtn.show()
                binding.goToHomePageBtn.show()
            }
            CheckAllDataPack_Status -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.pack_activation_failed)
                binding.subTitleMsg.text = getString(R.string.your_pack_expiration_date_could_not)
                binding.tryAgainBtn.hide()
                binding.callBtn.show()
                binding.goToHomePageBtn.show()
            }
            GetRequestStatus_REQUESTED -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.your_request_is_under_process)
                binding.subTitleMsg.text = getString(R.string.please_wait_for_confirmation_message)
                binding.tryAgainBtn.hide()
                binding.callBtn.hide()
                binding.goToHomePageBtn.show()
            }
            BKASH_PAYMENT_FAILED -> {
                binding.statusImageView.setImageResource(R.drawable.ic_purchase_failed)
                binding.titleMsg.text = getString(R.string.bkash_activation_failed)
                binding.subTitleMsg.text = statusMessage ?: getString(R.string.bkash_technical_issue_occured)
                binding.tryAgainBtn.show()
                binding.callBtn.hide()
                binding.goToHomePageBtn.hide()
            }
        }
    }
    
    private suspend fun dismissDialog() {
        coroutineScope {
            delay(3000)
            mPref.prePurchaseClickedContent.value?.let { item ->
                if (item.seriesSummaryId > 0) {
                    val seriesData = SeriesPlaybackInfo(
                        item.seriesSummaryId,
                        item.seriesName ?: "",
                        item.seasonNo,
                        item.totalSeason,
                        listOf(1),
                        item.video_share_url,
                        item.id.toInt(),
                        item
                    )
                    homeViewModel.addToPlayListMutableLiveData.postValue(
                        AddToPlaylistData(
                            seriesData.playlistId(),
                            listOf(item)
                        )
                    )
                    homeViewModel.playContentLiveData.postValue(seriesData)
                } else {
                    homeViewModel.playContentLiveData.postValue(item)
                }
            }
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