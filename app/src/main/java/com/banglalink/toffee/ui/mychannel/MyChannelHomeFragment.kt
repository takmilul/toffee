package com.banglalink.toffee.ui.mychannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
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
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.databinding.FragmentMyChannelHomeBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.banglalink.toffee.ui.widget.ExpandableTextView
import com.banglalink.toffee.util.bindButtonState
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_dialog_my_channel_rating.view.*
import kotlinx.android.synthetic.main.layout_my_channel_detail.*
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject


@AndroidEntryPoint
class MyChannelHomeFragment : androidx.fragment.app.Fragment(), OnClickListener {

    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var channelOwnerId: Int = 0
    private var rating: Float = 0.0f
    private var myRating: Int = 0
    private var isSubscribed: Int = 0
    private var isPublic: Int = 0
    private var isFromOutside: Boolean = false
    private var myChannelDetail: MyChannelDetail? = null
    private lateinit var binding: FragmentMyChannelHomeBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var fragmentList: ArrayList<androidx.fragment.app.Fragment> = arrayListOf()
    private var fragmentTitleList: ArrayList<String> = arrayListOf()
    @Inject lateinit var preference: Preference

    @Inject lateinit var myChannelHomeViewModelAssistedFactory: MyChannelHomeViewModel.AssistedFactory
    private val viewModel by viewModels<MyChannelHomeViewModel> { MyChannelHomeViewModel.provideFactory(myChannelHomeViewModelAssistedFactory, isOwner, isPublic, channelId, channelOwnerId) }

    //    private val viewModel by viewModels<CreatorChannelViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val subscribeChannelViewModel by viewModels<MyChannelSubscribeViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelPlaylistReloadViewModel>()

