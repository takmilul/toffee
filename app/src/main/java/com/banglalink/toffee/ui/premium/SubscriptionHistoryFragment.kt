package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.SubsHistoryDetail
import com.banglalink.toffee.databinding.FragmentSubscriptionHistoryBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltipUtils


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
        //https://github.com/douglasjunior/android-simple-tooltip
        SimpleTooltip.Builder(requireContext())
            .anchorView(view)
            .text(R.string.subscription_history_tooltip_text)
            .gravity(Gravity.BOTTOM)
            .animated(false)
            .transparentOverlay(true)
            .margin(0f)
            .contentView(R.layout.tooltip_layout_subscription, R.id.tooltipText)
            .arrowColor(resources.getColor(R.color.tooltip_bg_color))
            .arrowHeight(SimpleTooltipUtils.pxFromDp(10f).toInt().toFloat())
            .arrowWidth(SimpleTooltipUtils.pxFromDp(14f).toInt().toFloat())
            .focusable(true)
            .build()
            .show()

//        https://github.com/skydoves/balloon#balloon-builder-methods
//        val balloon = Balloon.Builder(requireContext())
//            .setWidthRatio(1.0f)
//            .setWidth(BalloonSizeSpec.WRAP)
//            .setHeight(BalloonSizeSpec.WRAP)
//            .setText(resources.getString(R.string.subscription_history_tooltip_text))
//            .setTextColorResource(R.color.tooltip_txt_color)
//            .setTextSize(12f)
//            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
//            .setArrowSize(10)
//            .setArrowPosition(0.5f)
//            .setPadding(8)
//            .setMarginHorizontal(20.dp)
//            .setTextTypeface(Typeface.BOLD)
//            .setCornerRadius(8f)
//            .setBackgroundColorResource(R.color.tooltip_bg_color)
////            .setBalloonAnimation(BalloonAnimation.FADE)
////            .setLifecycleOwner(viewLifecycleOwner)
//            .setTextGravity(Gravity.LEFT)
//            .build()
//
//        view.showAlignBottom(balloon)
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