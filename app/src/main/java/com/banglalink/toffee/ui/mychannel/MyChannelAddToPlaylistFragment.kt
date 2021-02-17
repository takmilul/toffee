package com.banglalink.toffee.ui.mychannel

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.databinding.AlertDialogMyChannelAddToPlaylistBinding
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.model.MyChannelPlaylistContentId
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.CheckedChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelAddToPlaylistFragment : DialogFragment(), CheckedChangeListener<MyChannelPlaylist>, OnClickListener {

    private var contentId: Int = 0
    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var playlistId: Int = 0
    private lateinit var channelInfo: ChannelInfo
    private lateinit var binding: AlertDialogMyChannelAddToPlaylistBinding
    private val mAdapter: MyChannelAddToPlaylistAdapter by lazy { MyChannelAddToPlaylistAdapter(this) }
    private val viewModel by viewModels<MyChannelAddToPlaylistViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    @Inject lateinit var viewModelAssistedFactory: MyChannelPlaylistViewModel.AssistedFactory
    private val playlistViewModel by viewModels<MyChannelPlaylistViewModel>{
        MyChannelPlaylistViewModel.provideFactory(viewModelAssistedFactory, isOwner, channelId)
    }
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()

    private lateinit var alertDialog: AlertDialog

    companion object {
        private const val CONTENT_ID = "contentId"
        private const val IS_OWNER = "isOwner"
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_INFO = "channelInfo"
        fun newInstance(contentId: Int, isOwner: Int, channelId: Int, channelInfo: ChannelInfo): MyChannelAddToPlaylistFragment {
            val instance = MyChannelAddToPlaylistFragment()
            val bundle = Bundle()
            bundle.putInt(CONTENT_ID, contentId)
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_ID, channelId)
            bundle.putParcelable(CHANNEL_INFO, channelInfo)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isOwner = arguments?.getInt(IS_OWNER) ?: 0
        channelId = arguments?.getInt(CHANNEL_ID) ?: 0
        
        channelId = if (isOwner == 0) 0 else channelId
        channelInfo = arguments?.getParcelable(CHANNEL_INFO)!!
    }

    private fun observePlaylist() {
        lifecycleScope.launchWhenStarted {
            playlistViewModel.getListData.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        contentId = arguments?.getInt(CONTENT_ID) ?: 0
        binding = AlertDialogMyChannelAddToPlaylistBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        binding.listview.adapter = mAdapter
        binding.viewModel = createPlaylistViewModel
        observePlaylist()
        binding.addButton.safeClick(this)
        binding.doneButton.safeClick(this)
        binding.cancelButton.safeClick(this)
        binding.createButton.safeClick(this)
        binding.closeIv.safeClick(this)
        return alertDialog
    }

    override fun onClick(v: View?) {
        when(v){
            binding.addButton -> {
                binding.addToPlaylistGroup.visibility = View.GONE
                binding.createPlaylistGroup.visibility = View.VISIBLE
            }
            binding.doneButton -> addToPlaylist(false)
            binding.cancelButton -> alertDialog.dismiss()
            binding.createButton -> createPlaylist()
            binding.closeIv -> alertDialog.dismiss()
        }
    }

    private fun createPlaylist() {
        if (!createPlaylistViewModel.playlistName.isNullOrBlank()) {
            observeCreatePlaylist()
            createPlaylistViewModel.createPlaylist(isOwner, channelId)
        } else {
            Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.createPlaylistLiveData) {
            when (it) {
                is Success -> {
                    playlistId = it.data.playlistNameId
                    addToPlaylist(true)
                    //Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addToPlaylist(isCreate: Boolean) {
        if (mAdapter.selectedPosition < 0 && playlistId == 0) {
            Toast.makeText(requireContext(), "Please select a playlist", Toast.LENGTH_SHORT).show()
        } else {
            var isAlreadyAdded = false
            if (mAdapter.selectedPosition >= 0 && !isCreate) {
                val selectedItem = mAdapter.getItemByIndex(mAdapter.selectedPosition)
                playlistId = selectedItem!!.id
                isAlreadyAdded = selectedItem.playlistContentIdList?.contains(MyChannelPlaylistContentId(contentId.toString())) ?: false
            }
            if (isAlreadyAdded) {
                requireContext().showToast("This content is already added in this playlist")
            }
            else {
                observeAddToPlaylist()
                viewModel.addToPlaylist(playlistId, contentId, channelId, isOwner)
                viewModel.insertActivity(channelInfo, Reaction.Add.value)
            }
        }
    }

    private fun observeAddToPlaylist() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    alertDialog.dismiss()
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    playlistReloadViewModel.reloadPlaylist.postValue(true)
                }
                is Failure -> {
                    alertDialog.dismiss()
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCheckedChanged(view: View, item: MyChannelPlaylist, position: Int, isFromCheckableView: Boolean) {
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