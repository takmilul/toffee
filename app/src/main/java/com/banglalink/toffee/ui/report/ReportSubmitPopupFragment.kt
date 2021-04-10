package com.banglalink.toffee.ui.report

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentReportPopupBinding
import com.banglalink.toffee.databinding.FragmentReportSubmitPopupBinding
import com.banglalink.toffee.databinding.FragmentSubscribedChannelsBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilderTypeTwo


class ReportSubmitPopupFragment:DialogFragment(),View.OnClickListener {

    var selectedItemPosition:Int=0
    var itemTitle:String=""
    private var videoDuration: String =""
    private var _binding: FragmentReportSubmitPopupBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ReportSubmitPopupViewModel>()
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedItemPosition = arguments?.getInt(SELECTED_ITEM_POSITION)!!
        itemTitle = arguments?.getString(SELECTED_ITEM_TITLE)!!
        videoDuration = arguments?.getString(VIDEO_DURATION)!!
    }

    companion object {
        private const val SELECTED_ITEM_POSITION = "itempostition"
        private const val SELECTED_ITEM_TITLE = "itemtitle"
        private const val VIDEO_DURATION="videoduration"

        fun newInstance(itemposition:Int=0,itemTitle:String,duration:String): ReportSubmitPopupFragment {
            return ReportSubmitPopupFragment().apply {
                arguments = Bundle().apply {
                    putInt(SELECTED_ITEM_POSITION, itemposition)
                    putString(SELECTED_ITEM_TITLE,itemTitle)
                    putString(VIDEO_DURATION,duration)
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

        timeStamp()

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
              val fragment = ReportPopupFragment.newInstance(selectedItemPosition,videoDuration)
              fragment.show(requireActivity().supportFragmentManager, "report_submit")
          }

          binding.closeIv->alertDialog.dismiss()
      }
    }


    private fun timeStamp()
    {

        binding.hourEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val lenght = s.toString().length
                if (lenght==2) {
                    binding.minuteEt.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.minuteEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val lenght = s.toString().length
                if (lenght==2 ) {
                    binding.secondsEt.requestFocus()
                } else if(lenght==0) binding.hourEt.requestFocus()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.secondsEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                // if (s.toString().length == 0) minute_et.requestFocus()

                val lenght = s.toString().length
                if (lenght==0 ) {
                    binding.minuteEt.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

    }


}