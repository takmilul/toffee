package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.ReactionFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MyChannelVideosFragment : BaseListFragment<ChannelInfo>(), ContentReactionCallback<ChannelInfo> {

    private var isOwner: Int = 0
    private var channelOwnerId: Int = 0
    private var isPublic: Int = 0
    private var enableToolbar: Boolean = false

    override val itemMargin: Int = 16

    @Inject lateinit var reactionDao: ReactionDao
    override val mAdapter by lazy { MyChannelVideosAdapter(this) }
    private val homeViewModel by activityViewModels<HomeViewModel>()
    @Inject lateinit var viewModelAssistedFactory: MyChannelVideosViewModel.AssistedFactory
    override val mViewModel by viewModels<MyChannelVideosViewModel> { MyChannelVideosViewModel.provideFactory(viewModelAssistedFactory, isOwner, channelOwnerId, isPublic) }

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        private const val IS_OWNER = "isOwner"
        private const val CHANNEL_OWNER_ID = "channelOwnerId"
        private const val IS_PUBLIC = "isPublic"
        fun newInstance(enableToolbar: Boolean, isOwner: Int, channelOwnerId: Int, isPublic: Int): MyChannelVideosFragment {
            val instance = MyChannelVideosFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_OWNER_ID, channelOwnerId)
            bundle.putInt(IS_PUBLIC, isPublic)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isOwner = arguments?.getInt(IS_OWNER) ?: 0
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: 0
        isPublic = arguments?.getInt(IS_PUBLIC) ?: 0
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_videos_empty, "You haven't uploaded any video yet")
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        PopupMenu(requireContext(), view).apply {
            if (isOwner == 1) {
                inflate(R.menu.menu_channel_owner_videos)
                if (item.isApproved == 1) {
                    this.menu.removeItem(R.id.menu_edit_content)
                }
            }
            else {
                inflate(R.menu.menu_channel_videos)
            }
            this.menu.removeItem(R.id.menu_share)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit_content -> {
                        if (findNavController().currentDestination?.id == R.id.myChannelHomeFragment) {
                            val action = MyChannelHomeFragmentDirections.actionMyChannelHomeFragmentToMyChannelVideosEditFragment(item)
                            parentFragment?.findNavController()?.navigate(action)
                        }
                        else {
                            this@MyChannelVideosFragment.findNavController().navigate(R.id.action_menu_channel_to_myChannelVideosEditFragment, Bundle().apply { putParcelable(MyChannelVideosEditFragment.CHANNEL_INFO, item) })
                        }
                    }
                    R.id.menu_add_to_playlist -> {
                        val fragment = MyChannelAddToPlaylistFragment.newInstance(item.id.toInt(), isOwner, channelOwnerId, item)
                        fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
                    }
                    R.id.menu_share -> {
                        homeViewModel.shareContentLiveData.postValue(item)
                    }
                    R.id.menu_fav -> {
                        homeViewModel.updateFavorite(item).observe(viewLifecycleOwner, Observer {
                            handleFavoriteResponse(it)
                        })
                    }
                    /*R.id.menu_not_interested -> {
                        removeItemNotInterestedItem(item)
                        return@setOnMenuItemClickListener true
                    }*/
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }

    }

    fun handleFavoriteResponse(it: Resource<ChannelInfo>) {
        when (it) {
            is Resource.Success -> {
                val channelInfo = it.data
                when (channelInfo.favorite) {
                    "0" -> {
                        context?.showToast("Content successfully removed from favorite list")
                        handleFavoriteRemovedSuccessFully(channelInfo)
                    }
                    "1" -> {
                        handleFavoriteAddedSuccessfully(channelInfo)
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Resource.Failure -> {
                context?.showToast(it.error.msg)
            }
        }
    }

    fun handleFavoriteAddedSuccessfully(channelInfo: ChannelInfo) {
        //subclass can hook here
    }

    fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo) {
        //subclass can hook here
    }

    fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }

    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
//        if (item.isApproved == 0) {
//            Toast.makeText(requireContext(), "Your video has not approved yet. Once it's approved, you can play the video", Toast.LENGTH_SHORT).show()
//        }
//        else {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
//        }
    }

    override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionClicked(view, reactionCountView, item)
        requireActivity().supportFragmentManager.beginTransaction().add(ReactionFragment.newInstance(view, reactionCountView, item), ReactionFragment.TAG).commit()
    }

    /*override fun onReactionLongPressed(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionLongPressed(view, reactionCountView, item)
        requireActivity().supportFragmentManager.beginTransaction().add(ReactionFragment.newInstance(view, reactionCountView, item), ReactionFragment.TAG).commit()
    }*/
    
    override fun onShareClicked(view: View, item: ChannelInfo) {
        super.onShareClicked(view, item)
        homeViewModel.shareContentLiveData.postValue(item)
    }
}