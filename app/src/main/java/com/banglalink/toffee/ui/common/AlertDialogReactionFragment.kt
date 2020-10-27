package com.banglalink.toffee.ui.common

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.databinding.AlertDialogReactionsBinding
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlertDialogReactionFragment : DialogFragment(), ContentReactionCallback<ChannelInfo> {

//    private var position: Int = 0
//    private lateinit var reactionImageView: ImageView
    private lateinit var reactionCountTextView: TextView
    private lateinit var channelInfo: ChannelInfo
    private lateinit var alertDialog: AlertDialog
//    private lateinit var mAdapter: BasePagingDataAdapter<ChannelInfo>
    
    @Inject lateinit var reactionDao: ReactionDao
    val mViewModel by viewModels<ReactionViewModel>()
    
    companion object {
        fun newInstance(): AlertDialogReactionFragment {
            return AlertDialogReactionFragment()
        }
    }
    
    // must call this function and set data before show the alert dialog
    fun setItem(/*adapter: BasePagingDataAdapter<ChannelInfo>, */reactView: View, channelInfo: ChannelInfo) {
//        val layout = reactView as LinearLayout
//        this.reactionImageView = layout.getChildAt(0) as ImageView
        this.reactionCountTextView = reactView as TextView
//        this.position = position
        this.channelInfo = channelInfo
//        this.mAdapter = adapter
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = AlertDialogReactionsBinding.inflate(this.layoutInflater)
        binding.data = channelInfo
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        lifecycleScope.launch {
            val reactionInfo = reactionDao.getReactionByContentId(channelInfo.id)

            if (reactionInfo?.contentId == channelInfo.id) {
                setPreviousReaction(reactionInfo, binding)
            }

            with(binding) {
                likeButton.setOnClickListener { reactionButton ->
                    reactionInfo ?: react(reactionButton, Like)
                    alertDialog.dismiss()
                }
                loveButton.setOnClickListener { reactionButton ->
                    reactionInfo ?: react(reactionButton, Love)
                    alertDialog.dismiss()
                }
                hahaButton.setOnClickListener { reactionButton ->
                    reactionInfo ?: react(reactionButton, HaHa)
                    alertDialog.dismiss()
                }
                wowButton.setOnClickListener { reactionButton ->
                    reactionInfo ?: react(reactionButton, Wow)
                    alertDialog.dismiss()
                }
                sadButton.setOnClickListener { reactionButton ->
                    reactionInfo ?: react(reactionButton, Sad)
                    alertDialog.dismiss()
                }
                angryButton.setOnClickListener { reactionButton ->
                    reactionInfo ?: react(reactionButton, Angry)
                    alertDialog.dismiss()
                }
            }
        }
        return alertDialog
    }

    private fun react(reactButton: View, reaction: Reaction) {
        val reactionInfo = ReactionInfo(null, channelInfo.id, reaction.value)
        mViewModel.insert(reactionInfo)
        mViewModel.insertActivity(channelInfo, reaction.value)
        val react = channelInfo.reaction?.run {
            like + love + haha + wow + sad + angry + 1
        } ?: 1L
        /*mAdapter.getItemByIndex(position)?*/channelInfo.userReaction = reaction.value
        /*mAdapter.getItemByIndex(position)?*/channelInfo.userReactionIcon = setReactionIcon(reaction.value)
        reactButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.teal_round_bg)
        reactionCountTextView.text = Utils.getFormattedViewsText(react.toString())
        reactionCountTextView.setCompoundDrawablesWithIntrinsicBounds((reactButton as ImageView).drawable, null, null, null)
    }

    private fun setPreviousReaction(reactionInfo: ReactionInfo?, binding: AlertDialogReactionsBinding) {
        val background = ContextCompat.getDrawable(requireContext(), R.drawable.teal_round_bg)
        when (reactionInfo?.reaction) {
            Like.value -> binding.likeButton.background = background
            Love.value -> binding.loveButton.background = background
            HaHa.value -> binding.hahaButton.background = background
            Wow.value -> binding.wowButton.background = background
            Sad.value -> binding.sadButton.background = background
            Angry.value -> binding.angryButton.background = background
        }
    }
}