package com.banglalink.toffee.ui.report

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentReportSubmitPopupBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.model.ReportInfo
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilderTypeTwo

class ReportSubmitPopupFragment : DialogFragment(), View.OnClickListener {
    
    var itemTitle: String = ""
    var selectedItemPosition: Int = 0
    private var contentId: String = ""
    private var videoDuration: String = ""
    private var selectedOffenceTypeId: Long = 0
    private lateinit var alertDialog: AlertDialog
    private var _binding: FragmentReportSubmitPopupBinding? = null
    private val binding get() = _binding !!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedItemPosition = arguments?.getInt(SELECTED_ITEM_POSITION) !!
        itemTitle = arguments?.getString(SELECTED_ITEM_TITLE) !!
        videoDuration = arguments?.getString(VIDEO_DURATION) !!
        contentId = arguments?.getString(CONTENT_ID) !!
        selectedOffenceTypeId = arguments?.getLong(OFFENCE_TYPE_ID) !!
    }
    
    companion object {
        private const val SELECTED_ITEM_POSITION = "itempostition"
        private const val SELECTED_ITEM_TITLE = "itemtitle"
        private const val VIDEO_DURATION = "videoduration"
        private const val CONTENT_ID = "contentid"
        private const val OFFENCE_TYPE_ID = "offencetypeid"
        
        fun newInstance(itemPosition: Int = 0, itemTitle: String, duration: String, vieocontentId: String, offenceTyoeId: Long): ReportSubmitPopupFragment {
            return ReportSubmitPopupFragment().apply {
                arguments = Bundle().apply {
                    putInt(SELECTED_ITEM_POSITION, itemPosition)
                    putString(SELECTED_ITEM_TITLE, itemTitle)
                    putString(VIDEO_DURATION, duration)
                    putString(CONTENT_ID, vieocontentId)
                    putLong(OFFENCE_TYPE_ID, offenceTyoeId)
                }
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentReportSubmitPopupBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        
        binding.reportButton.safeClick(this)
        binding.closeIv.safeClick(this)
        binding.backButton.safeClick(this)
        binding.closeIv.safeClick(this)
        binding.reportType.text = itemTitle
        return alertDialog
    }
    
    override fun onClick(v: View?) {
        when (v) {
            binding.reportButton -> submitInfo()
            binding.backButton -> {
                alertDialog.dismiss()
                val fragment = ReportPopupFragment.newInstance(selectedItemPosition, videoDuration, contentId)
                fragment.show(requireActivity().supportFragmentManager, "report_submit")
            }
            binding.closeIv -> alertDialog.dismiss()
        }
    }
    
    private fun submitInfo() {
        alertDialog.dismiss()
        val reportInfo = ReportInfo(contentId.toLong(), selectedOffenceTypeId.toInt(), "", binding.additionalDetailsEt.text.toString())
        homeViewModel.sendReportData(reportInfo)
        VelBoxAlertDialogBuilderTypeTwo(
            requireContext(),
            title = "Successfully Reported",
            text = "Toffee will review this report according to \n" +
                "Community Guidelines.\n" +
                "Thanks for staying vigilant!",
            icon = R.drawable.ic_check_magenta,
            positiveButtonTitle = "Ok",
            positiveButtonListener = {
                it?.dismiss()
            }
        ).create().show()
    }
}