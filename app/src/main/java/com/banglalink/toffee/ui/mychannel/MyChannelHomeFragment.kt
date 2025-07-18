package com.banglalink.toffee.ui.mychannel

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
import com.banglalink.toffee.R
import com.banglalink.toffee.R.color
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.databinding.AlertDialogMyChannelRatingBinding
import com.banglalink.toffee.databinding.FragmentMyChannelHomeBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.handleUrlShare
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.MyChannelDetailBean
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.BindingUtil
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.regex.*
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelHomeFragment : BaseFragment(), OnClickListener {
    
    private val binding get() = _binding!!
    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var cacheManager: CacheManager
    private val bindingRating get() = _bindingRating!!
    private var myChannelDetail: MyChannelDetail? = null
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var progressDialog: ToffeeProgressDialog
    private var _binding: FragmentMyChannelHomeBinding ? = null
    private var _bindingRating: AlertDialogMyChannelRatingBinding ? = null
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val viewModel by activityViewModels<MyChannelHomeViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    
    companion object {
        const val IS_MY_CHANNEL = "isMyChannel"
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        
        fun newInstance(channelOwnerId: Int, isMyChannel: Boolean): MyChannelHomeFragment {
            return MyChannelHomeFragment().apply {
                arguments = bundleOf(CHANNEL_OWNER_ID to channelOwnerId, IS_MY_CHANNEL to isMyChannel)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ToffeeProgressDialog(requireContext())
        viewModel.isMyChannel = arguments?.getBoolean(IS_MY_CHANNEL) ?: false
        viewModel.channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: mPref.customerId
        if(viewModel.channelOwnerId == 0) viewModel.channelOwnerId = mPref.customerId
        viewModel.isOwner = viewModel.channelOwnerId == mPref.customerId
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyChannelHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (_binding == null) {
            requireContext().showToast(getString(R.string.try_again_message))
            return
        }
        if (viewModel.isOwner) ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_MY_CHANNEL)
        binding.contentBody.hide()
        val showDetails = !(!mPref.isVerifiedUser && viewModel.isMyChannel && viewModel.isOwner)
        if(showDetails) {
            progressDialog.show()
            observeChannelDetail()
            observeSubscribeChannel()
            viewModel.getChannelDetail(viewModel.channelOwnerId)
        } else {
            setBindingData()
        }
        binding.channelDetailView.subscriptionButton.isEnabled = true
        binding.channelDetailView.addBioButton.safeClick(this)
        binding.channelDetailView.editButton.safeClick(this)
        binding.channelDetailView.analyticsButton.safeClick(this)
        binding.channelDetailView.ratingButton.safeClick(this)
        binding.channelDetailView.subscriptionButton.safeClick(this)
        binding.channelDetailView.channelShareButton.safeClick({
            myChannelDetail?.channelShareUrl?.let { requireActivity().handleUrlShare(it) }
        })
    }
    
    override fun onClick(v: View?) {
        handleClick(v)
    }
    
    private fun handleClick(v: View?) {
        when (v) {
            _binding?.channelDetailView?.addBioButton -> {
                if (!mPref.isVerifiedUser){
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.LOGIN_SOURCE,
                        bundleOf(
                            "source" to "add_channel_bio",
                            "method" to "mobile"
                        )
                    )
                }
                requireActivity().checkVerification { navigateToEditChannel() }
            }
            _binding?.channelDetailView?.editButton -> {
                if (!mPref.isVerifiedUser){
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.LOGIN_SOURCE,
                        bundleOf(
                            "source" to "create_channel",
                            "method" to "mobile"
                        )
                    )
                }
                requireActivity().checkVerification { navigateToEditChannel() }
            }
            _binding?.channelDetailView?.ratingButton -> {
                if (!mPref.isVerifiedUser){
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.LOGIN_SOURCE,
                        bundleOf(
                            "source" to "rate_channel",
                            "method" to "mobile"
                        )
                    )
                }
                requireActivity().checkVerification { showRatingDialog() }
            }
            _binding?.channelDetailView?.analyticsButton -> {
                if (!mPref.isVerifiedUser){
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.LOGIN_SOURCE,
                        bundleOf(
                            "source" to "create_channel_playlist",
                            "method" to "mobile"
                        )
                    )
                }
                requireActivity().checkVerification {
                    if (viewModel.channelId > 0) {
                        showCreatePlaylistDialog()
                    } else {
                        requireContext().showToast(getString(R.string.create_channel_msg))
                    }
                }
            }
            _binding?.channelDetailView?.subscriptionButton -> {
                if (!mPref.isVerifiedUser){
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.LOGIN_SOURCE,
                        bundleOf(
                            "source" to "follow_channel",
                            "method" to "mobile"
                        )
                    )
                }
                requireActivity().checkVerification {
                    if (viewModel.isSubscribed == 0) {
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, viewModel.channelOwnerId, mPref.customerId), 1)
                    } else {
                        UnSubscribeDialog.show(requireContext()) {
                            homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, viewModel.channelOwnerId, mPref.customerId), -1)
                        }
                    }
                }
            }
        }
    }
    
    private fun navigateToEditChannel() {
        findNavController().navigate(R.id.myChannelEditDetailFragment, bundleOf("channelOwnerId" to viewModel.channelOwnerId))
    }
    
    private fun showRatingDialog() {
        _bindingRating = AlertDialogMyChannelRatingBinding.inflate(layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(bindingRating.root)
        bindingRating.ratingBar.rating = viewModel.myRating.toFloat()
        var newRating = 0.0f
        bindingRating.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            newRating = rating
        }
        
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        bindingRating.submitButton.setOnClickListener {
            if (newRating > 0 && newRating.toInt() != viewModel.myRating) {
                viewModel.myRating = newRating.toInt()
                Timber.tag("Rating_").i("setting myRating: ${viewModel.myRating}")
                viewModel.rateMyChannel(viewModel.channelOwnerId, newRating)
            }
            alertDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        bindingUtil.bindButtonState(binding.channelDetailView.ratingButton, viewModel.myRating > 0)
    }
    
    fun showCreatePlaylistDialog() {
        val playlistBinding = AlertDialogMyChannelPlaylistCreateBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(playlistBinding.root)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        createPlaylistViewModel.playlistName = ""
        playlistBinding.playlistNameEditText.text.clear()
        playlistBinding.viewModel = createPlaylistViewModel
        playlistBinding.createButton.setOnClickListener {
            if (!createPlaylistViewModel.playlistName.isNullOrBlank()) {
                observeCreatePlaylist()
                createPlaylistViewModel.createPlaylist(viewModel.channelOwnerId)
                createPlaylistViewModel.playlistName = null
                alertDialog.dismiss()
            } else {
                requireContext().showToast(getString(R.string.playlist_name_empty_msg))
            }
        }
        playlistBinding.closeIv.setOnClickListener { alertDialog.dismiss() }
    }
    
    private fun observeChannelDetail() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    setBindingData(it.data)
                }
                is Failure -> {
                    setBindingData()

                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.GET_MY_CHANNEL_DETAILS,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.MY_CHANNEL_PLAYLIST_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg
                        )
                    )
                }
            }
        }
    }
    
    private fun setBindingData(channelData: MyChannelDetailBean? = null) {
        channelData?.let {
            myChannelDetail = it.myChannelDetail
            viewModel.rating = it.ratingCount
            viewModel.myRating = it.myRating
            Timber.tag("Rating_").i("setting myRating--: ${viewModel.myRating}")
            viewModel.isOwner = it.isOwner == 1 && mPref.isVerifiedUser
            viewModel.isSubscribed = it.isSubscribed
            mPref.isChannelDetailChecked = true
            viewModel.subscriberCount = it.subscriberCount
            viewModel.channelId = myChannelDetail?.id?.toInt() ?: 0
            binding.channelDetailView.channelShareButton.isVisible = channelData.myChannelDetail?.isApproved ?: false
        }
        
        if (viewModel.isOwner) {
            myChannelDetail?.let {
                mPref.channelLogo = it.profileUrl ?: ""
                mPref.channelName = it.channelName ?: ""
                mPref.customerName = it.name  ?: ""
                mPref.customerEmail = it.email  ?: ""
                mPref.customerAddress = it.address  ?: ""
                mPref.customerDOB = it.dateOfBirth ?: ""
                mPref.customerNID = it.nationalIdNo ?: ""
            }
        }
        
        mPref.channelId = 0
        binding.data = channelData
        binding.isSubscribed = viewModel.isSubscribed
        binding.myRating = viewModel.myRating
        binding.isOwner = viewModel.isOwner
        binding.subscriberCount = viewModel.subscriberCount
        
        if (viewModel.isOwner && mPref.isVerifiedUser) {
            mPref.channelId = viewModel.channelId
        }
        else if (viewModel.isOwner && ! mPref.isVerifiedUser) {
            myChannelDetail = null
            binding.data = null
        }
        else if (!viewModel.isOwner && ! mPref.isVerifiedUser) {
            binding.isSubscribed = 0
            binding.myRating = 0
        }
        loadBody()
    }
    
    private fun loadBody() {
        if (viewModel.isOwner) {
            activity?.title = "My Channel"
        } else {
            activity?.title = myChannelDetail?.channelName ?: "Channel"
        }
        progressDialog.dismiss()
        binding.contentBody.visibility = View.VISIBLE
        
        observeRatingChannel()

        
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        if (viewPagerAdapter.itemCount == 0) {
            viewPagerAdapter.addFragments(listOf(
                MyChannelVideosFragment.newInstance(viewModel.channelOwnerId, viewModel.isMyChannel),
                MyChannelPlaylistsHostFragment.newInstance(viewModel.channelOwnerId, viewModel.isMyChannel)
            ))
        }
        binding.viewPager.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT
        binding.viewPager.adapter = viewPagerAdapter
        
        val fragmentTitleList = listOf(resources.getString(R.string.videos), resources.getString(R.string.creators_playlist))
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()
        
        myChannelDetail?.description?.let {
            val spannable: Spannable = it.toSpannable()
            val matcher = Pattern.compile("(#\\w+)").matcher(spannable)
            while (matcher.find()) {
                spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), color.colorAccent2)), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            binding.channelDetailView.channelDescriptionTextView.text = spannable
        }
    }
    
    fun getViewPagerPosition(): Int {
        return binding.viewPager.currentItem
    }
    
    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Success -> {
                    if (response.data == null) {
                        requireContext().showToast(getString(R.string.try_again_message))
                    } else {
                        viewModel.isSubscribed = response.data?.isSubscribed ?: 0
                        viewModel.subscriberCount = response.data?.subscriberCount ?: 0
                        binding.isSubscribed = viewModel.isSubscribed
                        binding.subscriberCount = viewModel.subscriberCount
                    }
                }
                is Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
            binding.channelDetailView.subscriptionButton.isEnabled = true
        }
    }
    
    private fun observeRatingChannel() {
        observe(viewModel.ratingLiveData) {
            when (it) {
                is Success -> {
                    if (it.data == null) {
                        requireContext().showToast(getString(R.string.try_again_message))
                    } else {
                        binding.myRating = viewModel.myRating
                        Timber.tag("Rating_").i("getting myRating: ${viewModel.myRating}")
                        bindingUtil.bindButtonState(binding.channelDetailView.ratingButton, viewModel.myRating > 0)
                        binding.channelDetailView.ratingCountTextView.text = (it.data?.ratingCount ?: 0).toString()
                        cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_DETAILS)
                    }
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.RATE_CHANNEL,
                            FirebaseParams.BROWSER_SCREEN to "Users Channel",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                }
            }
        }
    }
    
    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.createPlaylistLiveData) {
            when (it) {
                is Success -> {
                    if (it.data == null) {
                        requireContext().showToast(getString(R.string.try_again_message))
                    } else {
                        requireContext().showToast(it.data?.message)
                        playlistReloadViewModel.reloadPlaylist.value = true
                    }
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.CREATE_PLAYLIST,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.ALL_USER_CHANNELS_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        binding.viewPager.adapter = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onDestroy() {
        progressDialog.dismiss()
        super.onDestroy()
        _binding = null
        _bindingRating= null
    }
}