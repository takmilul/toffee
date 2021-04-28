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
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.color
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_DETAILS
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
import com.banglalink.toffee.util.bindButtonState
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
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
    @Inject lateinit var cacheManager: CacheManager
    private var myChannelDetail: MyChannelDetail? = null
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    val homeViewModel by activityViewModels<HomeViewModel>()
    private lateinit var progressDialog: VelBoxProgressDialog
    private var _binding: FragmentMyChannelHomeBinding ? = null
    private val binding get() = _binding!!
    private var _bindingRating: AlertDialogMyChannelRatingBinding ? = null
    private val bindingRating get() = _bindingRating!!
    private val viewModel by viewModels<MyChannelHomeViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    
    companion object {
        const val PAGE_TITLE = "title"
        const val IS_OWNER = "isOwner"
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        
        fun newInstance(channelOwnerId: Int, isOwner: Boolean = false): MyChannelHomeFragment {
            return MyChannelHomeFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(IS_OWNER, isOwner)
                    putInt(CHANNEL_OWNER_ID, channelOwnerId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
        isOwner = arguments?.getBoolean(IS_OWNER) ?: true
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: mPref.customerId
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
        
        progressDialog.show()
        binding.contentBody.hide()
        
        observeChannelDetail()
//        observeSubscribeChannel()
        viewModel.getChannelDetail(channelOwnerId)
        
        binding.channelDetailView.subscriptionButton.isEnabled = true
        binding.channelDetailView.addBioButton.safeClick(this)
        binding.channelDetailView.editButton.safeClick(this)
        binding.channelDetailView.analyticsButton.safeClick(this)
        binding.channelDetailView.ratingButton.safeClick(this)
        binding.channelDetailView.subscriptionButton.safeClick(this)
    }

    override fun onClick(v: View?) {
        requireActivity().checkVerification {
            handleClick(v)
        }
    }

    private fun handleClick(v: View?) {
        when (v) {
            binding.channelDetailView.addBioButton -> {
                navigateToEditChannel()
            }

            binding.channelDetailView.editButton -> {
                navigateToEditChannel()
            }

            binding.channelDetailView.ratingButton -> {
                showRatingDialog()
            }
            binding.channelDetailView.analyticsButton -> {
                if (channelId > 0) {
                    showCreatePlaylistDialog()
                } else {
                    Toast.makeText(requireContext(), "Please create channel first", Toast.LENGTH_SHORT).show()
                }
            }
            binding.channelDetailView.subscriptionButton -> {
                if (isSubscribed == 0) {
                    binding.channelDetailView.subscriptionButton.isEnabled = false
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, channelOwnerId, mPref.customerId), 1)
                    isSubscribed = 1
                    binding.isSubscribed = isSubscribed
                    binding.subscriberCount = ++subscriberCount
                    binding.channelDetailView.subscriptionButton.isEnabled = true
                } else {
                    UnSubscribeDialog.show(requireContext()) {
                        binding.channelDetailView.subscriptionButton.isEnabled = false
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, channelOwnerId, mPref.customerId), -1)
                        isSubscribed = 0
                        binding.subscriberCount = --subscriberCount
                        binding.isSubscribed = isSubscribed
                        binding.channelDetailView.subscriptionButton.isEnabled = true
                    }
                }
            }
        }
    }
    
    private fun navigateToEditChannel() {
        if (findNavController().currentDestination?.id != R.id.myChannelEditDetailFragment && findNavController().currentDestination?.id == R.id.myChannelHomeFragment) {
            findNavController().navigate(R.id.action_myChannelHomeFragment_to_MyChannelEditDetailFragment,
                Bundle().apply { putParcelable("myChannelDetail", myChannelDetail) })
        }
        else if (findNavController().currentDestination?.id != R.id.myChannelEditDetailFragment && findNavController().currentDestination?.id == R.id.menu_channel) {
            findNavController().navigate(R.id.action_menu_channel_to_myChannelEditFragment,
                Bundle().apply { putParcelable("myChannelDetail", myChannelDetail) })
        }
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
        alertDialog.setOnDismissListener { bindButtonState(binding.channelDetailView.ratingButton, myRating > 0) }
    }

    override fun onResume() {
        super.onResume()
        bindButtonState(binding.channelDetailView.ratingButton, myRating > 0)
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
                Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
            }
        }
        playlistBinding.closeIv.setOnClickListener { alertDialog.dismiss() }
    }
    
    private fun observeChannelDetail() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    it.data?.let { channelData ->
                        myChannelDetail = channelData.myChannelDetail
                        rating = channelData.ratingCount
                        myRating = channelData.myRating
                        isOwner = channelData.isOwner == 1
                        isSubscribed = channelData.isSubscribed
                        subscriberCount = channelData.subscriberCount
                        channelId = myChannelDetail?.id?.toInt() ?: 0
                        mPref.channelId = channelId
    
                        setBindingData(channelData)
                        loadBody()
                    } ?: setEmptyData()
                }
                is Failure -> {
                    setEmptyData()
                }
            }
        }
    }
    
    private fun setEmptyData() {
        myChannelDetail = null
        binding.data = null
        binding.isSubscribed = 0
        binding.isOwner = isOwner
        loadBody()
    }
    
    private fun setBindingData(channelData: MyChannelDetailBean?) {
        if (isOwner) {
            myChannelDetail?.let { detail ->
                if (! detail.profileUrl.isNullOrBlank()) {
                    mPref.channelLogo = detail.profileUrl
                }
                if (! detail.channelName.isNullOrBlank()) {
                    mPref.channelName = detail.channelName
                }
            }
        }
        if (! mPref.isVerifiedUser && isOwner) {
            myChannelDetail = null
            binding.data = null
            binding.isOwner = isOwner
        }
        else if (! mPref.isVerifiedUser && ! isOwner) {
            binding.data = channelData
            binding.isOwner = isOwner
            binding.myRating = 0
            binding.isSubscribed = 0
            binding.subscriberCount = channelData?.subscriberCount
        }
        else {
            binding.data = channelData
            binding.isOwner = isOwner
            binding.myRating = myRating
            binding.isSubscribed = channelData?.isSubscribed
            binding.subscriberCount = channelData?.subscriberCount
        }
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
        
        val fragmentTitleList = listOf("Videos", "Playlists")
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
    
    private fun reduceMarginsInTabs(tabLayout: TabLayout, marginOffset: Int) {
        val tabStrip = tabLayout.getChildAt(0)
        if (tabStrip is ViewGroup) {
            for (i in 0 until tabStrip.childCount) {
                val tabView = tabStrip.getChildAt(i)
                
                if (tabView.layoutParams is MarginLayoutParams) {
                    (tabView.layoutParams as MarginLayoutParams).leftMargin = marginOffset
                    (tabView.layoutParams as MarginLayoutParams).rightMargin = marginOffset
                }
            }
            tabLayout.requestLayout()
        }
    }
    
    fun getViewPagerPosition(): Int {
        return binding.viewPager.currentItem
    }
    
    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Success -> {
                    val status = response.data.isSubscribed.takeIf { it == 1 } ?: -1
                    if (response.data.isSubscribed == 1) {
                        isSubscribed = 1
                        binding.isSubscribed = isSubscribed
                        binding.subscriberCount = ++subscriberCount
                    }
                    else {
                        isSubscribed = 0
                        binding.isSubscribed = isSubscribed
                        binding.subscriberCount = --subscriberCount
                    }
                    binding.channelDetailView.subscriptionButton.isEnabled = true
                    homeViewModel.updateSubscriptionCountTable(SubscriptionInfo(null, channelOwnerId, mPref.customerId), status)
                }
                is Failure -> {
                    requireContext().showToast(response.error.msg)
                    binding.channelDetailView.subscriptionButton.isEnabled = true
                }
            }
        }
    }
    
    private fun observeRatingChannel() {
        observe(viewModel.ratingLiveData) {
            when (it) {
                is Success -> {
                    binding.myRating = myRating
                    bindButtonState(binding.channelDetailView.ratingButton, myRating > 0)
                    binding.channelDetailView.ratingCountTextView.text = it.data.ratingCount.toString()
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_DETAILS)
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.createPlaylistLiveData) {
            when (it) {
                is Success -> {
                    requireContext().showToast(it.data.message ?: "")
                    playlistReloadViewModel.reloadPlaylist.value = true
                }
                is Failure -> {
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