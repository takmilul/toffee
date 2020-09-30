package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentWithdrawBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.PaymentMethod
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.CheckboxCheckedChangedListener
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import kotlinx.android.synthetic.main.fragment_withdraw.*

class WithdrawFragment : Fragment(), CheckboxCheckedChangedListener<PaymentMethod>, OnClickListener {

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private lateinit var mAdapter: WithdrawAdapter
    private lateinit var mViewModel: WithdrawViewModel
    private var enableToolbar: Boolean = false
    lateinit var binding: FragmentWithdrawBinding

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"

        @JvmStatic
        fun newInstance(enableToolbar: Boolean): WithdrawFragment {
            val instance = WithdrawFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_withdraw, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    fun initAdapter() {
        mAdapter = WithdrawAdapter(this)
        mViewModel = ViewModelProvider(this).get(WithdrawViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }

    private fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_payment_methods_empty, "Add payment method to withdraw money")
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()

        arguments?.getString(SingleListFragmentV2.ARG_TITLE)?.let {
            activity?.title = it
        }

        setupEmptyView()

        setupListView()

        observeList()

        mViewModel.loadData()

        binding.nextButton.setOnClickListener(this)
    }

    private fun setupListView() {
        with(binding.listview) {
            adapter = mAdapter

            val listLayoutManager = getRecyclerLayoutManager()
            layoutManager = listLayoutManager

            if (shouldLoadMore) {
                if (listLayoutManager is LinearLayoutManager) {
                    scrollListener = object : EndlessRecyclerViewScrollListener(listLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            view: RecyclerView
                        ) {
                            mViewModel.loadData()
                        }
                    }
                }
                else if (listLayoutManager is GridLayoutManager) {
                    scrollListener = object : EndlessRecyclerViewScrollListener(listLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            view: RecyclerView
                        ) {
                            mViewModel.loadData()
                        }
                    }
                }
                addOnScrollListener(scrollListener)
            }
            setHasFixedSize(true)
        }
    }

    private val shouldLoadMore = true

    private fun getRecyclerLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
    }

    private fun setupEmptyView() {
        val info = getEmptyViewInfo()
        if (info.first > 0) {
            binding.emptyViewIcon.setImageResource(info.first)
        }
        else {
            binding.emptyViewIcon.visibility = View.GONE
        }

        info.second?.let {
            binding.emptyViewLabel.text = it
        }
    }

    private fun observeList() {
        mViewModel.listData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val itemCount = mAdapter.itemCount
                    if (it.data.isEmpty() && itemCount == 0) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                        binding.nextButton.visibility = View.GONE
                        binding.addPaymentButton.setOnClickListener(this)
                    }
                    else {
                        binding.emptyView.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                        binding.nextButton.visibility = View.VISIBLE
                    }
                    mAdapter.addAll(it.data)
                }
                is Failure -> {
                    scrollListener.resetState()
                    activity?.showToast(it.error.msg)
                }
            }
        })

        mViewModel.showProgress.observe(viewLifecycleOwner, Observer {
            if (it) {
                showProgress()
            }
            else {
                hideProgress()
            }
        })
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        binding.listview.adapter = null
        binding.listview.clearOnScrollListeners()
        binding.unbind()
        super.onDestroyView()
    }

    override fun onCheckedChanged(view: View, item: PaymentMethod, position: Int) {
        super.onCheckedChanged(view, item, position)
        if ((view as CheckBox).isChecked) {
            mAdapter.setSelectedItemPosition(position)
            mAdapter.notifyDataSetChanged()
        }
        else {
            mAdapter.setSelectedItemPosition(- 1)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            nextButton -> {
                val amount = binding.withdrawalAmountEditText.text.toString()
                if (amount.isNotEmpty() && mAdapter.selectedPosition >= 0) {
                    val paymentMethod = mAdapter.getItem(mAdapter.selectedPosition)
                    val action = EarningsFragmentDirections.actionEarningsFragmentToReviewWithdrawalFragment(amount, paymentMethod !!)
                    findNavController().navigate(action)
                }
                else {
                    Toast.makeText(requireContext(), "Please enter amount and select an account to withdraw", Toast.LENGTH_SHORT).show()
                }
            }
            addPaymentButton -> {
                findNavController().navigate(R.id.action_earningsFragment_to_addNewPaymentMethodFragment)
            }
        }
    }
}