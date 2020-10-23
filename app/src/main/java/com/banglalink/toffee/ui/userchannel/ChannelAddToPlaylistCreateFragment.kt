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
import com.banglalink.toffee.model.UgcChannelPlaylist
import com.banglalink.toffee.ui.common.CheckedChangeListener
import kotlinx.android.synthetic.main.alert_dialog_add_to_playlist.view.listview
import kotlinx.android.synthetic.main.alert_dialog_add_to_playlist_create.view.*
import java.util.*

class ChannelAddToPlaylistCreateFragment : DialogFragment(), CheckedChangeListener<UgcChannelPlaylist> {
    private val mAdapter: ChannelAddToPlaylistAdapter by lazy { ChannelAddToPlaylistAdapter(this) }

    companion object {
        fun newInstance(items: List<String>): ChannelAddToPlaylistCreateFragment {
            val instance = ChannelAddToPlaylistCreateFragment()
            val bundle = Bundle()
            bundle.putStringArrayList("items", items as ArrayList<String>)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val data = arguments?.getStringArrayList("items") as List<String>
//        mAdapter.addAll(data)
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_add_to_playlist_create, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialogView.listview.adapter = mAdapter
        dialogView.cancelButton.setOnClickListener { alertDialog.dismiss() }
        dialogView.createButton.setOnClickListener { alertDialog.dismiss() }

        return alertDialog
    }

    override fun onCheckedChanged(view: View, item: UgcChannelPlaylist, position: Int, isFromCheckableView: Boolean) {
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