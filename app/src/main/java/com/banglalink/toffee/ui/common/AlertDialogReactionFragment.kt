package com.banglalink.toffee.ui.common

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.storage.Preference
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

    private lateinit var reactionCountTextView: TextView
    private lateinit var channelInfo: ChannelInfo
    private lateinit var alertDialog: AlertDialog
    @Inject lateinit var preference: Preference
    @Inject lateinit var reactionDao: ReactionDao
    val mViewModel by viewModels<ReactionViewModel>()
    private lateinit var binding: AlertDialogReactionsBinding
    
    companion object {
        @JvmStatic fun newInstance(reactView: View, channelInfo: ChannelInfo): AlertDialogReactionFragment {
            val instance = AlertDialogReactionFragment()
            instance.reactionCountTextView = reactView as TextView
            instance.channelInfo = channelInfo
            return instance
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AlertDialogReactionsBinding.inflate(this.layoutInflater)
        binding.data = channelInfo
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        lifecycleScope.launch {
            val reactionInfo = reactionDao.getReactionByContentId(preference.customerId, channelInfo.id)

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
        val reactionInfo = ReactionInfo(null, preference.customerId, channelInfo.id, reaction.value)
        mViewModel.insert(reactionInfo)
        mViewModel.insertActivity(preference.customerId, channelInfo, reaction.value)
        val react = channelInfo.reaction?.run {
            like + love + haha + wow + sad + angry + 1
        } ?: 1L
        channelInfo.myReaction = reaction.value
        reactionCountTextView.text = Utils.getFormattedViewsText(react.toString())
        reactionCountTextView.setCompoundDrawablesWithIntrinsicBounds((reactButton as ImageView).drawable, null, null, null)
    }
}