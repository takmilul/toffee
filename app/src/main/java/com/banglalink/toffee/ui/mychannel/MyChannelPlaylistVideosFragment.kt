package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosViewModel.AssistedFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistVideosFragment : BaseListFragment<ChannelInfo>(), BaseListItemCallback<ChannelInfo> {
    
    private var enableToolbar: Boolean = false
    private lateinit var requestParams: MyChannelPlaylistContentParam
    override val mAdapter by lazy { MyChannelPlaylistVideosAdapter(this) }
    @Inject lateinit var viewModelAssistedFactory: AssistedFactory
    override val mViewModel by viewModels<MyChannelPlaylistVideosViewModel>{MyChannelPlaylistVideosViewModel.provideAssisted(viewModelAssistedFactory, requestParams)}

    private val homeViewModel by activityViewModels<HomeViewModel>()

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        fun newInstance(enableToolbar: Boolean): MyChannelPlaylistVideosFragment {
            val instance = MyChannelPlaylistVideosFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        } 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = MyChannelPlaylistVideosFragmentArgs.fromBundle(requireArguments())
        requestParams = MyChannelPlaylistContentParam(args.channelOwnerId, args.isOwner, args.playlistId)
        Log.i("UGC_Owner", "isOwner: ${requestParams.isOwner}")
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        if (item.isApproved == 0) {
            Toast.makeText(requireContext(), "Your video has not approved yet. Once it's approved, you can play the video", Toast.LENGTH_SHORT).show()
        } else {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
        }
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        if (requestParams.isOwner == 1) {
            PopupMenu(requireContext(), view).apply {
                inflate(R.menu.menu_delete_playlist_video)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_delete_playlist_video -> {
                            observeDeletePlaylistVideo()
                            mViewModel.deletePlaylistVideo(requestParams.channelOwnerId, item.playlistContentId, requestParams.playlistId)
                            mViewModel.insertActivity(item, Reaction.Delete.value)
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
    }

    private fun observeDeletePlaylistVideo() {
        observe(mViewModel.deletePlaylistVideoLiveData){
            when(it){
                is Success -> {
                    mAdapter.refresh()
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}