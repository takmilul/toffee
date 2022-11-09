package com.banglalink.toffee.ui.bubble

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.BubbleViewV2LayoutBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotBlank
import com.banglalink.toffee.extension.isNotBlank
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.BubbleConfig
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemGravity
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemTouchEvent
import com.banglalink.toffee.ui.bubble.listener.IBubbleDraggableWindowItemEventListener
import com.banglalink.toffee.ui.bubble.listener.IBubbleInteractionListener
import com.banglalink.toffee.ui.bubble.util.isInBounds
import com.banglalink.toffee.ui.bubble.view.BubbleCloseItem
import com.banglalink.toffee.ui.bubble.view.BubbleDraggableItem
import com.banglalink.toffee.util.Log
import com.google.ads.interactivemedia.v3.internal.it
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
        
        mPref.bubbleConfigLiveData.observeForever { bubbleConfig ->
            runCatching {
                bubbleConfig?.let {
                    this.bubbleConfig = it
                    it.poweredBy?.ifNotBlank { binding.poweredByText.text = it }
                    it.poweredByIconUrl.ifNotBlank {
                        binding.poweredByImage.load(it)
                    }
                    countDownTimer?.cancel()
                    if (it.isGlobalCountDownActive) {
                        binding.awayTeamFlag.hide()
                        binding.liveGif.hide()
                        it.adIconUrl.ifNotBlank {
                            binding.homeTeamFlag.load(it)
                        }
                        binding.fifaTitleOne.text = it.bubbleText
                        binding.fifaTitleOne.text = it.bubbleText?.trim()?.replace("\n", "<br/>")?.let {
                            HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        }
                        
                        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val dateTimeDifference = dateTimeFormat.parse(
                            it.countDownEndTime ?: "2022-11-20 16:00:00"
                        )?.time?.minus(mPref.getSystemTime().time) ?: 0L
                        showCountdown(dateTimeDifference)
                    } else if (it.type == "running") {
                        matchRunningState()
                    } else if (it.type == "upcomming") {
                        matchUpcomingState()
                    }
                }
            }.onFailure {
                ToffeeAnalytics.logException(it)
            }
        }
        mPref.bubbleVisibilityLiveData.observeForever {
            it?.let {
                binding.root.isVisible = it
            }
        }
        if (mPref.bubbleConfigLiveData.value == null) {
            coroutineScope.launch {
                bubbleConfig = bubbleConfigRepository.getLatestConfig()
                mPref.bubbleConfigLiveData.postValue(bubbleConfig)
            }
        }
        return BubbleDraggableItem.Builder()
            .setLayout(binding.root)
            .setGravity(DraggableWindowItemGravity.BOTTOM_RIGHT)
            .setListener(this)
            .build()
    }
    
    private fun showCountdown(dateTimeDifference: Long) {
        countDownTimer = object : CountDownTimer(dateTimeDifference, 10000) {
            override fun onTick(millisUntilFinished: Long) {
                setCountDownTime()
            }
            
            override fun onFinish() {
                setCountDownTime()
            }
        }.start()
    }
    
    private fun setCountDownTime() {
        runCatching {
            val countDownEndTime: String = bubbleConfig?.countDownEndTime ?: "2022-11-20 16:00:00"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val startDay: Date = dateFormat.parse(dateFormat.format(mPref.getSystemTime())) ?: currentDate
            val endDay: Date = dateFormat.parse(countDownEndTime) ?: currentDate
            val remainingDays = DAYS.convert(endDay.time - startDay.time, MILLISECONDS)
            if (remainingDays <= 1L) {
                binding.scoreCard.text = "Starts in 1 day"
            } else {
                binding.scoreCard.text = "Starts in $remainingDays days"
            }
        }.onFailure {
            ToffeeAnalytics.logException(it)
        }
    }
    
    private fun matchRunningState() {
        binding.awayTeamFlag.show()
        binding.liveGif.show()
        binding.liveGif.load(getDrawable(R.drawable.bubble_live_gif))
        bubbleConfig?.match?.homeTeam?.homeCountryFlag.ifNotBlank {
            bindingUtil.bindRoundImage(binding.homeTeamFlag, it)
        }
        bubbleConfig?.match?.awayTeam?.awayCountryFlag.ifNotBlank {
            bindingUtil.bindRoundImage(binding.awayTeamFlag, it)
        }
        val homeTeamScore = bubbleConfig?.match?.homeTeam?.homeScore?.isNotBlank { it } ?: "0"
        val awayTeamScore = bubbleConfig?.match?.awayTeam?.awayScore?.isNotBlank { it } ?: "0"
        binding.scoreCard.text = "$homeTeamScore - $awayTeamScore"
        binding.fifaTitleOne.text = "LIVE"
    }
    
    private fun matchUpcomingState() {
        runCatching {
            binding.awayTeamFlag.show()
            binding.liveGif.hide()
            bubbleConfig?.match?.homeTeam?.homeCountryFlag.ifNotBlank {
                bindingUtil.bindRoundImage(binding.homeTeamFlag, it)
            }
            bubbleConfig?.match?.awayTeam?.awayCountryFlag.ifNotBlank {
                bindingUtil.bindRoundImage(binding.awayTeamFlag, it)
            }
            val sampleFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val startDayTime = bubbleConfig?.matchStartTime ?: "2022-11-21 16:00:00"
            val dateTime = sampleFormat.parse(startDayTime)
            
            val convertedDayFormat: DateFormat = SimpleDateFormat("d MMM")
            val finalDay: String? = dateTime?.let { convertedDayFormat.format(it).toString() }
            binding.fifaTitleOne.text = finalDay?.uppercase(Locale.getDefault())
            
            val convertedTimeFormat: DateFormat = SimpleDateFormat("h:mm a")
            val finalTime: String? = dateTime?.let { convertedTimeFormat.format(it).toString() }
            binding.scoreCard.text = finalTime
        }.onFailure {
            ToffeeAnalytics.logException(it)
        }
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