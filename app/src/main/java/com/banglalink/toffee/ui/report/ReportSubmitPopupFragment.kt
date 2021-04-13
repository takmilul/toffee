package com.banglalink.toffee.ui.report

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentReportSubmitPopupBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ReportInfo
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilderTypeTwo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportSubmitPopupFragment:DialogFragment(),View.OnClickListener {

    var selectedItemPosition:Int=0
    var itemTitle:String=""
    private var videoDuration: String =""
    private var contentId:String =""
    private var selectedOffenceTypeId:Long=0
    var timestamp=""
    private var _binding: FragmentReportSubmitPopupBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ReportSubmitPopupViewModel>()
    private val homeViewModel by  activityViewModels<HomeViewModel>()
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedItemPosition = arguments?.getInt(SELECTED_ITEM_POSITION)!!
        itemTitle = arguments?.getString(SELECTED_ITEM_TITLE)!!
        videoDuration = arguments?.getString(VIDEO_DURATION)!!
        contentId = arguments?.getString(CONTENT_ID)!!
        selectedOffenceTypeId = arguments?.getLong(OFFENCE_TYPE_ID)!!
    }

    companion object {
        private const val SELECTED_ITEM_POSITION = "itempostition"
        private const val SELECTED_ITEM_TITLE = "itemtitle"
        private const val VIDEO_DURATION="videoduration"
        private const val CONTENT_ID="contentid"
        private const val OFFENCE_TYPE_ID="offencetypeid"

        fun newInstance(itemposition: Int = 0, itemTitle: String, duration: String, vieocontentId:String,offenceTyoeId:Long): ReportSubmitPopupFragment {
            return ReportSubmitPopupFragment().apply {
                arguments = Bundle().apply {
                    putInt(SELECTED_ITEM_POSITION, itemposition)
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
        binding.reportType.setText(itemTitle)

        handleTimestamp()

        return alertDialog
    }

    override fun onClick(v: View?) {
      when(v){
          binding.reportButton -> {

              handleSubmitButton()
          }

          binding.backButton -> {

              alertDialog.dismiss()
              val fragment = ReportPopupFragment.newInstance(selectedItemPosition,videoDuration,contentId)
              fragment.show(requireActivity().supportFragmentManager, "report_submit")
          }

          binding.closeIv -> alertDialog.dismiss()
      }
    }

    private fun handleSubmitButton() {
        if (binding.hourEt.text.isNullOrBlank() &&
            binding.minuteEt.text.isNullOrBlank() &&
            binding.secondsEt.text.isNullOrBlank()){

            binding.errorTimestampTv.let {
                it.show()
                it.setText("Timestamp required")
            }
        }
        else{
            binding.errorTimestampTv.hide()

            compareTime()
        }
    }


    private fun handleTimestamp()
    {
        binding.hourEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val data = s.toString()
                val lenght = data.length
                if (lenght == 2) {
                    if (data.toInt() > 5) {
                        binding.hourEt.setText("05")
                    }
                    binding.minuteEt.requestFocus()
                    if (binding.minuteEt.text.length == 2) binding.minuteEt.setSelection(2)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.minuteEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val data = s.toString()
                val lenght = data.length
                if (lenght == 2) {
                    if (data.toInt() > 59) binding.minuteEt.setText("59")
                    binding.secondsEt.requestFocus()
                    if (binding.secondsEt.text.length == 2) binding.secondsEt.setSelection(2)
                } else if (lenght == 0) {
                    binding.hourEt.requestFocus()
                    if (binding.hourEt.text.length == 2) binding.hourEt.setSelection(2)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val v = ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.secondsEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val data = s.toString()
                val lenght = data.length
                if (lenght == 0) {
                    binding.minuteEt.requestFocus()
                    if (binding.minuteEt.text.length == 2) binding.minuteEt.setSelection(2)
                } else if (data.toInt() > 59) {
                    binding.secondsEt.setText("59")
                    binding.secondsEt.setSelection(2)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

    }

    fun compareTime()
    {
        val hour= binding.hourEt.text
        val minute= binding.minuteEt.text
        val second= binding.secondsEt.text
         timestamp ="${if(hour.isNullOrBlank()) "00" else if (minute.length==1) "0$hour" else hour}:" +
                "${if(minute.isNullOrBlank()) "00" else if (minute.length==1) "0$minute" else minute}:" +
                "${if(second.isNullOrBlank()) "00" else if (minute.length==1) "0$second" else second}"

        val timeformate: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

        val videoDurationTime = timeformate.parse(videoDuration).time
        val timeStampTime = timeformate.parse(timestamp).time

        if (videoDurationTime >= timeStampTime) {
            if (timestamp.equals("00:00:00"))
            {
                binding.errorTimestampTv.let {
                    it.show()
                    it.setText("*Timestamp required")
                }
                return
            }
            submitInfo()
            binding.errorTimestampTv.hide()
        }
        else{
            binding.errorTimestampTv.let {
                it.show()
                it.setText("*$timestamp is greater then $videoDuration")
            }
        }

    }

    private fun submitInfo() {

        val reporInfo = ReportInfo(contentId = contentId.toLong(),
            offenseTypeId = selectedOffenceTypeId.toInt(),
            timeStamp = timestamp,
            additionalDetail = binding.additionalDetailsEt.text.toString())
        homeViewModel.sendReportData(reporInfo)

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

}