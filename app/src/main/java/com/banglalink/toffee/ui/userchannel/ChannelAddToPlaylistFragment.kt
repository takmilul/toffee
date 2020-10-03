package com.banglalink.toffee.ui.userchannel

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.ui.common.CheckedChangeListener
import kotlinx.android.synthetic.main.alert_dialog_add_to_playlist.view.*
import java.util.*

class ChannelAddToPlaylistFragment : DialogFragment(), CheckedChangeListener<String> {
    private lateinit var mAdapter: ChannelAddToPlaylistAdapter

    companion object {
        fun newInstance(items: List<String>): ChannelAddToPlaylistFragment {
            val instance = ChannelAddToPlaylistFragment()
            val bundle = Bundle()
            bundle.putStringArrayList("items", items as ArrayList<String>)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val data = arguments?.getStringArrayList("items") as List<String>
        mAdapter = ChannelAddToPlaylistAdapter(this)
        mAdapter.addAll(data)
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_add_to_playlist, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialogView.listview.adapter = mAdapter
        dialogView.addButton.setOnClickListener {
            alertDialog.dismiss()
            val fragment = ChannelAddToPlaylistCreateFragment.newInstance(data)
            fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
            fragment.dialog?.setCanceledOnTouchOutside(true)
        }
        dialogView.doneButton.setOnClickListener { alertDialog.dismiss() }
        
        return alertDialog
    }

    override fun onCheckedChanged(view: View, item: String, position: Int, isFromCheckableView: Boolean) {
        super.onCheckedChanged(view, item, position, isFromCheckableView)
        when (view) {
            is RadioButton -> {
                if (view.isChecked) {
                    mAdapter.setSelectedItemPosition(position)
                    mAdapter.notifyDataSetChanged()
                }
                else {
                    mAdapter.setSelectedItemPosition(-1)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}