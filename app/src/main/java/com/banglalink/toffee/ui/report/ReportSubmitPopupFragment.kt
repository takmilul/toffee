package com.banglalink.toffee.ui.report

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentReportPopupBinding
import com.banglalink.toffee.databinding.FragmentReportSubmitPopupBinding
import com.banglalink.toffee.databinding.FragmentSubscribedChannelsBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilderTypeTwo


class ReportSubmitPopupFragment:  DialogFragment(),View.OnClickListener {
    
    var selectedItemPosition:Int=0
    var itemTitle:String=""
    private var _binding: FragmentReportSubmitPopupBinding? = null
    private val binding get() = _binding!!
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedItemPosition = arguments?.getInt(SELECTED_ITEM_POSITION)!!
        itemTitle = arguments?.getString(SELECTED_ITEM_TITLE)!!
    }

    companion object {
        private const val SELECTED_ITEM_POSITION = "itempostition"
        private const val SELECTED_ITEM_TITLE = "itemtitle"

        fun newInstance(itemposition:Int=0,itemTitle:String): ReportSubmitPopupFragment {
            return ReportSubmitPopupFragment().apply {
                arguments = Bundle().apply {
                    putInt(SELECTED_ITEM_POSITION, itemposition)
                    putString(SELECTED_ITEM_TITLE,itemTitle)
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
        binding.reportType.setText(itemTitle)


        return alertDialog
    }

    override fun onClick(v: View?) {
      when(v){
          binding.reportButton -> {
              alertDialog.dismiss()
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

          binding.backButton ->{

              alertDialog.dismiss()
              val fragment = ReportPopupFragment.newInstance(selectedItemPosition)
              fragment.show(requireActivity().supportFragmentManager, "report_submit")
          }

          binding.closeIv->alertDialog.dismiss()
      }
    }


}