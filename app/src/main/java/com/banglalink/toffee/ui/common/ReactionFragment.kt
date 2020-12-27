package com.banglalink.toffee.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.AlertDialogReactionsBinding
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReactionFragment: Fragment() {

    private var isSingleTapped = false
    private lateinit var reactionCountView: TextView
    private lateinit var reactionIconView: TextView
    private lateinit var channelInfo: ChannelInfo
    private lateinit var alertDialog: AlertDialog
    @Inject lateinit var preference: Preference
    @Inject lateinit var reactionDao: ReactionDao
    private val mViewModel by viewModels<ReactionViewModel>()
    private lateinit var binding: AlertDialogReactionsBinding

    companion object {
        const val TAG = "reaction_fragment"
        @JvmStatic fun newInstance(reactionIconView: View, reactionCountView: View, channelInfo: ChannelInfo, isSingleTapped: Boolean = false): ReactionFragment {
            val instance = ReactionFragment()
            instance.reactionCountView = reactionCountView as TextView
            instance.reactionIconView = reactionIconView as TextView
            instance.isSingleTapped = isSingleTapped
            instance.channelInfo = channelInfo
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlertDialogReactionsBinding.inflate(this.layoutInflater)
        binding.data = channelInfo
        
        lifecycleScope.launch {
            
            if (isSingleTapped){
                react(Reaction.Love)
            }
            else {
                val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
                alertDialog = dialogBuilder.create().apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                with(binding) {
                    likeButton.setOnClickListener { reactionButton ->
                        react(Reaction.Like, reactionButton)
                        alertDialog.dismiss()
                    }
                    loveButton.setOnClickListener { reactionButton ->
                        react(Reaction.Love, reactionButton)
                        alertDialog.dismiss()
                    }
                    hahaButton.setOnClickListener { reactionButton ->
                        react(Reaction.HaHa, reactionButton)
                        alertDialog.dismiss()
                    }
                    wowButton.setOnClickListener { reactionButton ->
                        react(Reaction.Wow, reactionButton)
                        alertDialog.dismiss()
                    }
                    sadButton.setOnClickListener { reactionButton ->
                        react(Reaction.Sad, reactionButton)
                        alertDialog.dismiss()
                    }
                    angryButton.setOnClickListener { reactionButton ->
                        react(Reaction.Angry, reactionButton)
                        alertDialog.dismiss()
                    }
                }
                alertDialog.show()
            }
        }
    }
    
    private fun react(reaction: Reaction, reactButton: View? = null) {
        lifecycleScope.launchWhenStarted {
            val previousReactionInfo = reactionDao.getReactionByContentId(preference.customerId, channelInfo.id)
            val newReactionInfo = ReactionInfo(null, preference.customerId, channelInfo.id, reaction.value)
            var reactionCount = 0L
            var reactionText = reaction.name
            var reactionIcon = reactButton?.let { (reactButton as ImageView).drawable }
            reactionIconView.setTextColor(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color))

            channelInfo.myReaction = previousReactionInfo?.let { 
                if (previousReactionInfo.reaction == newReactionInfo.reaction || isSingleTapped){
                    mViewModel.removeReaction(previousReactionInfo)
                    reactionText = "React"
                    reactionCount = channelInfo.reaction?.run {
                        like + love + haha + wow + sad + angry
                    } ?: 0L
                    reactionIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_reaction_love_empty)
                    Reaction.None.value
                }
                else {
                    mViewModel.updateReaction(newReactionInfo)
                    if (reaction == Love) {
                        reactionIconView.setTextColor(Color.RED)
                        reactionIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_reaction_love_filled)
                    }
                    reactionCount = channelInfo.reaction?.run {
                        like + love + haha + wow + sad + angry + 1
                    } ?: 1L
                    reaction.value
                }
            } ?: let{ 
                mViewModel.insertReaction(newReactionInfo)
                mViewModel.insertActivity(preference.customerId, channelInfo, reaction.value)
                reactionCount = channelInfo.reaction?.run {
                    like + love + haha + wow + sad + angry + 1
                } ?: 1L
                if (reaction == Love) {
                    reactionIconView.setTextColor(Color.RED)
                    reactionIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_reaction_love_filled)
                }
                reaction.value
            }
            
            reactionCountView.text = Utils.getFormattedViewsText(reactionCount.toString())
            reactButton.let {
                reactionIconView.text = reactionText
                reactionIconView.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, null, null, null)
            }
        }
    }
}