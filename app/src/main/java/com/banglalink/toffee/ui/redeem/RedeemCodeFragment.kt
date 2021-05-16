package com.banglalink.toffee.ui.redeem

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.databinding.FragmentRedeemCodeBinding
import com.banglalink.toffee.extension.action
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.snack
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.util.unsafeLazy

class RedeemCodeFragment : BaseFragment() {
    private var _binding: FragmentRedeemCodeBinding?=null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RedeemCodeViewModel>()

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(requireContext())
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
                    showDisplayMessageDialog(requireContext(), it.data.referralStatusMessage) {
                        findNavController().popBackStack()
                    }
                }
                is Resource.Failure -> {
                    if (it.error.code == 100) {
                        showDisplayMessageDialog(requireContext(), it.error.msg)
                    } else {
                        binding.root.snack(it.error.msg) {
                            action("Ok") {
                            }
                        }
                    }
                }
            }
        }
    }


}