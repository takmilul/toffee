package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.SubsHistoryDetail
import com.banglalink.toffee.databinding.FragmentSubscriptionHistoryBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.getBalloon
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.showAlignBottom
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration

class SubscriptionHistoryFragment : BaseFragment(), BaseListItemCallback<SubsHistoryDetail> {
    private var _binding: FragmentSubscriptionHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private lateinit var mAdapter: SubscriptionHistoryAdapter
    private lateinit var mAdapterWithFooter: SubscriptionHistoryFooterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionHistoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = SubscriptionHistoryAdapter(this)
        mAdapterWithFooter = SubscriptionHistoryFooterAdapter(mAdapter)

        with(binding.paymentHisList) {
            adapter = mAdapterWithFooter
            addItemDecoration(MarginItemDecoration(8))
        }
        binding.progressBar.load(R.drawable.content_loader)
        binding.paymentHisList.hide()
        binding.failureInfoLayout.hide()

        if (mPref.isVerifiedUser) {
            observeSubscriptionHistory()
            observeClick()
        } else {
            showSignIn()
        }
    }

    override fun onOpenMenu(view: View, item: SubsHistoryDetail) {
        super.onOpenMenu(view, item)
        runCatching {
            view.showAlignBottom(
                requireContext().getBalloon(
                    item.tooltipMessage ?: getString(R.string.subscription_history_tooltip_text)
                )
            )
        }
    }


    private fun observeClick() {
        observe(viewModel.clickedOnSubHistory) {
            if (it) {
                binding.failureInfoLayout.hide()
                binding.paymentHisList.hide()
                binding.progressBar.show()
                viewModel.getPremiumPackSubscriptionHistory()
            }
        }
    }

    private fun observeSubscriptionHistory() {
        observe(viewModel.premiumPackSubHistoryLiveData) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    binding.failureInfoLayout.hide()
                    binding.paymentHisList.show()

                    response.data.let { subHistoryResponseBean ->
                        subHistoryResponseBean?.subsHistoryDetails.ifNotNullOrEmpty {
                            mAdapter.removeAll()
                            mAdapter.addAll(it.toList())
                            mAdapterWithFooter.setFooterText(response.data?.historyShowingText ?: "Showing up to 2 years of payment history")
                            mAdapter.notifyDataSetChanged()
                            mAdapterWithFooter.notifyDataSetChanged()
                        }
                        if (subHistoryResponseBean?.subsHistoryDetails.isNullOrEmpty()) {
                            showNotFound()
                        }
                    }
                }

                is Resource.Failure -> {
                    showNotFound()
                    binding.progressBar.hide()
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }

    private fun showNotFound() {
        binding.paymentHisList.hide()
        binding.iconFailureType.setImageResource(R.drawable.ic_empty_pack_list)
        binding.textFailureMessage.text =
            getString(R.string.no_subscription_found)
        binding.btnSingin.hide()
        binding.failureInfoLayout.show()
    }

    private fun showSignIn() {
        binding.paymentHisList.hide()
        binding.iconFailureType.setImageResource(R.drawable.ic_subscription_login)
        binding.textFailureMessage.text = getString(R.string.signin_text_subscription)
        binding.btnSingin.show()
        binding.failureInfoLayout.show()

        binding.btnSingin.safeClick({
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "my_subscriptions",
                    "method" to "mobile"
                )
            )
            requireActivity().checkVerification{
                mPref.isLoggedInFromSubHistory = true
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}