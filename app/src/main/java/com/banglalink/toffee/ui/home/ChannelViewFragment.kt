package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.CatchupDetailsListHeaderNewBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.channels.ChannelFragment
import com.banglalink.toffee.ui.common.AlertDialogReactionFragment
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.landing.UserChannelViewModel
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelViewFragment: BaseFragment(), ContentReactionCallback<ChannelInfo> {
    private lateinit var binding: CatchupDetailsListHeaderNewBinding
    private val viewModel by viewModels<UserChannelViewModel>()

    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.catchup_details_list_header_new, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val channelInfo = requireArguments().getParcelable<ChannelInfo>(ARG_CHANNEL_INFO) ?: return
        binding.setVariable(BR.data, channelInfo)
        binding.setVariable(BR.callback, this)
        binding.executePendingBindings()

        if (channelInfo.isLive) {
            binding.channelDetailsGroup.isVisible = false
//            val fragment = parentFragmentManager.findFragmentById(R.id.relatedContainer)
//            Log.e("FRAG", "$fragment")
//            if (fragment !is ChannelFragment) {
                parentFragmentManager.commit {
                    replace(
                        R.id.relatedContainer,
                        ChannelFragment.createInstance(getString(R.string.menu_channel_text))
                    )
                }
//            }
        } else {
            parentFragmentManager.commit { replace(R.id.relatedContainer, CatchupDetailsFragment.createInstance(channelInfo)) }
        }

        /*binding.subscribeButton.setOnClickListener {
            viewModel.toggleSubscriptionStatus(channelInfo.content_provider_id?.toLongOrNull() ?: 0L)
        }

        observe(viewModel.channelSubscriberCount) {
            binding.channelSubCount.text = it
        }

        observe(viewModel.isChannelSubscribed) {
            binding.subscribeButton.setSubscriptionInfo(it)
        }*/

        val contentProviderId =  channelInfo.content_provider_id?.toLongOrNull() ?: 0L
        val isOwner = if(contentProviderId == mPref.customerId.toLong()) 0 else 1
        viewModel.getChannelInfo(isOwner, 1, contentProviderId, contentProviderId.toInt())
    }

    override fun onReactionClicked(view: View, item: ChannelInfo) {
        super.onReactionClicked(view, item)
        AlertDialogReactionFragment.newInstance(view, item)
            .show(requireActivity().supportFragmentManager, "ReactionDialog")
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        openMenu(view, item)
    }
    
    private fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
        if(hideNotInterestedMenuItem(channelInfo)){//we are checking if that could be shown or not
            popupMenu.menu.getItem(2).isVisible = false
        }
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    homeViewModel.shareContentLiveData.postValue(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    homeViewModel.updateFavorite(channelInfo).observe(viewLifecycleOwner, Observer {
                        handleFavoriteResponse(it)
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

    //hook for subclass for to hide Not Interested Menu Item.
    //In catchup details fragment we need to hide this menu from popup menu for first item
    fun hideNotInterestedMenuItem(channelInfo: ChannelInfo):Boolean{
        return false
    }

    fun handleFavoriteResponse(it: Resource<ChannelInfo>){
        when(it){
            is Resource.Success->{
                val channelInfo = it.data
                when(channelInfo.favorite){
                    "0"->{
                        context?.showToast("Content successfully removed from favorite list")
//                        handleFavoriteRemovedSuccessFully(channelInfo)
                    }
                    "1"->{
//                        handleFavoriteAddedSuccessfully(channelInfo)
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Resource.Failure->{
                context?.showToast(it.error.msg)
            }
        }
    }

    /*open fun handleFavoriteAddedSuccessfully(channelInfo: ChannelInfo){
        //subclass can hook here
    }

    open fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo){
        //subclass can hook here
    }

    abstract fun removeItemNotInterestedItem(channelInfo: ChannelInfo)*/
    companion object {
        const val ARG_CHANNEL_INFO = "arg-channel-info"

        fun newInstance(channelInfo: ChannelInfo): ChannelViewFragment {
            val args = Bundle()
            args.putParcelable(ARG_CHANNEL_INFO, channelInfo)
            val fragment = ChannelViewFragment()
            fragment.arguments = args
            return fragment
        }
    }
}