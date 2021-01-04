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
            likeButton.setOnClickListener {
                react(Like, R.drawable.ic_reaction_like_no_shadow)
                alertDialog.dismiss()
            }
            loveButton.setOnClickListener {
                react(Love, R.drawable.ic_reaction_love_no_shadow)
                alertDialog.dismiss()
            }
            hahaButton.setOnClickListener {
                react(HaHa, R.drawable.ic_reaction_haha_no_shadow)
                alertDialog.dismiss()
            }
            wowButton.setOnClickListener {
                react(Wow, R.drawable.ic_reaction_wow_no_shadow)
                alertDialog.dismiss()
            }
            sadButton.setOnClickListener {
                react(Sad, R.drawable.ic_reaction_sad_no_shadow)
                alertDialog.dismiss()
            }
            angryButton.setOnClickListener {
                react(Angry, R.drawable.ic_reaction_angry_no_shadow)
                alertDialog.dismiss()
            }
        }
        return alertDialog
    }
    
    private fun react(reaction: Reaction, reactIcon: Int) {
        if (channelInfo != null && reactionIconView != null && reactionCountView != null) {
            lifecycleScope.launchWhenStarted {
                val previousReactionInfo = reactionDao.getReactionByContentId(preference.customerId, channelInfo!!.id)
                val newReactionInfo = ReactionInfo(null, preference.customerId, channelInfo!!.id, reaction.value)
                var reactionCount = channelInfo!!.reaction?.run {
                    like + love + haha + wow + sad + angry
                } ?: 0L
                var reactionText = reaction.name
                var reactionIcon = reactIcon

                channelInfo!!.myReaction = previousReactionInfo?.let {
                    if (previousReactionInfo.reaction == newReactionInfo.reaction) {
                        mViewModel.removeReaction(previousReactionInfo)
                        reactionText = "React"
                        reactionIcon = R.drawable.ic_reaction_love_empty
                        None.value
                    }
                    else {
                        mViewModel.updateReaction(newReactionInfo)
                        reactionCount++
                        reaction.value
                    }
                } ?: let {
                    mViewModel.insertReaction(newReactionInfo)
                    mViewModel.insertActivity(preference.customerId, channelInfo!!, reaction.value)
                    reactionCount++
                    reaction.value
                }

                reactionCountView!!.text = Utils.getFormattedViewsText(reactionCount.toString())
                reactionIconView!!.text = reactionText
                reactionIconView!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color))
                reactionIconView!!.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, 0, 0, 0)
                if (reactionText == Love.name) {
                    reactionIconView!!.setTextColor(Color.RED)
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