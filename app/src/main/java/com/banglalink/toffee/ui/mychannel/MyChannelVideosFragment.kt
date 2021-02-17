package com.banglalink.toffee.ui.mychannel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_VIDEOS_URL
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.about.AboutActivity
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.common.ReactionIconCallback
import com.banglalink.toffee.ui.common.ReactionPopup
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_my_channel_videos_empty_view.*
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelVideosFragment : BaseListFragment<ChannelInfo>(), ContentReactionCallback<ChannelInfo> {

    private var isOwner: Int = 0
    private var channelOwnerId: Int = 0
    private var isPublic: Int = 0
    private var enableToolbar: Boolean = false

    override val itemMargin: Int = 16

    @Inject lateinit var reactionDao: ReactionDao
    @Inject lateinit var cacheManager: CacheManager
    override val mAdapter by lazy { MyChannelVideosAdapter(this) }
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val videosReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
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
        observeReloadVideos()
        observeDeleteVideo()
    }

    override fun setEmptyView() {
        val customView = layoutInflater.inflate(R.layout.layout_my_channel_videos_empty_view, null)
        with(binding.emptyView) {
            removeAllViews()
            addView(customView)
            gravity = Gravity.CENTER_HORIZONTAL
            visibility = View.VISIBLE
            if (isOwner == 1){
                empty_view_label.text = "You haven't uploaded any video yet"
                uploadVideoButton.setOnClickListener { 
                    if(requireActivity() is HomeActivity){
                        (requireActivity() as HomeActivity).showUploadDialog()
                    }
                }
                creatorsPolicyButton.setOnClickListener {
                    val intent = Intent(requireActivity(), HtmlPageViewActivity::class.java).apply {
                        putExtra(HtmlPageViewActivity.CONTENT_KEY, AboutActivity.PRIVACY_POLICY_URL)
                        putExtra(HtmlPageViewActivity.TITLE_KEY, "Creators Policy")
                    }
                    requireActivity().startActivity(intent)
                }
            }
            else{
                uploadVideoButton.hide()
                creatorsPolicyButton.hide()
                empty_view_label.text = "This channel has no video yet"
            }
        }
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        PopupMenu(requireContext(), view).apply {
            if (isOwner == 1) {
                inflate(R.menu.menu_channel_owner_videos)
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
                    R.id.menu_delete_content -> {
                        showDeleteVideoDialog(item.id.toInt())
                    }
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }

    }

    private fun showDeleteVideoDialog(contentId: Int){
        VelBoxAlertDialogBuilder(
            requireContext(),
            text = "Are you sure to delete?",
            positiveButtonTitle = "No",
            negativeButtonTitle = "Delete",
            positiveButtonListener = { it?.dismiss() },
            negativeButtonListener = { 
                mViewModel.deleteVideo(contentId)
                it?.dismiss()
            }
        ).create().show()
    }
    
    private fun handleFavoriteResponse(it: Resource<ChannelInfo>) {
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

    private fun handleFavoriteAddedSuccessfully(channelInfo: ChannelInfo) {
        //subclass can hook here
    }

    private fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo) {
        //subclass can hook here
    }

    fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }

    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionClicked(view, reactionCountView, item)
        val iconLocation = IntArray(2)
        view.getLocationOnScreen(iconLocation)
        val reactionPopupFragment = ReactionPopup.newInstance(item, iconLocation, view.height).apply { setCallback(object : ReactionIconCallback {
            override fun onReactionChange(reactionCount: String, reactionText: String, reactionIcon: Int) {
                (reactionCountView as TextView).text = reactionCount
                (view as TextView).text = reactionText
                view.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, 0, 0, 0)
                if (reactionText == Love.name) {
                    view.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                }
                else{
                    view.setTextColor(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color))
                }
                Log.e(ReactionPopup.TAG, "setReaction: icon", )
            }
        })
        }
        childFragmentManager.commit { add(reactionPopupFragment, ReactionPopup.TAG) }
    }

    override fun onShareClicked(view: View, item: ChannelInfo) {
        super.onShareClicked(view, item)
        homeViewModel.shareContentLiveData.postValue(item)
    }

    private fun observeReloadVideos() {
        observe(videosReloadViewModel.reloadPlaylist) {
            if (it) {
                reloadVideosList()
            }
        }
    }
    
    private fun observeDeleteVideo(){
        observe(mViewModel.deleteVideoLiveData){
            when(it){
                is Success -> {
                    requireContext().showToast(it.data.message)
                    reloadVideosList()
                }
                is Failure -> requireContext().showToast(it.error.msg)
            }
        }
    }
    
    private fun reloadVideosList(){
        cacheManager.clearCacheByUrl(GET_MY_CHANNEL_VIDEOS_URL)
        mAdapter.refresh()
    }
}