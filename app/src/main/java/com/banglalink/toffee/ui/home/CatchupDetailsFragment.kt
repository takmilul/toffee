package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.AlertDialogReactionFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_catchup.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CatchupDetailsFragment:HomeBaseFragment(), ContentReactionCallback<ChannelInfo> {
    private lateinit var mAdapter: ConcatAdapter
    private lateinit var catchupAdapter: CatchUpDetailsAdapter
    private lateinit var detailsAdapter: ChannelHeaderAdapter
    private lateinit var currentItem: ChannelInfo

    private val viewModel by viewModels<CatchupDetailsViewModel>()

    companion object{
        const val CHANNEL_INFO = "channel_info_"
        fun createInstance(channelInfo: ChannelInfo): CatchupDetailsFragment {
            return CatchupDetailsFragment().apply {
                arguments = Bundle().also {
                    it.putParcelable(CHANNEL_INFO, channelInfo)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentItem = arguments?.getParcelable(CHANNEL_INFO)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catchup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAdapter()
        with(listview) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        observeList()
    }

    private fun initAdapter() {
        catchupAdapter = CatchUpDetailsAdapter(object: BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
            }

            override fun onOpenMenu(view: View, item: ChannelInfo) {
                openMenu(view, item)
            }
        })
        detailsAdapter = ChannelHeaderAdapter(currentItem, this)
        mAdapter = ConcatAdapter(detailsAdapter, catchupAdapter)
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadRelativeContent(currentItem).collectLatest {
                catchupAdapter.submitData(it)
            }
        }
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

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}