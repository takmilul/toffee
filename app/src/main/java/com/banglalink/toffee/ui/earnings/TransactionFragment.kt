package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.Transaction

class TransactionFragment : BaseListFragment<Transaction>(), BaseListItemCallback<Transaction> {

    private var enableToolbar: Boolean = false
    override val mAdapter by lazy { TransactionAdapter(this) }
    override val mViewModel by viewModels<TransactionViewModel>()

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

    /*override fun initAdapter() {
        mAdapter = TransactionAdapter(this)
        mViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }*/

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_transactions_empty, "You don't have any transactions yet")
    }

}