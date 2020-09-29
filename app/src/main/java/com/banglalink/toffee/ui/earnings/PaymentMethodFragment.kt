package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class PaymentMethodFragment : SingleListFragmentV2<PaymentMethod>(), SingleListItemCallback<PaymentMethod> {

    private var enableToolbar: Boolean = false

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        @JvmStatic
        fun newInstance(enableToolbar: Boolean): PaymentMethodFragment {
            val instance = PaymentMethodFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        }
    }

    override fun initAdapter() {
        mAdapter = PaymentMethodAdapter(this)
        mViewModel = ViewModelProvider(this).get(PaymentMethodViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_payment_methods_empty, "Add payment method to withdraw money")
    }

    override fun onOpenMenu(anchor: View, item: PaymentMethod) {
        super.onOpenMenu(anchor, item)
        findNavController().navigate(R.id.action_earningsFragment_to_addNewPaymentMethodFragment)
    }
}