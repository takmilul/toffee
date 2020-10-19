package com.banglalink.toffee.ui.userchannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_dialog_reactions.view.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ChannelVideosFragment : BaseListFragment<ChannelInfo>(), ContentReactionCallback<ChannelInfo> {

    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var enableToolbar: Boolean = false

    @Inject
    lateinit var reactionDao: ReactionDao

    override val mAdapter by lazy { ChannelVideoListAdapter(this) }
//    override val mViewModel by viewModels<ChannelVideosViewModel>()

    @Inject lateinit var viewModelAssistedFactory: ChannelVideosViewModel.AssistedFactory
    override val mViewModel by viewModels<ChannelVideosViewModel> { ChannelVideosViewModel.provideFactory(viewModelAssistedFactory, isOwner, channelId) }

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        private const val IS_OWNER = "isOwner"
        private const val CHANNEL_ID = "channelId"
        fun newInstance(enableToolbar: Boolean, isOwner: Int, channelId: Int): ChannelVideosFragment {
            val instance = ChannelVideosFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_ID, channelId)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isOwner = arguments?.getInt(IS_OWNER) ?: 0
        channelId = arguments?.getInt(CHANNEL_ID) ?: 0

    }
    
    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_videos_empty, "You haven't uploaded any video yet")
    }

    override fun onReactionClicked(view: View, position: Int, item: ChannelInfo) {
        super.onReactionClicked(view, position, item)
        showReactionDialog(view, position, item)
    }

    private fun showReactionDialog(reactView: View, position: Int, item: ChannelInfo) {
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_reactions, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

        lifecycleScope.launch {
            val reactionInfo = reactionDao.getReactionByContentId(item.id)

            if (reactionInfo?.contentId == item.id) {
                setPreviousReaction(reactionInfo, dialogView)
            }

            with(dialogView) {
                likeButton.setOnClickListener {reactionButton->
                    reactionInfo?: react(item, position, reactView, reactionButton, item.id, Like)
                    alertDialog.dismiss()
                }
                loveButton.setOnClickListener {reactionButton->
                    reactionInfo?: react(item, position, reactView, reactionButton, item.id, Love)
                    alertDialog.dismiss()
                }
                hahaButton.setOnClickListener {reactionButton->
                    reactionInfo?: react(item, position, reactView, reactionButton, item.id, HaHa)
                    alertDialog.dismiss()
                }
                wowButton.setOnClickListener {reactionButton->
                    reactionInfo?: react(item, position, reactView, reactionButton, item.id, Wow)
                    alertDialog.dismiss()
                }
                sadButton.setOnClickListener {reactionButton->
                    reactionInfo?: react(item, position, reactView, reactionButton, item.id, Sad)
                    alertDialog.dismiss()
                }
                angryButton.setOnClickListener {reactionButton->
                    reactionInfo?: react(item, position, reactView, reactionButton, item.id, Angry)
                    alertDialog.dismiss()
                }
            }
        }
    }

    private fun react(item: ChannelInfo, position: Int, reactView: View, reactButton: View, contentId: String, reaction: Reaction) {
        val reactionInfo = ReactionInfo(null, contentId, reaction.value)
        mViewModel.insert(reactionInfo)
        mAdapter.getItemByIndex(position)?.reaction = getReactionIcon(reaction.value)
        reactButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.teal_round_bg)
        (reactView as ImageView).setImageDrawable((reactButton as ImageView).drawable)
    }

    private fun setPreviousReaction(reactionInfo: ReactionInfo?, dialogView: View) {
        val background = ContextCompat.getDrawable(requireContext(), R.drawable.teal_round_bg)
        when (reactionInfo?.reaction) {
            Like.value -> dialogView.likeButton.background = background
            Love.value -> dialogView.loveButton.background = background
            HaHa.value -> dialogView.hahaButton.background = background
            Wow.value -> dialogView.wowButton.background = background
            Sad.value -> dialogView.sadButton.background = background
            Angry.value -> dialogView.angryButton.background = background
        }
    }

    private fun getReactionIcon(reaction: Int?): Int? {
        return when (reaction) {
            Like.value -> R.drawable.ic_reaction_like
            Love.value -> R.drawable.ic_reaction_love
            HaHa.value -> R.drawable.ic_reaction_haha
            Wow.value -> R.drawable.ic_reaction_wow
            Sad.value -> R.drawable.ic_reaction_sad
            Angry.value -> R.drawable.ic_reaction_angry
            else -> R.drawable.ic_like_emo
        }
    }

}