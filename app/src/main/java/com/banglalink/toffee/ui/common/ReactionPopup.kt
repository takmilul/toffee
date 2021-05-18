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
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.AlertDialogReactionsBinding
import com.banglalink.toffee.enums.ActivityType.*
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.*
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReactionPopup: Fragment() {

    private var reactionIconCallback: ReactionIconCallback? = null
    private var channelInfo: ChannelInfo? = null
    @Inject lateinit var preference: SessionPreference
    @Inject lateinit var reactionDao: ReactionDao
    private val mViewModel by viewModels<ReactionViewModel>()
    private var reactionPopupWindow: PopupWindow? = null
    private lateinit var binding :AlertDialogReactionsBinding
    
    companion object {
        const val CHANNEL_INFO = "channelInfo"
        const val ICON_LOCATION = "icon_location"
        const val ICON_HEIGHT = "icon_height"
        const val SHOW_BELOW = "show_below"
        const val TAG = "reaction_fragment"
        
        @JvmStatic fun newInstance(channelInfo: ChannelInfo, iconLocation: IntArray, iconHeight: Int, showPopupBelow: Boolean = false): ReactionPopup {
            return ReactionPopup().apply {
                arguments = Bundle().apply { 
                    putParcelable(CHANNEL_INFO, channelInfo)
                    putIntArray(ICON_LOCATION, iconLocation)
                    putInt(ICON_HEIGHT, iconHeight)
                    putBoolean(SHOW_BELOW, showPopupBelow)
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
        val iconLocation: IntArray = requireArguments().getIntArray(ICON_LOCATION) ?: intArrayOf(0, 0)
        val iconHeight = requireArguments().getInt(ICON_HEIGHT)
        val isShowPopupBelow = requireArguments().getBoolean(SHOW_BELOW)
        
        binding = AlertDialogReactionsBinding.inflate(this.layoutInflater)
        binding.data = channelInfo
//        binding.root.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in_animation)
        binding.root.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec. UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec. UNSPECIFIED))
        
        val topLocation = iconLocation[1].minus(binding.root.measuredHeight + 10)
        val bottomLocation = iconLocation[1].plus(iconHeight + 8)
        
        val x = parentFragment?.view?.width?.minus(binding.root.measuredWidth)?.div(2) ?: 0
        val y = if ((isShowPopupBelow && bottomLocation + binding.root.measuredHeight < parentFragment?.view?.height?:0) || topLocation < binding.root.measuredHeight) {
            bottomLocation
        } else {
            topLocation
        }
        
        reactionPopupWindow = PopupWindow(requireContext())
        
        with(reactionPopupWindow!!){
            contentView = binding.root
            isTouchable = true
            isFocusable = true
            isOutsideTouchable = true
            elevation = 48F
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            parentFragment?.view?.let {
                animationStyle = R.style.ZoomAnimation
                showAtLocation(it, Gravity.NO_GRAVITY, x, y)
            }
        }
        
        with(binding){
            likeButton.setOnClickListener { react(Like, R.drawable.ic_reaction_like_no_shadow) }
            loveButton.setOnClickListener { react(Love, R.drawable.ic_reaction_love_no_shadow) }
            hahaButton.setOnClickListener { react(HaHa, R.drawable.ic_reaction_haha_no_shadow) }
            wowButton.setOnClickListener { react(Wow, R.drawable.ic_reaction_wow_no_shadow) }
            sadButton.setOnClickListener { react(Sad, R.drawable.ic_reaction_sad_no_shadow) }
            angryButton.setOnClickListener { react(Angry, R.drawable.ic_reaction_angry_no_shadow) }
        }
    }

    private fun react(reaction: Reaction, reactIcon: Int) {
        requireActivity().checkVerification {
            reactionPopupWindow?.dismiss()
            channelInfo?.let { info ->
                lifecycleScope.launchWhenStarted {
                    val previousReactionInfo =
                        reactionDao.getReactionByContentId(preference.customerId, info.id.toLong())
                    val newReactionInfo =
                        ReactionInfo(null, preference.customerId, info.id.toLong(), reaction.value)
                    var reactionCount = info.reaction?.run {
                        like + love + haha + wow + sad + angry
                    } ?: 0L
                    var reactionText = reaction.name
                    var reactionIcon = reactIcon

                    info.myReaction = previousReactionInfo?.let {
                        if (it.reactionType == newReactionInfo.reactionType) {
                            reactionText = "React"
                            reactionIcon = R.drawable.ic_reaction_love_empty
                            mViewModel.removeReaction(it)
                            mViewModel.insertActivity(
                                preference.customerId,
                                info,
                                REACTION_REMOVED.value,
                                reaction.value
                            )
                            None.value
                        } else {
                            reactionCount++
                            mViewModel.updateReaction(newReactionInfo, it)
                            mViewModel.insertActivity(
                                preference.customerId,
                                info,
                                REACTION_CHANGED.value,
                                reaction.value
                            )
                            reaction.value
                        }
                    } ?: run {
                        mViewModel.insertReaction(newReactionInfo)
                        mViewModel.insertActivity(
                            preference.customerId,
                            info,
                            REACTED.value,
                            reaction.value
                        )
                        reactionCount++
                        reaction.value
                    }

                    reactionIconCallback?.onReactionChange(
                        Utils.getFormattedViewsText(reactionCount.toString()),
                        reactionText,
                        reactionIcon
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        reactionIconCallback = null
        reactionPopupWindow?.dismiss()
        super.onDestroy()
    }
}