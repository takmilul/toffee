package com.banglalink.toffee.ui.mychannel

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_PLAYLISTS
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_PLAYLIST_VIDEOS
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
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
    
    private var channelOwnerId: Int = 0
    private var playlistId: Int = 0
    private lateinit var channelInfo: ChannelInfo
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    private var _binding: AlertDialogMyChannelAddToPlaylistBinding ? = null
    private val binding get() = _binding!!
    private val mAdapter: MyChannelAddToPlaylistAdapter by lazy { MyChannelAddToPlaylistAdapter(this) }
    private val viewModel by viewModels<MyChannelAddToPlaylistViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val playlistViewModel by viewModels<MyChannelPlaylistViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    
    private lateinit var alertDialog: AlertDialog
    
    companion object {
        private const val CHANNEL_OWNER_ID = "channelOwnerId"
        private const val CHANNEL_INFO = "channelInfo"
        
        fun newInstance(channelId: Int, channelInfo: ChannelInfo): MyChannelAddToPlaylistFragment {
            return MyChannelAddToPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHANNEL_OWNER_ID, channelId)
                    putParcelable(CHANNEL_INFO, channelInfo)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelInfo = arguments?.getParcelable(CHANNEL_INFO)!!
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: mPref.customerId
    }
    
    private fun observePlaylist() {
        lifecycleScope.launchWhenStarted {
            playlistViewModel.getMyChannelPlaylists(channelOwnerId).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        
        _binding = AlertDialogMyChannelAddToPlaylistBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        binding.listview.adapter = mAdapter
        binding.viewModel = createPlaylistViewModel
        cacheManager.clearCacheByUrl(GET_MY_CHANNEL_PLAYLISTS)
        observePlaylist()
        binding.addButton.safeClick(this)
        binding.doneButton.safeClick(this)
        binding.cancelButton.safeClick(this)
        binding.createButton.safeClick(this)
        binding.closeIv.safeClick(this)
        return alertDialog
    }
    
    override fun onClick(v: View?) {
        when (v) {
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

    override fun onDestroyView() {
        binding.listview.adapter = null
        super.onDestroyView()
        _binding = null
    }
    
    private fun createPlaylist() {
        if (!createPlaylistViewModel.playlistName.isNullOrBlank()) {
            observeCreatePlaylist()
            createPlaylistViewModel.createPlaylist(channelOwnerId)
        } else {
           // requireContext().showToast("Please give a playlist name")
            requireContext().showToast("Playlist name empty!")
        }
    }
    
    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.createPlaylistLiveData) {
            when (it) {
                is Success -> {
                    playlistId = it.data.playlistNameId
                    addToPlaylist(true)
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun addToPlaylist(isCreate: Boolean) {
        if (mAdapter.selectedPosition < 0 && playlistId == 0) {
            requireContext().showToast("Please select a playlist")
        } else {
            var isAlreadyAdded = false
            if (mAdapter.selectedPosition >= 0 && !isCreate) {
                val selectedItem = mAdapter.getItemByIndex(mAdapter.selectedPosition)
                playlistId = selectedItem!!.id
                isAlreadyAdded = selectedItem.playlistContentIdList?.contains(MyChannelPlaylistContentId(channelInfo.id)) ?: false
            }
            if (isAlreadyAdded) {
                requireContext().showToast("This content is already added in this playlist")
            } else {
                observeAddToPlaylist()
                viewModel.addToPlaylist(playlistId, channelInfo.id.toInt(), channelOwnerId)
                viewModel.insertActivity(channelInfo, Reaction.Add.value)
            }
        }
    }
    
    private fun observeAddToPlaylist() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    alertDialog.dismiss()
                    requireContext().showToast(it.data.message)
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_PLAYLIST_VIDEOS)
                    playlistReloadViewModel.reloadPlaylist.value = true
                }
                is Failure -> {
                    alertDialog.dismiss()
                    requireContext().showToast(it.error.msg)
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