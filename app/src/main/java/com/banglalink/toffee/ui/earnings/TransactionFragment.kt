package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.model.Transaction
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class TransactionFragment : SingleListFragmentV2<Transaction>(), SingleListItemCallback<Transaction> {

    private var enableToolbar: Boolean = false

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        @JvmStatic
        fun newInstance(enableToolbar: Boolean): TransactionFragment {
            val instance = TransactionFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        }
    }

    override fun initAdapter() {
        mAdapter = TransactionAdapter(this)
        mViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_transactions_empty, "You don't have any transactions yet")
    }
}