    companion object {
        const val IS_OWNER = "isOwner"
        const val IS_SUBSCRIBED = "isSubscribed"
        const val CHANNEL_ID = "channelId"
        const val IS_PUBLIC = "isPublic"
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        const val IS_FROM_OUTSIDE = "isFromOutside"

        fun newInstance(isSubscribed: Int, isOwner: Int, channelId: Int, channelOwnerId: Int, isPublic: Int, isFromOutside: Boolean): MyChannelHomeFragment {
            val instance = MyChannelHomeFragment()
            val bundle = Bundle()
            bundle.putInt(IS_SUBSCRIBED, isSubscribed)
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_ID, channelId)
            bundle.putInt(IS_PUBLIC, isPublic)
            bundle.putInt(CHANNEL_OWNER_ID, channelOwnerId)
            bundle.putBoolean(IS_FROM_OUTSIDE, isFromOutside)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        isSubscribed = args?.getInt(IS_SUBSCRIBED) ?: 0
        isOwner = args?.getInt(IS_OWNER) ?: 1
        channelId = args?.getInt(CHANNEL_ID) ?: 0
        isPublic = args?.getInt(IS_PUBLIC) ?: 0
        channelId = if (channelId == 0) preference.channelId else channelId
        channelOwnerId = args?.getInt(CHANNEL_OWNER_ID) ?: preference.customerId
        channelOwnerId = if (channelOwnerId == 0) preference.customerId else channelOwnerId

        isFromOutside = args?.getBoolean(IS_FROM_OUTSIDE) ?: false
        Log.i("UGC_Home", "onCreate -- isSubscribed: ${isSubscribed}")
        /*isSubscribed = args.isSubscribed*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        binding.progressBar.visibility = View.VISIBLE

        observeChannelDetail()
        viewModel.getChannelDetail()

        binding.channelDetailView.addBioButton.setOnClickListener(this)
        binding.channelDetailView.editButton.setOnClickListener(this)
        binding.channelDetailView.analyticsButton.setOnClickListener(this)
        binding.channelDetailView.ratingButton.setOnClickListener(this)
        binding.channelDetailView.subscriptionButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            addBioButton -> {
                if (isFromOutside) {
                    val action = MyChannelHomeFragmentDirections.actionMyChannelHomeFragmentToMyChannelEditDetailFragment(myChannelDetail)
                    findNavController().navigate(action)
                }
                else {
                    findNavController().navigate(R.id.action_menu_channel_to_myChannelEditFragment, Bundle().apply { putParcelable("myChannelDetail", myChannelDetail) })
                }
            }

            editButton -> {
                if (isFromOutside) {
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
            subscriptionButton -> subscribeChannelViewModel.subscribe(channelId, if (isSubscribed == 0) 1 else 0, channelOwnerId)
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
            dialogView.submitButton.isEnabled = rating > 0
        }

        val alertDialog: android.app.AlertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        dialogView.submitButton.setOnClickListener {
            myRating = newRating.toInt()
            viewModel.rateMyChannel(newRating)
            alertDialog.dismiss()
        }
        alertDialog.setOnDismissListener { bindButtonState(binding.channelDetailView.ratingButton, myRating > 0) }
    }

    override fun onResume() {
        super.onResume()
        bindButtonState(binding.channelDetailView.ratingButton, myRating > 0)
    }
    
    private fun showCreatePlaylistDialog() {
        val playlistBinding = AlertDialogMyChannelPlaylistCreateBinding.inflate(this.layoutInflater)
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext()).setView(playlistBinding.root)
        val alertDialog: android.app.AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        playlistBinding.viewModel = createPlaylistViewModel
        playlistBinding.createButton.setOnClickListener {
            if (!createPlaylistViewModel.playlistName.isNullOrEmpty()) {
                observeCreatePlaylist()
                createPlaylistViewModel.createPlaylist(isOwner, channelId)
                createPlaylistViewModel.playlistName = null
                alertDialog.dismiss()
            }
            else {
                Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
            }
        }
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
                        Log.i("UGC_Home", "Detail Response Success -- isSubscribed: ${isSubscribed}, subscribeCount: ${it.data.subscriberCount}")
                        isOwner = it.data.isOwner
                        channelId = myChannelDetail?.id?.toInt() ?: 0
                        binding.data = it.data
                        binding.isSubscribed = isSubscribed
                        preference.channelId = channelId

                        loadBody()
                    }
                }
                is Failure -> {
                    myChannelDetail = null
                    isSubscribed = 0
                    Log.i("UGC_Home", "Response Failed -- isSubscribed: ${isSubscribed}")
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
        binding.progressBar.visibility = View.GONE
        binding.contentBody.visibility = View.VISIBLE

        if (fragmentList.isEmpty()) {

            fragmentTitleList.add("Videos")
            fragmentTitleList.add("Playlists")

            fragmentList.add(MyChannelVideosFragment.newInstance(true, isOwner, channelOwnerId, isPublic, isFromOutside))
            fragmentList.add(MyChannelPlaylistsFragment.newInstance(true, isOwner, channelOwnerId, isFromOutside))
        }

        observeRatingChannel()
        observeSubscribeChannel()

        binding.viewPager.offscreenPageLimit = 1
        viewPagerAdapter = ViewPagerAdapter(this, fragmentList)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()

        // toggle the Expand button
        binding.channelDetailView.expandButton.setOnClickListener {
            val resource = if (binding.channelDetailView.channelDescriptionTextView.isExpanded) R.drawable.ic_down_arrow else R.drawable.ic_up_arrow
            binding.channelDetailView.expandButton.setImageResource(resource)
            binding.channelDetailView.channelDescriptionTextView.toggle()
        }

        binding.channelDetailView.channelDescriptionTextView.addOnExpandListener(object : ExpandableTextView.SimpleOnExpandListener(){
            override fun onControllerVisibility(view: ExpandableTextView, show: Boolean) {
                binding.channelDetailView.expandButton.visibility = if(show) View.VISIBLE else View.GONE
            }
        })

        myChannelDetail?.description?.let {
            val spannable: Spannable = it.toSpannable()
            val matcher = Pattern.compile("(#\\w+)").matcher(spannable)
            while (matcher.find()) {
                spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), color.purple)), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                    Log.i("UGC_Home", "Sucbscribe response -- isSubscribed: ${isSubscribed}, subscribeCount: ${it.data.subscriberCount}")
                    binding.isSubscribed = isSubscribed
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
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
                    playlistReloadViewModel.reloadPlaylist.value = true
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}