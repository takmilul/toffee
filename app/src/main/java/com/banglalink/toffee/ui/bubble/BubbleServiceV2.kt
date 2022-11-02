package com.banglalink.toffee.ui.bubble

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.BubbleViewV2LayoutBinding
import com.banglalink.toffee.extension.ifNotBlank
import com.banglalink.toffee.model.BubbleConfig
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemGravity
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemTouchEvent
import com.banglalink.toffee.ui.bubble.listener.IBubbleDraggableWindowItemEventListener
import com.banglalink.toffee.ui.bubble.listener.IBubbleInteractionListener
import com.banglalink.toffee.ui.bubble.util.isInBounds
import com.banglalink.toffee.ui.bubble.view.BubbleCloseItem
import com.banglalink.toffee.ui.bubble.view.BubbleDraggableItem
import com.banglalink.toffee.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit.*

class BubbleServiceV2 : BaseBubbleService(), IBubbleDraggableWindowItemEventListener, IBubbleInteractionListener {
    
    private var bubbleConfig: BubbleConfig? = null
    private val coroutineScope = CoroutineScope(Default)
    private lateinit var binding: BubbleViewV2LayoutBinding
    
    override fun createBubble(): Bubble {
        return Bubble.Builder()
            .with(this)
            .setDraggableItem(createDraggableItem())
            .setRemoveItem(createRemoveItem())
            .setListener(this)
            .build()
    }
    
    private fun createDraggableItem(): BubbleDraggableItem {
        binding = BubbleViewV2LayoutBinding.inflate(LayoutInflater.from(this))

        coroutineScope.launch {
            bubbleConfig = bubbleConfigRepository.getLatestConfig()
        }
        if (mPref.bubbleConfigLiveData.value == null) {
            coroutineScope.launch {
                bubbleConfig = bubbleConfigRepository.getLatestConfig()
                mPref.bubbleConfigLiveData.postValue(bubbleConfig)
            }
        }
        mPref.bubbleConfigLiveData.observeForever { bubbleConfig ->
            try {
                binding.poweredByText.text = bubbleConfig?.poweredBy
                bubbleConfig?.poweredByIconUrl.ifNotBlank {
                    binding.poweredByImage.load(it)
                }
                if (bubbleConfig?.isGlobalCountDownActive == true) {
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val endDateDay = bubbleConfig.countDownEndTime ?: "2022-11-21 16:00:00"
                    val endDate = format.parse(endDateDay)
                    //milliseconds
                    val different = endDate?.time?.minus(mPref.getSystemTime().time) ?: 0L
                    binding.leftSideImage.visibility = View.GONE
                    this.bubbleConfig = bubbleConfig
                    bubbleConfig.adIconUrl.ifNotBlank {
                        binding.homeTeamFlag.load(it)
                    }
                    binding.fifaTitleOne.text = bubbleConfig.bubbleText
                    countDownTimer?.cancel()
                    showCountdown(different)
                }
                else if (bubbleConfig?.isGlobalCountDownActive == false && bubbleConfig.type == "running") {
                    binding.leftSideImage.visibility = View.VISIBLE
                    matchRunningState()
                }
                else if (bubbleConfig?.isGlobalCountDownActive == false && bubbleConfig.type == "upcomming") {
                    binding.leftSideImage.visibility = View.VISIBLE
                    matchUpcommingState()
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logException(e)
            }
        }
        mPref.bubbleVisibilityLiveData.observeForever {
            it?.let {
                binding.root.isVisible = it
            }
        }
        return BubbleDraggableItem.Builder()
            .setLayout(binding.root)
            .setGravity(DraggableWindowItemGravity.BOTTOM_RIGHT)
            .setListener(this)
            .build()
    }

    private fun showCountdown(different: Long) {
        countDownTimer = object : CountDownTimer(different, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var untilFinished = millisUntilFinished
                val day = MILLISECONDS.toDays(untilFinished)
                untilFinished -= DAYS.toMillis(day)
                val hour = MILLISECONDS.toHours(untilFinished)
                untilFinished -= HOURS.toMillis(hour)
                val minute = MILLISECONDS.toMinutes(untilFinished)
                untilFinished -= MINUTES.toMillis(minute)
                val second = MILLISECONDS.toSeconds(untilFinished)

                binding.scoreCard.text = "Starts in $second days"
            }

            override fun onFinish() {
                binding.scoreCard.text = "Starts in 0 days"
                if (bubbleConfig?.isGlobalCountDownActive == true && bubbleConfig!!.type == "running") {
                    binding.leftSideImage.visibility = View.VISIBLE
                    matchRunningState()
                }
                else if (bubbleConfig?.isGlobalCountDownActive == true && bubbleConfig!!.type == "upcomming") {
                    binding.leftSideImage.visibility = View.VISIBLE
                    matchUpcommingState()
                }
            }
        }.start()
    }

