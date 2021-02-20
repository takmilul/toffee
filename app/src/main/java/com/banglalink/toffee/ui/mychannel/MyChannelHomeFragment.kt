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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.color
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_DETAILS_URL
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.databinding.FragmentMyChannelHomeBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.bindButtonState
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_dialog_my_channel_rating.view.*
import kotlinx.android.synthetic.main.layout_my_channel_detail.*
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelHomeFragment : BaseFragment(), OnClickListener {

    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var channelOwnerId: Int = 0
    private var rating: Float = 0.0f
    private var myRating: Int = 0
    private var isSubscribed: Int = 0
    private var isPublic: Int = 0
    private var myChannelDetail: MyChannelDetail? = null
    private lateinit var progressDialog: VelBoxProgressDialog
    private lateinit var binding: FragmentMyChannelHomeBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var fragmentList: ArrayList<androidx.fragment.app.Fragment> = arrayListOf()
    private var fragmentTitleList: ArrayList<String> = arrayListOf()
    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var myChannelHomeViewModelAssistedFactory: MyChannelHomeViewModel.AssistedFactory
    private val viewModel by viewModels<MyChannelHomeViewModel> { MyChannelHomeViewModel.provideFactory(myChannelHomeViewModelAssistedFactory, isOwner, isPublic, channelId, channelOwnerId) }
//    @Inject @DefaultCache lateinit var retrofitCache: Cache
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val subscribeChannelViewModel by viewModels<MyChannelSubscribeViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()

    companion object {
        const val IS_OWNER = "isOwner"
        const val IS_SUBSCRIBED = "isSubscribed"
        const val CHANNEL_ID = "channelId"
        const val IS_PUBLIC = "isPublic"
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        const val PAGE_TITLE = "title"

        fun newInstance(isSubscribed: Int, isOwner: Int, channelId: Int, channelOwnerId: Int, isPublic: Int): MyChannelHomeFragment {
            val instance = MyChannelHomeFragment()
            val bundle = Bundle()
            bundle.putInt(IS_SUBSCRIBED, isSubscribed)
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_ID, channelId)
            bundle.putInt(IS_PUBLIC, isPublic)
            bundle.putInt(CHANNEL_OWNER_ID, channelOwnerId)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
        val args = arguments
        isSubscribed = args?.getInt(IS_SUBSCRIBED) ?: 0
        isOwner = args?.getInt(IS_OWNER) ?: 1
        channelId = args?.getInt(CHANNEL_ID) ?: 0
        isPublic = args?.getInt(IS_PUBLIC) ?: 0
        channelId = if (channelId == 0) mPref.channelId else channelId
        channelOwnerId = args?.getInt(CHANNEL_OWNER_ID) ?: mPref.customerId
        channelOwnerId = if (channelOwnerId == 0) mPref.customerId else channelOwnerId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_channel_home, container, false)
        binding.lifecycleOwner = this
        binding.isOwner = isOwner
        binding.isSubscribed = isSubscribed
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.contentBody.visibility = View.GONE
//        binding.progressBar.visibility = View.VISIBLE
        progressDialog.show()

        observeChannelDetail()
        viewModel.getChannelDetail()
        binding.channelDetailView.subscriptionButton.isEnabled = true

        binding.channelDetailView.addBioButton.safeClick(this)
        binding.channelDetailView.editButton.safeClick(this)
        binding.channelDetailView.analyticsButton.safeClick(this)
        binding.channelDetailView.ratingButton.safeClick(this)
        binding.channelDetailView.subscriptionButton.safeClick(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            addBioButton -> {
                if (findNavController().currentDestination?.id == R.id.myChannelHomeFragment){
                    val action = MyChannelHomeFragmentDirections.actionMyChannelHomeFragmentToMyChannelEditDetailFragment(myChannelDetail)
                    findNavController().navigate(action)
                }
                else{
                    findNavController().navigate(R.id.action_menu_channel_to_myChannelEditFragment, Bundle().apply { putParcelable("myChannelDetail", myChannelDetail) })
                }
            }

            editButton -> {
                if (findNavController().currentDestination?.id == R.id.myChannelHomeFragment) {
                    val action = MyChannelHomeFragmentDirections.actionMyChannelHomeFragmentToMyChannelEditDetailFragment(myChannelDetail)
                    findNavController().navigate(action)
                }
                else {
                    findNavController().navigate(R.id.action_menu_channel_to_myChannelEditFragment, Bundle().apply { putParcelable("myChannelDetail", myChannelDetail) })
                }
            }

            ratingButton -> showRatingDialog()
            analyticsButton -> {
                if (channelId > 0) {
                    showCreatePlaylistDialog()
                }
                else {
                    Toast.makeText(requireContext(), "Please create channel first", Toast.LENGTH_SHORT).show()
                }
            }
            subscriptionButton -> {
                if (isSubscribed == 0) {
                    subscribeChannelViewModel.isButtonEnabled.value = false
                    binding.channelDetailView.subscriptionButton.isEnabled = false
                    subscribeChannelViewModel.subscribe(channelId, 1, channelOwnerId)
                }
                else{
                    UnSubscribeDialog.show(requireContext()){
                        subscribeChannelViewModel.isButtonEnabled.value = false
                        binding.channelDetailView.subscriptionButton.isEnabled = false
                        subscribeChannelViewModel.subscribe(channelId, 0, channelOwnerId)
                    }
                }
            }
        }
    }

    private fun showRatingDialog() {
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext())
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(layout.alert_dialog_my_channel_rating, null)
        dialogBuilder.setView(dialogView)
        dialogView.ratingBar.rating = myRating.toFloat()
        var newRating = 0.0f
        dialogView.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            newRating = rating
        }

        val alertDialog: android.app.AlertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        dialogView.submitButton.setOnClickListener {
            if (newRating > 0 && newRating.toInt() != myRating) {
                myRating = newRating.toInt()
                viewModel.rateMyChannel(newRating)
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
                createPlaylistViewModel.createPlaylist(isOwner, channelId)
                createPlaylistViewModel.playlistName = null
                alertDialog.dismiss()
            }
            else {
                Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
            }
        }
        playlistBinding.closeIv.setOnClickListener { alertDialog.dismiss() }
    }

    private fun observeChannelDetail() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    if (it.data != null) {
                        myChannelDetail = it.data.myChannelDetail
                        isSubscribed = it.data.isSubscribed
                        rating = it.data.ratingCount
                        myRating = it.data.myRating
                        binding.myRating = myRating
                        binding.channelDetailView.subscriptionCountTextView.text = it.data.subscriberCount.toString()
                        isOwner = it.data.isOwner
                        channelId = myChannelDetail?.id?.toInt() ?: 0
                        binding.data = it.data
                        binding.isSubscribed = isSubscribed
                        mPref.channelId = channelId
                        if (isOwner == 1) {
                            myChannelDetail?.profileUrl?.let { channelLogo -> mPref.channelLogo = channelLogo }
                            myChannelDetail?.channelName?.let { channelName -> mPref.channelName = channelName }
                        }
                        loadBody()
                    }
                }
                is Failure -> {
                    myChannelDetail = null
                    isSubscribed = 0
                    binding.data = null
                    binding.isSubscribed = 0

                    loadBody()
                }
            }
        }
    }

    private fun loadBody() {
        if(isPublic == 1){
            activity?.title = myChannelDetail?.channelName ?: "Channel"
        }
        else {
            activity?.title = "My Channel"
        }
        progressDialog.dismiss()
//        binding.progressBar.visibility = View.GONE
        binding.contentBody.visibility = View.VISIBLE

        observeRatingChannel()
        observeSubscribeChannel()

        binding.viewPager.offscreenPageLimit = 1
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)

        if (fragmentList.isEmpty()) {

            fragmentTitleList.add("Videos")
            fragmentTitleList.add("Playlists")

            viewPagerAdapter.addFragment(MyChannelVideosFragment.newInstance(true, isOwner, channelOwnerId, isPublic))
            viewPagerAdapter.addFragment(MyChannelPlaylistsFragment.newInstance(true, isOwner, channelOwnerId, channelId))
            
        }
        
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.isUserInputEnabled = false

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
    
    private fun observeSubscribeChannel() {
        observe(subscribeChannelViewModel.liveData) {
            when (it) {
                is Success -> {
                    isSubscribed = it.data.isSubscribed
                    binding.channelDetailView.subscriptionCountTextView.text = it.data.subscriberCount.toString()
                    binding.isSubscribed = isSubscribed
                    binding.channelDetailView.subscriptionButton.isEnabled = true
                    cacheManager.clearSubscriptionCache()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
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
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_DETAILS_URL)
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
                    requireContext().showToast(it.data.message?:"")
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
    }
}