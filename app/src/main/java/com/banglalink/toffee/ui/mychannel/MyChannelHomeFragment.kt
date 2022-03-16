package com.banglalink.toffee.ui.mychannel

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
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.MyChannelDetailBean
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.BindingUtil
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.*
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelHomeFragment : BaseFragment(), OnClickListener {
    
    private var myRating: Int = 0
    private var channelId: Int = 0
    private var rating: Float = 0.0f
    private var isSubscribed: Int = 0
    private var channelOwnerId: Int = 0
    private var isOwner: Boolean = false
    private var subscriberCount: Long = 0
    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var cacheManager: CacheManager
    private var myChannelDetail: MyChannelDetail? = null
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var progressDialog: VelBoxProgressDialog
    private var _binding: FragmentMyChannelHomeBinding ? = null
    private val binding get() = _binding!!
    private var _bindingRating: AlertDialogMyChannelRatingBinding ? = null
    val homeViewModel by activityViewModels<HomeViewModel>()
    private val bindingRating get() = _bindingRating!!
    private val viewModel by activityViewModels<MyChannelHomeViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    
    companion object {
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        
        fun newInstance(channelOwnerId: Int): MyChannelHomeFragment {
            return MyChannelHomeFragment().apply {
                arguments = bundleOf(CHANNEL_OWNER_ID to channelOwnerId)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: mPref.customerId
        if(channelOwnerId == 0) channelOwnerId = mPref.customerId
        isOwner = channelOwnerId == mPref.customerId
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyChannelHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.viewPager.adapter = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (isOwner) ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_MY_CHANNEL)
        binding.contentBody.hide()
        if(mPref.isVerifiedUser || !isOwner) {
            progressDialog.show()
            observeChannelDetail()
            observeSubscribeChannel()
            viewModel.getChannelDetail(channelOwnerId)
        } else {
            setBindingData()
        }
        binding.channelDetailView.subscriptionButton.isEnabled = true
        binding.channelDetailView.addBioButton.safeClick(this)
        binding.channelDetailView.editButton.safeClick(this)
        binding.channelDetailView.analyticsButton.safeClick(this)
        binding.channelDetailView.ratingButton.safeClick(this)
        binding.channelDetailView.subscriptionButton.safeClick(this)
        binding.channelDetailView.channelShareButton.safeClick(this)
    }
    
    override fun onClick(v: View?) {
        requireActivity().checkVerification {
            handleClick(v)
        }
    }

    private fun handleClick(v: View?) {
        when (v) {
            binding.channelDetailView.addBioButton -> { navigateToEditChannel() }
            binding.channelDetailView.editButton -> { navigateToEditChannel() }
            binding.channelDetailView.ratingButton -> { showRatingDialog() }
            binding.channelDetailView.analyticsButton -> {
                if (channelId > 0) {
                    showCreatePlaylistDialog()
                } else {
                    requireContext().showToast(getString(R.string.create_channel_msg))
                }
            }
            binding.channelDetailView.subscriptionButton -> {
                binding.channelDetailView.subscriptionButton.isEnabled = false
                if (isSubscribed == 0) {
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, channelOwnerId, mPref.customerId), 1)
                } else {
                    UnSubscribeDialog.show(requireContext()) {
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, channelOwnerId, mPref.customerId), -1)
                    }
                }
            }
            binding.channelDetailView.channelShareButton -> {
                myChannelDetail?.channelShareUrl?.let { requireActivity().handleUrlShare(it) }
            }
        }
    }
    
    private fun navigateToEditChannel() {
        findNavController().navigate(R.id.myChannelEditDetailFragment, bundleOf("myChannelDetail" to myChannelDetail))
    }
    
    private fun showRatingDialog() {
        _bindingRating = AlertDialogMyChannelRatingBinding.inflate(layoutInflater)
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext())
        dialogBuilder.setView(bindingRating.root)
        bindingRating.ratingBar.rating = myRating.toFloat()
        var newRating = 0.0f
        bindingRating.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            newRating = rating
        }
        
        val alertDialog: android.app.AlertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        bindingRating.submitButton.setOnClickListener {
            if (newRating > 0 && newRating.toInt() != myRating) {
                myRating = newRating.toInt()
                viewModel.rateMyChannel(channelOwnerId, newRating)
            }
            alertDialog.dismiss()
        }
        alertDialog.setOnDismissListener { bindingUtil.bindButtonState(binding.channelDetailView.ratingButton, myRating > 0) }
    }

    override fun onResume() {
        super.onResume()
        bindingUtil.bindButtonState(binding.channelDetailView.ratingButton, myRating > 0)
    }
    
    fun showCreatePlaylistDialog() {
        val playlistBinding = AlertDialogMyChannelPlaylistCreateBinding.inflate(this.layoutInflater)
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext()).setView(playlistBinding.root)
        val alertDialog: android.app.AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        playlistBinding.viewModel = createPlaylistViewModel
        playlistBinding.createButton.setOnClickListener {
            if (!createPlaylistViewModel.playlistName.isNullOrBlank()) {
                observeCreatePlaylist()
                createPlaylistViewModel.createPlaylist(channelOwnerId)
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
            rating = it.ratingCount
            myRating = it.myRating
            isOwner = it.isOwner == 1
            isSubscribed = it.isSubscribed
            mPref.isChannelDetailChecked = true
            subscriberCount = it.subscriberCount
            channelId = myChannelDetail?.id?.toInt() ?: 0
            binding.channelDetailView.channelShareButton.isVisible = channelData.myChannelDetail?.isApproved ?: false
        }
        
        if (isOwner) {
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
        binding.isSubscribed = isSubscribed
        binding.myRating = myRating
        binding.isOwner = isOwner
        binding.subscriberCount = subscriberCount
        
        if (isOwner && mPref.isVerifiedUser) {
            mPref.channelId = channelId
        }
        else if (isOwner && ! mPref.isVerifiedUser) {
            myChannelDetail = null
            binding.data = null
        }
        else if (!isOwner && ! mPref.isVerifiedUser) {
            binding.isSubscribed = 0
            binding.myRating = 0
        }
        loadBody()
    }
    
    private fun loadBody() {
        if (isOwner) {
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
                MyChannelVideosFragment.newInstance(channelOwnerId),
                MyChannelPlaylistsHostFragment.newInstance(channelOwnerId)
            ))
        }
        binding.viewPager.offscreenPageLimit = 1
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
                    isSubscribed = response.data.isSubscribed
                    subscriberCount = response.data.subscriberCount
                    binding.isSubscribed = isSubscribed
                    binding.subscriberCount = subscriberCount
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
                    binding.myRating = myRating
                    bindingUtil.bindButtonState(binding.channelDetailView.ratingButton, myRating > 0)
                    binding.channelDetailView.ratingCountTextView.text = it.data.ratingCount.toString()
                    cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_DETAILS)
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
                    requireContext().showToast(it.data.message)
                    playlistReloadViewModel.reloadPlaylist.value = true
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
    
    override fun onDestroy() {
        progressDialog.dismiss()
        super.onDestroy()
        _binding = null
        _bindingRating= null
    }
}