    private fun matchRunningState() {
        bubbleConfig?.match?.homeTeam?.homeCountryFlag.ifNotBlank {
            binding.homeTeamFlag.load(it)
        }
        bubbleConfig?.match?.awayTeam?.awayCountryFlag.ifNotBlank {
            binding.awayTeamFlag.load(it)
        }
        val homeTeamScore = bubbleConfig?.match?.homeTeam?.homeScore.toString()
        val awayTeamScore = bubbleConfig?.match?.awayTeam?.awayScore.toString()
        binding.scoreCard.text = "$homeTeamScore - $awayTeamScore"
        binding.fifaTitleOne.text = "LIVE"
        binding.fifaTitleOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_score_live, 0, 0, 0)
        binding.fifaTitleOne.compoundDrawablePadding = 8
    }

    private fun matchUpcommingState() {
        bubbleConfig?.match?.homeTeam?.homeCountryFlag.ifNotBlank {
            binding.homeTeamFlag.load(it)
        }
        bubbleConfig?.match?.awayTeam?.awayCountryFlag.ifNotBlank {
            binding.awayTeamFlag.load(it)
        }
        val sampleFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val startDayTime = bubbleConfig?.matchStartTime ?: "2022-11-21 16:00:00"
        val dateTime = sampleFormat.parse(startDayTime)

        val convertedDayFormat: DateFormat = SimpleDateFormat("d MMM")
        val finalDay: String? = dateTime?.let { convertedDayFormat.format(it).toString() }
        binding.fifaTitleOne.text = finalDay

        val convertedTimeFormat: DateFormat = SimpleDateFormat("h:mm a")
        val finalTime: String? = dateTime?.let { convertedTimeFormat.format(it).toString() }
        binding.scoreCard.text = finalTime
    }
    
    private fun createRemoveItem(): BubbleCloseItem {
        return BubbleCloseItem.Builder()
            .with(this)
            .setShouldFollowDrag(true)
            .setExpandable(true)
            .build()
    }
    
    override fun onTouchEventChanged(
        view: View,
        currentViewPosition: Point,
        currentTouchPoint: Point,
        velocityX: Float,
        velocityY: Float,
        draggableWindowItemTouchEvent: DraggableWindowItemTouchEvent,
    ) {
        
        when (draggableWindowItemTouchEvent) {
            DraggableWindowItemTouchEvent.CLICK_EVENT -> {
                try {
                    val bubbleIconView = (view as ConstraintLayout).getViewById(R.id.bubbleIconView)
                    val isTouched = binding.bubbleIconView.isInBounds(currentTouchPoint.x, currentTouchPoint.y)
                    if (isTouched) {
                        val uriUrl: Uri = Uri.parse(bubbleConfig?.adForwardUrl?.ifBlank { "https://toffeelive.com?routing=internal&page=home" })
                        val intent = Intent(Intent.ACTION_VIEW, uriUrl)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        intent.setPackage("com.android.chrome")
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    Log.i("bubble_", "onTouchEventChanged: ${e.message}")
                }
            }
            DraggableWindowItemTouchEvent.DRAG_EVENT -> {
//                val imageView = view.findViewById<ImageView>(R.id.draggable_view)
//                    imageView.setImageDrawable(getDrawable(R.drawable.title))
            }
            DraggableWindowItemTouchEvent.DRAG_STOP_EVENT -> {
//                view.findViewById<ImageView>(R.id.draggable_view).setImageDrawable(getDrawable(R.drawable.title))
            }
        }
    }
    
    override fun onOverlappingRemoveItemOnDrag(removeItem: BubbleCloseItem, draggableItem: BubbleDraggableItem) {
//        val imageView = draggableItem.view.findViewById<ImageView>(R.id.draggable_view)
//        imageView.setImageDrawable(getDrawable(R.drawable.title))
    }
    
    override fun onNotOverlappingRemoveItemOnDrag(removeItem: BubbleCloseItem, draggableItem: BubbleDraggableItem) {
//        val imageView = draggableItem.view.findViewById<ImageView>(R.id.draggable_view)
//        imageView.setImageDrawable(getDrawable(R.drawable.title))
    }
    
    override fun onDropInRemoveItem(removeItem: BubbleCloseItem, draggableItem: BubbleDraggableItem) {
        // Nothing to do
    }
}