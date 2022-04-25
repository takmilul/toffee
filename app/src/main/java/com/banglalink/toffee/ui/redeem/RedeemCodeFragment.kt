package com.banglalink.toffee.ui.redeem

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.databinding.FragmentRedeemCodeBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.ToffeeAlertDialogBuilder
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.ui.widget.showRedeemDisplayMessageDialog
import com.banglalink.toffee.util.unsafeLazy

class RedeemCodeFragment : BaseFragment() {
    private var _binding: FragmentRedeemCodeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RedeemCodeViewModel>()

    private val progressDialog by unsafeLazy {
        ToffeeProgressDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRedeemCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.redeemBtn.setOnClickListener { _ -> handleRedeemCodeButton() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleRedeemCodeButton() {
        if (TextUtils.isEmpty(binding.referralCode.text.toString())) {
            showDisplayMessageDialog(requireContext(), "Please enter valid referral code.")
            return
        }
        redeemReferralCode(binding.referralCode.text.toString())
    }

    private fun redeemReferralCode(redeemCode: String) {
        progressDialog.show()
        observe(viewModel.redeemReferralCode(redeemCode)) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    val response = it.data
                    if (response.isBullterPointMessage!!) {
                        showRedeemDisplayMessageDialog(
                            requireContext(),
                            response.title,
                            response.message,
                            response.bulletMessage
                        )
                    } else {
                        if (!response.isRedeemSuccess!!) {
                            ToffeeAlertDialogBuilder(
                                requireContext(),
                                hideCloseButton = true,
                                icon = R.drawable.ic_error,
                            ).apply {
                                response.title?.let { it1 -> setTitle(it1) }
                                response.message?.let { it1 -> setText(it1) }
                                setNegativeButtonListener(getString(R.string.okay_text)) {
                                    it?.dismiss()
                                }
                            }.create().show()
                        } else {
                            ToffeeAlertDialogBuilder(
                                requireContext(),
                                hideCloseButton = true,
                                icon = R.drawable.ic_check_magenta
                            ).apply {
                                response.title?.let { it1 -> setTitle(it1) }
                                response.message?.let { it1 -> setText(it1) }
                                setNegativeButtonListener(getString(R.string.okay_text)) {
                                    it?.dismiss()
                                }
                            }.create().show()
                        }
                    }
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.REDEEM_REFERRAL_CODE,
                            FirebaseParams.BROWSER_SCREEN to "Enter Referral code page",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg
                        )
                    )
                    if (it.error.code == 100) {
                        showDisplayMessageDialog(requireContext(), it.error.msg)
                    } else {
                        context?.showToast(it.error.msg)
                    }
                }
            }
        }
    }


}