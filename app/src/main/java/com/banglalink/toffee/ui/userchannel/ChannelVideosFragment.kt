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
import com.banglalink.toffee.data.storage.Preference
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

    private var enableToolbar: Boolean = false

    @Inject
    lateinit var reactionDao: ReactionDao

    @Inject
    lateinit var pref: Preference
    private var mReactionInfo: ReactionInfo? = null
    override val mAdapter by lazy { ChannelVideoListAdapter(this) }
    override val mViewModel by viewModels<ChannelVideosViewModel>()

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        fun newInstance(enableToolbar: Boolean): ChannelVideosFragment {
            val instance = ChannelVideosFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        }
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_videos_empty, "You haven't uploaded any video yet")
    }

    override fun onReactionClicked(view: View, item: ChannelInfo) {
        super.onReactionClicked(view, item)
        showReactionDialog(view, item)
    }

    private fun showReactionDialog(reactView: View, item: ChannelInfo) {
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
                likeButton.setOnClickListener {button->
                    reactionInfo?: react(reactView, button, item.id, Like)
                    alertDialog.dismiss()
                }
                loveButton.setOnClickListener {button->
                    reactionInfo?: react(reactView, button, item.id, Love)
                    alertDialog.dismiss()
                }
                hahaButton.setOnClickListener {button->
                    reactionInfo?: react(reactView, button, item.id, HaHa)
                    alertDialog.dismiss()
                }
                wowButton.setOnClickListener {button->
                    reactionInfo?: react(reactView, button, item.id, Wow)
                    alertDialog.dismiss()
                }
                sadButton.setOnClickListener {button->
                    reactionInfo?: react(reactView, button, item.id, Sad)
                    alertDialog.dismiss()
                }
                angryButton.setOnClickListener {button->
                    reactionInfo?: react(reactView, button, item.id, Angry)
                    alertDialog.dismiss()
                }
            }
        }
    }

    private fun react(reactView: View, reactButton: View, contentId: String, reaction: Reaction) {
        val reactionInfo = ReactionInfo(null, contentId, reaction.value)
        mViewModel.insert(reactionInfo)
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
}