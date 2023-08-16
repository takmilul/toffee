package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentReedemVoucherCodeBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.validateInput

class ReedemVoucherCodeFragment : Fragment() {
    private lateinit var binding: FragmentReedemVoucherCodeBinding
    private var voucherCode: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReedemVoucherCodeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(null,null, null, null)
        binding.backImg.safeClick({
            findNavController().popBackStack()
        })

        binding.redeemVoucherBtn.safeClick({
            voucherCode = binding.giftVoucherCode.text.toString().trim()
            if (voucherCode.isBlank()) {
                binding.giftVoucherCode.validateInput(
                    binding.tvGiftVoucherCodeError,
                    R.string.voucher_code_not_valid,
                    R.color.pink_to_accent_color,
                    R.drawable.error_single_line_input_text_bg
                )
                binding.tvGiftVoucherCodeError.visibility = View.VISIBLE
                binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(),R.drawable.ic_not_verified), null)
            }
        })
    }

}