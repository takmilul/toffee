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
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.apiservice.MyChannelPlaylistParams
import com.banglalink.toffee.databinding.AlertDialogAddToPlaylistBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.UgcChannelPlaylist
import com.banglalink.toffee.ui.common.CheckedChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelAddToPlaylistFragment : DialogFragment(), CheckedChangeListener<UgcChannelPlaylist> {

    private var contentId: Int = 0
    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var playlistId: Int = 0
    private val mAdapter: ChannelAddToPlaylistAdapter by lazy { ChannelAddToPlaylistAdapter(this) }
    private val viewModel by viewModels<UgcMyChannelAddToPlaylistViewModel>()

    private val createPlaylistViewModel by viewModels<UgcMyChannelCreatePlaylistViewModel>()

    @Inject
    lateinit var viewModelAssistedFactory: ChannelPlaylistViewModel.AssistedFactory
    private val playlistViewModel by viewModels<ChannelPlaylistViewModel> {
        ChannelPlaylistViewModel.provideFactory(viewModelAssistedFactory,
            MyChannelPlaylistParams(isOwner, channelId))
    }

    private lateinit var alertDialog: AlertDialog

    companion object {
        private const val CONTENT_ID = "contentId"
        private const val IS_OWNER = "isOwner"
        private const val CHANNEL_ID = "channelId"
        fun newInstance(contentId: Int, isOwner: Int, channelId: Int): ChannelAddToPlaylistFragment {
            val instance = ChannelAddToPlaylistFragment()
            val bundle = Bundle()
            bundle.putInt(CONTENT_ID, contentId)
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_ID, channelId)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isOwner = arguments?.getInt(IS_OWNER) ?: 0
        channelId = arguments?.getInt(CHANNEL_ID) ?: 0

    }

    private fun observePlaylist() {
        lifecycleScope.launchWhenStarted {
            playlistViewModel.getListData().collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        contentId = arguments?.getInt(CONTENT_ID) ?: 0
        val binding = AlertDialogAddToPlaylistBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        binding.listview.adapter = mAdapter
        binding.viewModel = createPlaylistViewModel
        observePlaylist()
        binding.addButton.setOnClickListener {
            binding.addToPlaylistGroup.visibility = View.GONE
            binding.createPlaylistGroup.visibility = View.VISIBLE
        }
        binding.doneButton.setOnClickListener { addToPlaylist() }
        binding.cancelButton.setOnClickListener { alertDialog.dismiss() }
        binding.createButton.setOnClickListener {
            createPlaylist()
        }

        return alertDialog
    }

    private fun createPlaylist() {
        if (!createPlaylistViewModel.playlistName.isNullOrEmpty()) {
            observeCreatePlaylist()
            createPlaylistViewModel.createPlaylist(isOwner, channelId)
        } else {
            Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.liveData) {
            when (it) {
                is Success -> {
                    playlistId = it.data.playlistNameId
                    addToPlaylist()
                    //Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addToPlaylist() {
        if (mAdapter.selectedPosition < 0 && playlistId == 0) {
            Toast.makeText(requireContext(), "Please select a playlist", Toast.LENGTH_SHORT).show()
        } else {
            if (mAdapter.selectedPosition >= 0) {
                val selectedItem = mAdapter.getItemByIndex(mAdapter.selectedPosition)
                playlistId = selectedItem!!.id
            }
            observeAddToPlaylist()
            viewModel.addToPlaylist(playlistId, contentId)
        }
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