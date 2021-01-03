package com.banglalink.toffee.ui.common

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.AlertDialogReactionsBinding
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.getReactionIcon
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReactionFragment: DialogFragment() {

    private var reactionCountView: TextView? = null
    private var reactionIconView: TextView? = null
    private var channelInfo: ChannelInfo? = null
    private lateinit var alertDialog: AlertDialog
    @Inject lateinit var preference: Preference
    @Inject lateinit var reactionDao: ReactionDao
    private val mViewModel by viewModels<ReactionViewModel>()
    private lateinit var binding: AlertDialogReactionsBinding

    companion object {
        const val REACTION_ICON_VIEW_ID = "reactionIconViewId"
        const val REACTION_COUNT_VIEW_ID = "reactionCountViewId"
        const val CHANNEL_INFO = "channelInfo"
        const val TAG = "reaction_fragment"
        
        @JvmStatic fun newInstance(channelInfo: ChannelInfo): ReactionFragment {
            return ReactionFragment().apply {
                arguments = Bundle().apply { 
                    putParcelable(CHANNEL_INFO, channelInfo)
                }
            }
        }
    }

    fun setView(iconView: View, countView: View) {
        reactionIconView = iconView as TextView
        reactionCountView = countView as TextView
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        
        channelInfo = requireArguments().getParcelable(CHANNEL_INFO)

        binding = AlertDialogReactionsBinding.inflate(this.layoutInflater)
        binding.data = channelInfo

        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        with(binding) {
            likeButton.setOnClickListener { reactionButton ->
                react(Like, reactionButton)
                alertDialog.dismiss()
            }
            loveButton.setOnClickListener { reactionButton ->
                react(Love, reactionButton)
                alertDialog.dismiss()
            }
            hahaButton.setOnClickListener { reactionButton ->
                react(HaHa, reactionButton)
                alertDialog.dismiss()
            }
            wowButton.setOnClickListener { reactionButton ->
                react(Wow, reactionButton)
                alertDialog.dismiss()
            }
            sadButton.setOnClickListener { reactionButton ->
                react(Sad, reactionButton)
                alertDialog.dismiss()
            }
            angryButton.setOnClickListener { reactionButton ->
                react(Angry, reactionButton)
                alertDialog.dismiss()
            }
        }
        return alertDialog
    }
    
    private fun react(reaction: Reaction, reactButton: View? = null) {
        if (channelInfo != null && reactionIconView != null && reactionCountView != null) {
            lifecycleScope.launchWhenStarted {
                val previousReactionInfo = reactionDao.getReactionByContentId(preference.customerId, channelInfo!!.id)
                val newReactionInfo = ReactionInfo(null, preference.customerId, channelInfo!!.id, reaction.value)
                var reactionCount = 0L
                val generatedReaction = getReactionIcon(reactionIconView!!, reaction.value)
                var reactionText = generatedReaction.first
                var reactionIcon = generatedReaction.second
                reactionIconView!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color))

                channelInfo!!.myReaction = previousReactionInfo?.let {
                    if (previousReactionInfo.reaction == newReactionInfo.reaction) {
                        mViewModel.removeReaction(previousReactionInfo)
                        reactionText = "React"
                        reactionCount = channelInfo!!.reaction?.run {
                            like + love + haha + wow + sad + angry
                        } ?: 0L
                        reactionIcon = R.drawable.ic_reaction_love_empty
                        None.value
                    }
                    else {
                        mViewModel.updateReaction(newReactionInfo)
                        reactionCount = channelInfo!!.reaction?.run {
                            like + love + haha + wow + sad + angry + 1
                        } ?: 1L
                        reaction.value
                    }
                } ?: let {
                    mViewModel.insertReaction(newReactionInfo)
                    mViewModel.insertActivity(preference.customerId, channelInfo!!, reaction.value)
                    reactionCount = channelInfo!!.reaction?.run {
                        like + love + haha + wow + sad + angry + 1
                    } ?: 1L
                    reaction.value
                }

                reactionCountView!!.text = Utils.getFormattedViewsText(reactionCount.toString())
                reactButton.let {
                    reactionIconView!!.text = reactionText
                    reactionIconView!!.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, 0, 0, 0)
                }
            }
        }
    }

    override fun onDestroy() {
        reactionIconView = null
        reactionCountView = null
        alertDialog.dismiss()
        super.onDestroy()
    }
}