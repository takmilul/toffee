package com.banglalink.toffee.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.AlertDialogReactionsBinding
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReactionFragment: Fragment() {

    private var isLoveReacted = false
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
        @JvmStatic fun newInstance(reactionIconView: View, reactionCountView: View, channelInfo: ChannelInfo, isLoveReacted: Boolean = false): ReactionFragment {
            val instance = ReactionFragment()
            instance.reactionCountView = reactionCountView as TextView
            instance.reactionIconView = reactionIconView as TextView
            instance.isLoveReacted = isLoveReacted
            instance.channelInfo = channelInfo
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlertDialogReactionsBinding.inflate(this.layoutInflater)
        binding.data = channelInfo
        
        lifecycleScope.launch {
            val reactionInfo = reactionDao.getReactionByContentId(preference.customerId, channelInfo.id)

            if (isLoveReacted){
                reactionInfo ?: react(Reaction.Love)
                reactionIconView.text = Reaction.Love.name
                reactionIconView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reaction_love_filled, 0, 0, 0)
            }
            else {
                val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
                alertDialog = dialogBuilder.create().apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                with(binding) {
                    likeButton.setOnClickListener { reactionButton ->
                        reactionInfo ?: react(Reaction.Like, reactionButton)
                        alertDialog.dismiss()
                    }
                    loveButton.setOnClickListener { reactionButton ->
                        reactionInfo ?: react(Reaction.Love, reactionButton)
                        reactionIconView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reaction_love_filled, 0, 0, 0)
                        alertDialog.dismiss()
                    }
                    hahaButton.setOnClickListener { reactionButton ->
                        reactionInfo ?: react(Reaction.HaHa, reactionButton)
                        alertDialog.dismiss()
                    }
                    wowButton.setOnClickListener { reactionButton ->
                        reactionInfo ?: react(Reaction.Wow, reactionButton)
                        alertDialog.dismiss()
                    }
                    sadButton.setOnClickListener { reactionButton ->
                        reactionInfo ?: react(Reaction.Sad, reactionButton)
                        alertDialog.dismiss()
                    }
                    angryButton.setOnClickListener { reactionButton ->
                        reactionInfo ?: react(Reaction.Angry, reactionButton)
                        alertDialog.dismiss()
                    }
                }
                alertDialog.show()
            }
        }
    }
    
    private fun react(reaction: Reaction, reactButton: View? = null) {
        val reactionInfo = ReactionInfo(null, preference.customerId, channelInfo.id, reaction.value)
        mViewModel.insert(reactionInfo)
        mViewModel.insertActivity(preference.customerId, channelInfo, reaction.value)
        val react = channelInfo.reaction?.run {
            like + love + haha + wow + sad + angry + 1
        } ?: 1L
        channelInfo.myReaction = reaction.value
        reactionCountView.text = Utils.getFormattedViewsText(react.toString())
        reactButton?.let {
            reactionIconView.text = reaction.name
            reactionIconView.setCompoundDrawablesWithIntrinsicBounds((reactButton as ImageView).drawable, null, null, null)
        }
    }
}