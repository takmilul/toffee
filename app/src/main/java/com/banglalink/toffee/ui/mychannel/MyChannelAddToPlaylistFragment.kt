package com.banglalink.toffee.ui.mychannel

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.BrowsingScreens
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelAddToPlaylistFragment : DialogFragment(), CheckedChangeListener<MyChannelPlaylist>, OnClickListener {
    
    private var channelOwnerId: Int = 0
    private var playlistId: Int = 0
    private lateinit var channelInfo: ChannelInfo
    private var isUserPlaylist:Int = 0
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
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        const val CHANNEL_INFO = "channelInfo"
        const val IS_USER_PLAYLIST="isUserPlaylist"
        
        fun newInstance(channelId: Int, channelInfo: ChannelInfo, isUserPlaylist: Int=0): MyChannelAddToPlaylistFragment {
            return MyChannelAddToPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHANNEL_OWNER_ID, channelId)
                    putParcelable(CHANNEL_INFO, channelInfo)
                    putInt(IS_USER_PLAYLIST,isUserPlaylist)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelInfo = arguments?.getParcelable(CHANNEL_INFO)!!
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: mPref.customerId
        isUserPlaylist= arguments?.getInt(IS_USER_PLAYLIST) ?: 0
    }
    
    private fun observePlaylist() {
        lifecycleScope.launch {
            playlistViewModel.getMyChannelUserPlaylists(channelOwnerId).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AlertDialogMyChannelAddToPlaylistBinding.inflate(this.layoutInflater)
        binding.progressBar.load(R.drawable.content_loader)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_PLAYLISTS)
        with(binding) {
            var isInitialized = false
            lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                    emptyViewLabel.isVisible = isEmpty && !isLoading
                    progressBar.isVisible = isLoading
                    listview.isVisible = !isEmpty && !isLoading
                    isInitialized = true
                }
            }
            listview.adapter = mAdapter
            viewModel = createPlaylistViewModel
            listview.setHasFixedSize(true)
            addButton.safeClick(this@MyChannelAddToPlaylistFragment)
            doneButton.safeClick(this@MyChannelAddToPlaylistFragment)
            cancelButton.safeClick(this@MyChannelAddToPlaylistFragment)
            createButton.safeClick(this@MyChannelAddToPlaylistFragment)
            closeIv.safeClick(this@MyChannelAddToPlaylistFragment)
        }
        observePlaylist()
        return alertDialog
    }
    
    override fun onClick(v: View?) {
        when (v) {
            binding.addButton -> {
                binding.addToPlaylistGroup.visibility = View.GONE
                binding.createPlaylistGroup.visibility = View.VISIBLE
            }
            binding.doneButton -> {
                addToPlaylist(false)
                binding.playlistNameEditText.text.clear()
            }
            binding.cancelButton -> {
                alertDialog.dismiss()
                binding.playlistNameEditText.text.clear()
            }
            binding.createButton -> createPlaylist()
            binding.closeIv -> {
                alertDialog.dismiss()
                binding.playlistNameEditText.text.clear()
            }
        }
    }
    
    private fun createPlaylist() {
        if (!createPlaylistViewModel.playlistName.isNullOrBlank()) {
            observeCreatePlaylist()
            createPlaylistViewModel.createPlaylist(channelOwnerId,isUserPlaylist)
        } else {
            requireContext().showToast(getString(string.playlist_name_empty_msg))
        }
    }
    
    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.createPlaylistLiveData) {
            when (it) {
                is Success -> {
                    if (it.data == null) {
                        requireContext().showToast(getString(string.try_again_message))
                    } else {
                        playlistId = it.data?.playlistNameId ?: 0
                        if (isUserPlaylist == 1) cacheManager.clearCacheByUrl(ApiRoutes.GET_USER_PLAYLISTS)
                        addToPlaylist(true)
                    }
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.CREATE_PLAYLIST,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.MY_CHANNEL_PLAYLIST_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun addToPlaylist(isCreate: Boolean) {
        if (mAdapter.selectedPosition < 0 && playlistId == 0) {
            requireContext().showToast(getString(string.select_playlist_msg))
        } else {
            var isAlreadyAdded = false
            if (mAdapter.selectedPosition >= 0 && !isCreate) {
                val selectedItem = mAdapter.getItemByIndex(mAdapter.selectedPosition)
                playlistId = selectedItem!!.id
                isAlreadyAdded = selectedItem.playlistContentIdList?.contains(MyChannelPlaylistContentId(channelInfo.id)) ?: false
            }
            if (isAlreadyAdded) {
                requireContext().showToast(getString(string.duplicate_playlist_msg))
            } else {
                observeAddToPlaylist()
                viewModel.addToPlaylist(playlistId, channelInfo.id.toInt(), channelOwnerId, isUserPlaylist)
                viewModel.insertActivity(channelInfo, Reaction.Add.value)
            }
        }
    }
    
    private fun observeAddToPlaylist() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    alertDialog.dismiss()
                    if (it.data == null) {
                        requireContext().showToast(getString(string.try_again_message))
                    } else {
                        requireContext().showToast(it.data?.message)
                        if (isUserPlaylist == 1) {
                            cacheManager.clearCacheByUrl(ApiRoutes.GET_USER_PLAYLISTS)
                            cacheManager.clearCacheByUrl(ApiRoutes.GET_USER_PLAYLIST_VIDEOS)
                        } else {
                            cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_PLAYLISTS)
                            cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_PLAYLIST_VIDEOS)
                        }
                        playlistReloadViewModel.reloadPlaylist.value = true
                    }
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.ADD_CONTENT_TO_PLAYLIST,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.MY_CHANNEL_PLAYLIST_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
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

    override fun onDestroyView() {
        binding.listview.adapter = null
        super.onDestroyView()
        _binding = null
    }
}