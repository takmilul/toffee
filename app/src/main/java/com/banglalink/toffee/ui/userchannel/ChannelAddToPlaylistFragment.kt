package com.banglalink.toffee.ui.userchannel

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.databinding.AlertDialogAddToPlaylistBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.UgcChannelPlaylist
import com.banglalink.toffee.ui.common.CheckedChangeListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelAddToPlaylistFragment : DialogFragment(), CheckedChangeListener<UgcChannelPlaylist> {
    private var playlistId: Int = 0
    private val mAdapter: ChannelAddToPlaylistAdapter by lazy { ChannelAddToPlaylistAdapter(this) }
    private val viewModel by viewModels<UgcMyChannelAddToPlaylistViewModel>()
    private lateinit var alertDialog: AlertDialog

    companion object {
        private const val CONTENT_ID = "contentId"
        fun newInstance(contentId: Int, items: ArrayList<String>): ChannelAddToPlaylistFragment {
            val instance = ChannelAddToPlaylistFragment()
            val bundle = Bundle()
            bundle.putInt(CONTENT_ID, contentId)
            bundle.putStringArrayList("items", items as ArrayList<String>)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val data = arguments?.getStringArrayList("items") as List<String>
        val contentId = arguments?.getInt(CONTENT_ID) ?: 0
//        mAdapter.addAll(data)
        val binding = AlertDialogAddToPlaylistBinding.inflate(this.layoutInflater)
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_add_to_playlist, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        binding.listview.adapter = mAdapter
        binding.addButton.setOnClickListener {
            /*alertDialog.dismiss()
            val fragment = ChannelAddToPlaylistCreateFragment.newInstance(data)
            fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
            fragment.dialog?.setCanceledOnTouchOutside(true)*/
        }
        binding.doneButton.setOnClickListener {
            observeAddToPlaylist()
            viewModel.addToPlaylist(playlistId, contentId)
        }
        
        return alertDialog
    }

    private fun observeAddToPlaylist() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
            }
        }
    }

    override fun onCheckedChanged(view: View, item: UgcChannelPlaylist, position: Int, isFromCheckableView: Boolean) {
        super.onCheckedChanged(view, item, position, isFromCheckableView)
        when (view) {
            is RadioButton -> {
                if (view.isChecked) {
                    playlistId = 12
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