package com.banglalink.toffee.ui.report

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.banglalink.toffee.databinding.FragmentReportPopupBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.OffenseType
import com.banglalink.toffee.ui.common.CheckedChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class ReportPopupFragment : DialogFragment(),
    CheckedChangeListener<OffenseType>, View.OnClickListener {

    private var selectedItemPosition: Int =-1
    private var selectedType:String=""
    private var videoDuration: String =""
    private var contentId:String=""
    private var selectedOffenceTypeId:Long=0
    private var _binding: FragmentReportPopupBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ReportPopupFragmentViewModel>()
    lateinit var  mAdapter: ReportListAdapter
    private lateinit var alertDialog: AlertDialog

    companion object {
        private const val SELECTED_ITEM_POSITION = "itempostition"
        private const val VIDEO_DURATION="videoduration"
        private const val CONTENT_ID="contentid"

        fun newInstance(itemposition: Int = -1, duration: String, contentId:String): ReportPopupFragment {
            return ReportPopupFragment().apply {
                arguments = Bundle().apply {
                    putInt(SELECTED_ITEM_POSITION, itemposition)
                    putString(VIDEO_DURATION, duration)
                    putString(CONTENT_ID, contentId)

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedItemPosition = arguments?.getInt(SELECTED_ITEM_POSITION)!!
        videoDuration = arguments?.getString(VIDEO_DURATION)!!
        contentId = arguments?.getString(CONTENT_ID)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = FragmentReportPopupBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        var isInitialized = false

        binding.nextButton.safeClick(this)
        binding.cancelButton.safeClick(this)
        binding.closeIv.safeClick(this)
        mAdapter=ReportListAdapter(this)
        binding.listview.adapter = mAdapter

        //https://stackoverflow.com/questions/59521691/use-viewlifecycleowner-as-the-lifecycleowner (Comment)
        lifecycleScope.launchWhenStarted {
            viewModel.loadReportList().collectLatest {
                mAdapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            mAdapter.loadStateFlow
//                .distinctUntilChangedBy { it.refresh }
                .collectLatest {
                mAdapter.apply {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                    if (isEmpty && !isLoading) {
                        //requireContext().showToast("Unable to load data!")
                        requireContext().showToast("Oops! Something went wrong.")
                        alertDialog.dismiss()
                    }
                    isInitialized = true
                }
            }
        }

        if(selectedItemPosition>=0) {
            binding.nextButton.isEnabled=true
            mAdapter.setSelectedItemPosition(selectedItemPosition)
        }
        return alertDialog
    }



//    fun showData()
//    {
//        observe(viewModel.reports) {
//            if(selectedItemPosition>=0) {
//                binding.nextButton.isEnabled=true
//                mAdapter.setSelectedItemPosition(selectedItemPosition)
//                mAdapter.notifyDataSetChanged()
//            }
//        }
//
//    }

    override fun onClick(v: View?) {
        when (v) {
            binding.closeIv -> alertDialog.dismiss()
            binding.cancelButton -> alertDialog.dismiss()
            binding.nextButton -> {
                val fragment = ReportSubmitPopupFragment.newInstance(
                    selectedItemPosition,
                    selectedType,
                    videoDuration,
                    contentId,
                    selectedOffenceTypeId
                )
                fragment.show(requireActivity().supportFragmentManager, "report_popup")
                alertDialog.dismiss()
            }
        }
    }

    override fun onCheckedChanged(
        view: View,
        item: OffenseType,
        position: Int,
        isFromCheckableView: Boolean
    ) {
        super.onCheckedChanged(view, item, position, isFromCheckableView)
        selectedItemPosition=position
        binding.nextButton.isEnabled=true
        selectedType=item.type
        selectedOffenceTypeId=item.id
        when (view) {
            is RadioButton -> {
                if (view.isChecked) {
                    mAdapter.setSelectedItemPosition(position)
                    mAdapter.notifyDataSetChanged()
                } else {
                    mAdapter.setSelectedItemPosition(-1)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }


}