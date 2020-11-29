package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.AlertDialogReactionFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.home.ChannelHeaderAdapter
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosViewModel.AssistedFactory
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistVideosFragment : BaseListFragment<ChannelInfo>(),
    ContentReactionCallback<ChannelInfo> {
    private var currentItem: ChannelInfo? = null
    private var enableToolbar: Boolean = false
    private lateinit var requestParams: MyChannelPlaylistContentParam
    private var detailsAdapter: ChannelHeaderAdapter? = null
    private lateinit var args: MyChannelPlaylistVideosFragmentArgs
    override val mAdapter by lazy { MyChannelPlaylistVideosAdapter(this) }
    @Inject lateinit var viewModelAssistedFactory: AssistedFactory
    override val mViewModel by viewModels<MyChannelPlaylistVideosViewModel>{MyChannelPlaylistVideosViewModel.provideAssisted(viewModelAssistedFactory, requestParams)}

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val landingViewModel by activityViewModels<LandingPageViewModel>()

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        fun newInstance(
            info: PlaylistPlaybackInfo,
            enableToolbar: Boolean = false
        ): MyChannelPlaylistVideosFragment {
            val instance = MyChannelPlaylistVideosFragment()
            val bundle = Bundle()
            bundle.putParcelable("playlistInfo", info)
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        } 
    }

    fun getPlaylistId(): Int {
        return requestParams.playlistId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = MyChannelPlaylistVideosFragmentArgs.fromBundle(requireArguments())
        requestParams = MyChannelPlaylistContentParam(args.playlistInfo.channelOwnerId, args.playlistInfo.isOwner, args.playlistInfo.playlistId)
        Log.i("UGC_Owner", "isOwner: ${requestParams.isOwner}")
        currentItem = args.playlistInfo.channelInfo
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        binding.topPanel.root.visibility = View.VISIBLE
//        binding.topPanel.statusText.text = "${args.playlistInfo.playlistName} (${args.playlistInfo.playlistItemCount})"

        detailsAdapter = ChannelHeaderAdapter(currentItem, object: ContentReactionCallback<ChannelInfo> {
            override fun onOpenMenu(view: View, item: ChannelInfo) {
                openMenu(view, item)
            }

            override fun onReactionClicked(view: View, item: ChannelInfo) {
                AlertDialogReactionFragment.newInstance(view, item)
                    .show(requireActivity().supportFragmentManager, "ReactionDialog")
            }

            override fun onShareClicked(view: View, item: ChannelInfo) {
                homeViewModel.shareContentLiveData.postValue(item)
            }

            override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
//                viewModel.toggleSubscriptionStatus(item.channel_owner_id, item.channel_owner_id)
            }
        })
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getRecyclerAdapter(): RecyclerView.Adapter<*> {
        return ConcatAdapter(detailsAdapter, mAdapter)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        landingViewModel.navigateToMyChannel(this, item.channel_owner_id, item.isSubscribed)
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        if (item.isApproved == 0) {
            Toast.makeText(requireContext(), "Your video has not approved yet. Once it's approved, you can play the video", Toast.LENGTH_SHORT).show()
        } else {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(args.playlistInfo.apply {
                channelInfo = item
            })
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
        } else {
            openMenu(view, item)
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

    private fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        }
        else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }

        popupMenu.menu.findItem(R.id.menu_share).isVisible = false
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    homeViewModel.shareContentLiveData.postValue(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    homeViewModel.updateFavorite(channelInfo).observe(viewLifecycleOwner, { resp->
                        handleFavoriteResponse(resp)
                    })
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_not_interested->{
//                    removeItemNotInterestedItem(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }

    private fun handleFavoriteResponse(it: Resource<ChannelInfo>){
        when(it){
            is Resource.Success->{
                val channelInfo = it.data
                when(channelInfo.favorite){
                    "0"->{
                        context?.showToast("Content successfully removed from favorite list")
                    }
                    "1"->{
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Resource.Failure->{
                context?.showToast(it.error.msg)
            }
        }
    }

    fun setCurrentChannel(channelInfo: ChannelInfo?) {
        detailsAdapter?.setChannelInfo(channelInfo)
    }
}