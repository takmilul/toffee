package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.AlertDialogReactionFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MyChannelVideosFragment : BaseListFragment<ChannelInfo>(), ContentReactionCallback<ChannelInfo> {

    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var enableToolbar: Boolean = false

    @Inject
    lateinit var reactionDao: ReactionDao

    override val mAdapter by lazy { MyChannelVideosAdapter(this) }

    @Inject lateinit var viewModelAssistedFactory: MyChannelVideosViewModel.AssistedFactory
    override val mViewModel by viewModels<MyChannelVideosViewModel> { MyChannelVideosViewModel.provideFactory(viewModelAssistedFactory, isOwner, channelId) }
    
    private val homeViewModel by activityViewModels<HomeViewModel>()

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        private const val IS_OWNER = "isOwner"
        private const val CHANNEL_ID = "channelId"
        fun newInstance(enableToolbar: Boolean, isOwner: Int, channelId: Int): MyChannelVideosFragment {
            val instance = MyChannelVideosFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
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
    
    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_videos_empty, "You haven't uploaded any video yet")
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        PopupMenu(requireContext(), view).apply {
            if (isOwner == 1){
                inflate(R.menu.menu_channel_owner_videos)
            }
            else {
                inflate(R.menu.menu_channel_videos)
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_add_to_playlist -> {
                        val fragment = MyChannelAddToPlaylistFragment.newInstance(item.id.toInt(), isOwner, channelId)
                        fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_share->{
                        homeViewModel.shareContentLiveData.postValue(item)
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_fav->{
                        homeViewModel.updateFavorite(item).observe(viewLifecycleOwner, Observer {
                            handleFavoriteResponse(it)
                        })
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_not_interested->{
                        removeItemNotInterestedItem(item)
                        return@setOnMenuItemClickListener true
                    }
                    else->{
                        return@setOnMenuItemClickListener false
                    }
                }
            }
            show()
        }
        
    }

    fun handleFavoriteResponse(it: Resource<ChannelInfo>){
        when(it){
            is Resource.Success->{
                val channelInfo = it.data
                when(channelInfo.favorite){
                    "0"->{
                        context?.showToast("Content successfully removed from favorite list")
                        handleFavoriteRemovedSuccessFully(channelInfo)
                    }
                    "1"->{
                        handleFavoriteAddedSuccessfully(channelInfo)
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Resource.Failure->{
                context?.showToast(it.error.msg)
            }
        }
    }

    fun handleFavoriteAddedSuccessfully(channelInfo: ChannelInfo){
        //subclass can hook here
    }

    fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo){
        //subclass can hook here
    }

    fun removeItemNotInterestedItem(channelInfo: ChannelInfo){
        
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }
    
    override fun onReactionClicked(view: View, item: ChannelInfo) {
        super.onReactionClicked(view, item)
        val fragment = AlertDialogReactionFragment.newInstance()
        fragment.setItem(view, item)
        fragment.show(requireActivity().supportFragmentManager, "ReactionDialog")
    }
}