package com.banglalink.toffee.ui.mychannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.databinding.FragmentMyChannelHomeBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_dialog_my_channel_rating.view.*
import kotlinx.android.synthetic.main.layout_my_channel_detail.*
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MyChannelHomeFragment : androidx.fragment.app.Fragment(), OnClickListener {

    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var channelOwnerId: Int = 0
    private var rating: Float = 0.0f
    private var isSubscribed: Int = 0
    private var isPublic: Int = 0
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
        const val CHANNEL_ID = "channelId"
        const val IS_PUBLIC = "isPublic"
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        fun newInstance(isOwner: Int, channelId: Int, channelOwnerId: Int, isPublic: Int): MyChannelHomeFragment {
            val instance = MyChannelHomeFragment()
            val bundle = Bundle()
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
        val args = arguments
        isOwner = args?.getInt(IS_OWNER) ?: 1
        channelId = args?.getInt(CHANNEL_ID) ?: 0
        isPublic = args?.getInt(IS_PUBLIC) ?: 0
        channelId = if (channelId == 0) preference.channelId else channelId
        channelOwnerId = args?.getInt(CHANNEL_OWNER_ID) ?: preference.customerId
        channelOwnerId = if (channelOwnerId == 0) preference.customerId else channelOwnerId

        Log.i("UGC_Playlist_Service", "UGC_Home -- isOwner: ${isOwner}, ownerId: ${channelOwnerId}")
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
//        viewModel.getDetail()
        binding.channelDetailView.addBioButton.setOnClickListener(this)
        binding.channelDetailView.editButton.setOnClickListener(this)
        binding.channelDetailView.analyticsButton.setOnClickListener(this)
        binding.channelDetailView.ratingButton.setOnClickListener(this)
        binding.channelDetailView.subscriptionButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            addBioButton -> {
                findNavController().navigate(R.id.action_menu_channel_to_myChannelEditFragment, Bundle().apply { putParcelable("myChannelDetail", myChannelDetail) })
                /*val action = MyChannelHomeFragmentDirections.actionMyChannelHomeFragmentToMyChannelEditDetailFragment(myChannelDetail)
                findNavController().navigate(action)*/
            }

            editButton -> {
                findNavController().navigate(R.id.action_menu_channel_to_myChannelEditFragment, Bundle().apply { putParcelable("myChannelDetail", myChannelDetail) })
                /*val action = MyChannelHomeFragmentDirections.actionMyChannelHomeFragmentToMyChannelEditDetailFragment(myChannelDetail)
                findNavController().navigate(action)*/
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
            subscriptionButton -> subscribeChannelViewModel.subscribe(channelId, isSubscribed)
        }
    }

    private fun showRatingDialog() {
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext())
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(layout.alert_dialog_my_channel_rating, null)
        dialogBuilder.setView(dialogView)
        dialogView.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            this.rating = rating
            dialogView.submitButton.isEnabled = rating > 0
        }

        val alertDialog: android.app.AlertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        dialogView.submitButton.setOnClickListener {
            viewModel.rateMyChannel(rating)
            alertDialog.dismiss()
        }
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
                    binding.data = null
                    binding.isSubscribed = 0

                    loadBody()
                }
            }
        }
    }

    private fun loadBody() {
        binding.progressBar.visibility = View.GONE
        binding.contentBody.visibility = View.VISIBLE

        if (fragmentList.isEmpty()) {

            fragmentTitleList.add("Videos")
            fragmentTitleList.add("Playlists")

            fragmentList.add(MyChannelVideosFragment.newInstance(true, isOwner, channelOwnerId, isPublic))
            fragmentList.add(MyChannelPlaylistsFragment.newInstance(true, isOwner, channelOwnerId))
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

        // set interpolators for both expanding and collapsing animations
        binding.channelDetailView.channelDescriptionTextView.setInterpolator(OvershootInterpolator())

        // toggle the Expand button
        binding.channelDetailView.expandButton.setOnClickListener(View.OnClickListener {
            binding.channelDetailView.expandButton.background =
                if (binding.channelDetailView.channelDescriptionTextView.isExpanded) ContextCompat.getDrawable(requireContext(), R.drawable.ic_down_arrow) else ContextCompat.getDrawable(requireContext(), R.drawable.ic_up_arrow)
            binding.channelDetailView.channelDescriptionTextView.toggle()
        })
    }

    private fun observeSubscribeChannel() {
        observe(subscribeChannelViewModel.liveData) {
            when (it) {
                is Success -> {
                    isSubscribed = it.data.isSubscribed
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