package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.ReactionFragment
import com.banglalink.toffee.ui.home.CatchupDetailsViewModel
import com.banglalink.toffee.ui.home.ChannelHeaderAdapter
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosViewModel.AssistedFactory
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.catchup_details_list_header_new.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistVideosFragment : BaseListFragment<ChannelInfo>(),
    MyChannelPlaylistItemListener {
    private var currentItem: ChannelInfo? = null
    private var enableToolbar: Boolean = false
    private lateinit var requestParams: MyChannelPlaylistContentParam
    private var detailsAdapter: ChannelHeaderAdapter? = null
    private lateinit var args: MyChannelPlaylistVideosFragmentArgs
    override val mAdapter by lazy { MyChannelPlaylistVideosAdapter(this , currentItem) }
    @Inject lateinit var viewModelAssistedFactory: AssistedFactory
    override val mViewModel by viewModels<MyChannelPlaylistVideosViewModel>{MyChannelPlaylistVideosViewModel.provideAssisted(viewModelAssistedFactory, requestParams)}
    private val playerViewModel by viewModels<CatchupDetailsViewModel>()
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

    fun getPlaylistId(): Long  = args.playlistInfo.getPlaylistIdLong()

    fun isAutoplayEnabled(): Boolean {
        return binding.root.findViewById<SwitchMaterial>(R.id.autoplay_switch).isChecked
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = MyChannelPlaylistVideosFragmentArgs.fromBundle(requireArguments())
        requestParams = MyChannelPlaylistContentParam(args.playlistInfo.channelOwnerId, args.playlistInfo.isOwner, args.playlistInfo.playlistId)
        Log.i("UGC_Owner", "isOwner: ${requestParams.isOwner}")
        currentItem = args.playlistInfo.currentItem
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        binding.topPanel.root.visibility = View.VISIBLE
//        binding.topPanel.statusText.text = "${args.playlistInfo.playlistName} (${args.playlistInfo.playlistItemCount})"
        
        observe(playerViewModel.channelSubscriberCount) {
            currentItem?.isSubscribed = if (playerViewModel.isChannelSubscribed.value!!) 1 else 0
            currentItem?.subscriberCount = it
            detailsAdapter?.notifyDataSetChanged()
        }
        
        setSubscriptionStatus()

        detailsAdapter = ChannelHeaderAdapter(args.playlistInfo, object: ContentReactionCallback<ChannelInfo> {
            override fun onOpenMenu(view: View, item: ChannelInfo) {
                openMenu(view, item)
            }

            override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
                requireActivity().supportFragmentManager.beginTransaction().add(ReactionFragment.newInstance(view, reactionCountView, item, true), ReactionFragment.TAG).commit()
            }

            override fun onReactionLongPressed(view: View, reactionCountView: View, item: ChannelInfo) {
                super.onReactionLongPressed(view, reactionCountView, item)
                requireActivity().supportFragmentManager.beginTransaction().add(ReactionFragment.newInstance(view, reactionCountView, item), ReactionFragment.TAG).commit()
            }
            
            override fun onShareClicked(view: View, item: ChannelInfo) {
                homeViewModel.shareContentLiveData.postValue(item)
            }

            override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
                playerViewModel.toggleSubscriptionStatus(item.id.toInt(), item.channel_owner_id)
            }
            
            override fun onProviderIconClicked(item: ChannelInfo) {
                super.onProviderIconClicked(item)
                landingViewModel.navigateToMyChannel(this@MyChannelPlaylistVideosFragment, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
            }
        })
        super.onViewCreated(view, savedInstanceState)

        observeListState()
    }

    private fun setSubscriptionStatus() {
        currentItem?.let { channelInfo ->
            val customerId = mPref.customerId
            val isOwner = if (channelInfo.channel_owner_id == customerId) 1 else 0
            val isPublic = if (channelInfo.channel_owner_id == customerId) 0 else 1
            val channelId = channelInfo.channel_owner_id.toLong()
            playerViewModel.getChannelInfo(isOwner, isPublic, channelId, channelId.toInt())
        }
    }

    private fun observeListState() {
        lifecycleScope.launch {
            mAdapter
                .loadStateFlow
                .distinctUntilChangedBy {
                    it.refresh
                }.collect {
                    val list = mAdapter.snapshot()
                    homeViewModel.addToPlayListMutableLiveData.postValue(
                        AddToPlaylistData(getPlaylistId(), list.items, false)
                    )
                }
        }
    }

    override fun getRecyclerAdapter(): RecyclerView.Adapter<*> {
        return ConcatAdapter(detailsAdapter, mAdapter)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        landingViewModel.navigateToMyChannel(this, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
    }

    override fun onItemClickAtPosition(position: Int, item: ChannelInfo) {
        if(item == currentItem || item.id == currentItem?.id) {
            return
        }
        if (item.isApproved == 0) {
            Toast.makeText(requireContext(), "Your video has not approved yet. Once it's approved, you can play the video", Toast.LENGTH_SHORT).show()
        } else {
            homeViewModel.addToPlayListMutableLiveData.postValue(
                AddToPlaylistData(getPlaylistId(), mAdapter.snapshot().items)
            )
            homeViewModel.fragmentDetailsMutableLiveData.postValue(args.playlistInfo.apply {
                playIndex = position
                currentItem = item
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
            is Success->{
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
            is Failure->{
                context?.showToast(it.error.msg)
            }
        }
    }

    fun setCurrentChannel(channelInfo: ChannelInfo?) {
        currentItem = channelInfo
        detailsAdapter?.setChannelInfo(channelInfo)
        mAdapter.setSelectedItem(channelInfo)
        setSubscriptionStatus()
    }
}