package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.databinding.ButtomSheetPackPaymentOptionBinding
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.common.ChildDialogFragment

class PackPaymentOptionFragment : ChildDialogFragment() {

    private var _binding: ButtomSheetPackPaymentOptionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetPackPaymentOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            trailCard.setOnClickListener {
                findNavController().navigate(R.id.action_payment_to_trail)
            }
            blPackCard.setOnClickListener {
                findNavController().navigate(R.id.action_payment_to_pack)
            }
            bkashPackCard.setOnClickListener {
                findNavController().navigate(R.id.action_payment_to_pack)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}