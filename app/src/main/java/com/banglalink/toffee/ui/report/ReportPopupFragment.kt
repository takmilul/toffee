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
import com.banglalink.toffee.databinding.FragmentReportPopupBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ReportListModel
import com.banglalink.toffee.ui.common.CheckedChangeListener
import com.banglalink.toffee.ui.mychannel.MyChannelAddToPlaylistFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReportPopupFragment : DialogFragment() ,
    CheckedChangeListener<Category>, View.OnClickListener {

    private var selectedItemPosition: Int =-1
    private var _binding: FragmentReportPopupBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ReportPopupFragmentViewModel>()
    lateinit var  mAdapter: ReportListAdapter

    private lateinit var alertDialog: AlertDialog

    companion object {
        private const val SELECTED_ITEM_POSITION = "itempostition"

        fun newInstance(itemposition:Int=-1): ReportPopupFragment {
            return ReportPopupFragment().apply {
                arguments = Bundle().apply {
                    putInt(SELECTED_ITEM_POSITION, itemposition)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedItemPosition = arguments?.getInt(SELECTED_ITEM_POSITION)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = FragmentReportPopupBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.nextButton.safeClick(this)
        binding.cancelButton.safeClick(this)
        binding.closeIv.safeClick(this)
        showData()
        return alertDialog
    }

    fun showData()
    {
        observe(viewModel.reports) {
            mAdapter=ReportListAdapter(this,it)
            binding.listview.adapter = mAdapter

            if(selectedItemPosition>=0) {
                binding.nextButton.isEnabled=true
                mAdapter.setSelectedItemPosition(selectedItemPosition)
                mAdapter.notifyDataSetChanged()
            }
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.closeIv -> alertDialog.dismiss()
            binding.cancelButton ->alertDialog.dismiss()
            binding.nextButton ->{
                alertDialog.dismiss()
                val fragment = viewModel.reports.value?.get(selectedItemPosition)?.let {
                    ReportSubmitPopupFragment.newInstance(selectedItemPosition,it.categoryName)
                }
                fragment?.show(requireActivity().supportFragmentManager, "report_submit")

            }

        }
    }

    override fun onCheckedChanged(view: View, item: Category, position: Int, isFromCheckableView: Boolean) {
        super.onCheckedChanged(view, item, position, isFromCheckableView)
        selectedItemPosition=position
        binding.nextButton.isEnabled=true
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