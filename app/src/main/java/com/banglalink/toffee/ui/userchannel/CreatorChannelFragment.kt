package com.banglalink.toffee.ui.userchannel

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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.databinding.AlertDialogCreatePlaylistBinding
import com.banglalink.toffee.databinding.FragmentCreatorChannelBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_dialog_channel_rating.view.*
import kotlinx.android.synthetic.main.layout_creator_channel_info.addBioButton
import kotlinx.android.synthetic.main.layout_creator_channel_info.editButton
import kotlinx.android.synthetic.main.layout_my_channel_detail.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CreatorChannelFragment : Fragment(), OnClickListener {

    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var playlistId: Int = 7
    private lateinit var binding: FragmentCreatorChannelBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var fragmentList: ArrayList<Fragment> = arrayListOf()
    private var fragmentTitleList: ArrayList<String> = arrayListOf()

    private val viewModel by viewModels<CreatorChannelViewModel>()
    private val createPlaylistViewModel by viewModels<UgcMyChannelCreatePlaylistViewModel>()
    private val deletePlaylistViewModel by viewModels<UgcMyChannelDeletePlaylistViewModel>()
    
    companion object {
        private const val IS_OWNER = "isOwner"
        private const val CHANNEL_ID = "channelId"
        fun newInstance(isOwner: Int, channelId: Int): CreatorChannelFragment {
            val instance = CreatorChannelFragment()
            val bundle = Bundle()
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_ID, channelId)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val args = CreatorChannelFragmentArgs.fromBundle(requireArguments())
        isOwner = args.isOwner
        channelId = args.channelId
        isSubscribed = args.isSubscribed*/

        isOwner = arguments?.getInt(IS_OWNER) ?: 1
        channelId = arguments?.getInt(CHANNEL_ID) ?: 2

        if (fragmentList.isEmpty()) {

            fragmentTitleList.add("Videos")
            fragmentTitleList.add("Playlists")

            fragmentList.add(ChannelVideosFragment.newInstance(true, isOwner, channelId))
            fragmentList.add(ChannelPlaylistsFragment.newInstance(true))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_creator_channel, container, false)
        binding.lifecycleOwner = this
        binding.isOwner = isOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadData(isOwner, channelId)

        binding.viewPager.offscreenPageLimit = 1
        binding.creatorChannelView.addBioButton.setOnClickListener(this)
        binding.creatorChannelView.editButton.setOnClickListener(this)
        binding.creatorChannelView.ratingButton.setOnClickListener(this)
        binding.creatorChannelView.analyticsButton.setOnClickListener(this)

        viewPagerAdapter = ViewPagerAdapter(this, fragmentList)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()

        // set interpolators for both expanding and collapsing animations
        binding.creatorChannelView.channelDescriptionTextView.setInterpolator(OvershootInterpolator())

        // toggle the Expand button
        binding.creatorChannelView.expandButton.setOnClickListener(View.OnClickListener {
            binding.creatorChannelView.expandButton.background =
                if (binding.creatorChannelView.channelDescriptionTextView.isExpanded) ContextCompat.getDrawable(requireContext(), R.drawable.ic_down_arrow) else ContextCompat.getDrawable(requireContext(), R.drawable.ic_up_arrow)
            binding.creatorChannelView.channelDescriptionTextView.toggle()
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            addBioButton -> {
//                findNavController().navigate(R.id.action_menu_channel_to_challengesFragment)
                val direction = CreatorChannelFragmentDirections.actionMenuChannelToCreatorChannelEditFragment(viewModel.channelInfo.value?.myChannelDetail)
                findNavController().navigate(direction)
            }

            editButton -> {
                val direction = CreatorChannelFragmentDirections.actionMenuChannelToCreatorChannelEditFragment(viewModel.channelInfo.value?.myChannelDetail)
                findNavController().navigate(direction)
            }

            ratingButton -> showRatingDialog()
            analyticsButton -> showCreatePlaylistDialog()
        }
    }

    private fun showRatingDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(layout.alert_dialog_channel_rating, null)
        dialogBuilder.setView(dialogView)

        dialogView.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            dialogView.submitButton.isEnabled = rating > 0
            Log.d("TAG", "showRatingDialog: $rating")
        }

        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        dialogView.submitButton.setOnClickListener { alertDialog.dismiss() }
    }

    private fun showCreatePlaylistDialog() {
        val playlistBinding = AlertDialogCreatePlaylistBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(playlistBinding.root)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        playlistBinding.viewModel = createPlaylistViewModel
        playlistBinding.createButton.setOnClickListener {
            if (!createPlaylistViewModel.playlistName.isNullOrEmpty()) {
                observeCreatePlaylist()
                createPlaylistViewModel.createPlaylist(isOwner, channelId)
                alertDialog.dismiss()
            }
            else {
                Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.liveData) {
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

    private fun observeDeletePlaylist() {
        observe(deletePlaylistViewModel.liveData) {
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

}