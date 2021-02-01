package com.banglalink.toffee.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.AlertDialogReactionsBinding
import com.banglalink.toffee.enums.ActivityType.*
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.None
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReactionPopup: Fragment() {

    private var reactionIconCallback: ReactionIconCallback? = null
    private var channelInfo: ChannelInfo? = null
    @Inject lateinit var preference: Preference
    @Inject lateinit var reactionDao: ReactionDao
    private var location: IntArray? = null
    private val mViewModel by viewModels<ReactionViewModel>()
    private lateinit var binding: AlertDialogReactionsBinding

    companion object {
        const val CHANNEL_INFO = "channelInfo"
        const val LOCATION = "location"
        const val TAG = "reaction_fragment"
        
        @JvmStatic fun newInstance(channelInfo: ChannelInfo, location: IntArray?): ReactionPopup {
            return ReactionPopup().apply {
                arguments = Bundle().apply { 
                    putParcelable(CHANNEL_INFO, channelInfo)
                    putIntArray(LOCATION, location)
                }
            }
        }
    }

    fun setCallback(reactionIconCallback: ReactionIconCallback) {
        this.reactionIconCallback = reactionIconCallback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelInfo = requireArguments().getParcelable(CHANNEL_INFO)
        location = requireArguments().getIntArray(LOCATION)
        
        val binding = AlertDialogReactionsBinding.inflate(this.layoutInflater)
        binding.data = channelInfo
        binding.root.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec. UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec. UNSPECIFIED))
        val reactionPopupWindow = PopupWindow(requireContext())
        with(reactionPopupWindow){
            contentView = binding.root
            isTouchable = true
            isFocusable = true
            isOutsideTouchable = true
            elevation = 48F
            val x = parentFragment?.view?.width?.minus(binding.root.measuredWidth)?.div(2)?: 50
            val y = location?.get(1)?.minus(binding.root.measuredHeight + 10)?:0
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            parentFragment?.view?.let {
                showAtLocation(it, Gravity.NO_GRAVITY, x, y)
            }
        }
        with(binding){
            likeButton.setOnClickListener { reactionPopupWindow.dismiss() }
        }
    }

    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AlertDialogReactionsBinding.inflate(this.layoutInflater)
        binding.data = channelInfo
        return binding.root
    }*/

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(binding) {
            likeButton.setOnClickListener { react(Like, R.drawable.ic_reaction_like_no_shadow) }
            loveButton.setOnClickListener { react(Love, R.drawable.ic_reaction_love_no_shadow) }
            hahaButton.setOnClickListener { react(HaHa, R.drawable.ic_reaction_haha_no_shadow) }
            wowButton.setOnClickListener { react(Wow, R.drawable.ic_reaction_wow_no_shadow) }
            sadButton.setOnClickListener { react(Sad, R.drawable.ic_reaction_sad_no_shadow) }
            angryButton.setOnClickListener { react(Angry, R.drawable.ic_reaction_angry_no_shadow) }
        }
    }*/
    
    private fun react(reaction: Reaction, reactIcon: Int) {
//        dialog?.dismiss()
        channelInfo?.let {
            lifecycleScope.launchWhenStarted {
                val previousReactionInfo = reactionDao.getReactionByContentId(preference.customerId, channelInfo!!.id)
                val newReactionInfo = ReactionInfo(null, preference.customerId, channelInfo!!.id, reaction.value)
                var reactionCount = channelInfo!!.reaction?.run {
                    like + love + haha + wow + sad + angry
                } ?: 0L
                var reactionText = reaction.name
                var reactionIcon = reactIcon

                channelInfo!!.myReaction = previousReactionInfo?.let {
                    if (it.reaction == newReactionInfo.reaction) {
                        reactionText = "React"
                        reactionIcon = R.drawable.ic_reaction_love_empty
                        mViewModel.removeReaction(it)
                        mViewModel.insertActivity(preference.customerId, channelInfo!!, REACTION_REMOVED.value, reaction.value)
                        None.value
                    }
                    else {
                        reactionCount++
                        mViewModel.updateReaction(newReactionInfo)
                        mViewModel.insertActivity(preference.customerId, channelInfo!!, REACTION_CHANGED.value, reaction.value)
                        reaction.value
                    }
                } ?: run {
                    mViewModel.insertReaction(newReactionInfo)
                    mViewModel.insertActivity(preference.customerId, channelInfo!!, REACTED.value, reaction.value)
                    reactionCount++
                    reaction.value
                }

                reactionIconCallback?.onReactionChange(Utils.getFormattedViewsText(reactionCount.toString()), reactionText, reactionIcon)
            }
        }
    }

    override fun onDestroy() {
        reactionIconCallback = null
//        dialog?.dismiss()
        super.onDestroy()
    }
}