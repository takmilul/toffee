package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosViewModel.AssistedFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistVideosFragment : BaseListFragment<ChannelInfo>(), BaseListItemCallback<ChannelInfo> {
    
    private var enableToolbar: Boolean = false
    private lateinit var requestParams: MyChannelPlaylistContentParam
    override val mAdapter by lazy { MyChannelPlaylistVideosAdapter(this) }
    @Inject lateinit var viewModelAssistedfactory: AssistedFactory
    override val mViewModel by viewModels<MyChannelPlaylistVideosViewModel>{MyChannelPlaylistVideosViewModel.provideAssisted(viewModelAssistedfactory, requestParams)}
    
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
        requestParams = MyChannelPlaylistContentParam(args.channelId, args.isOwner, args.playlistId)
    }
}