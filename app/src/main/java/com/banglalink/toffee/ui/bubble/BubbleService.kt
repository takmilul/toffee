package com.banglalink.toffee.ui.bubble

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.DraggableViewToffeeBinding
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit.*

class BubbleService : BaseBubbleService(), IBubbleDraggableWindowItemEventListener, IBubbleInteractionListener {
    
    private var bubbleConfig: BubbleConfig? = null
    private val coroutineScope = CoroutineScope(Default)
    private lateinit var binding: DraggableViewToffeeBinding
    
    override fun createBubble(): Bubble {
        return Bubble.Builder()
            .with(this)
            .setDraggableItem(createDraggableItem())
            .setRemoveItem(createRemoveItem())
            .setListener(this)
            .build()
    }
    
    private fun createDraggableItem(): BubbleDraggableItem {
        binding = DraggableViewToffeeBinding.inflate(LayoutInflater.from(this))

//        coroutineScope.launch {
//            bubbleConfig = bubbleConfigRepository.getLatestConfig()
//        }
        if (mPref.bubbleConfigLiveData.value == null) {
            coroutineScope.launch {
                bubbleConfig = bubbleConfigRepository.getLatestConfig()
                mPref.bubbleConfigLiveData.postValue(bubbleConfig)
            }
        }
        mPref.bubbleConfigLiveData.observeForever { bubbleConfig ->
            try {
                this.bubbleConfig = bubbleConfig
                bubbleConfig?.adIconUrl?.ifNotBlank {
                    binding.draggableViewImage.load(it)
                }
                if (bubbleConfig?.isGlobalCountDownActive == true) {
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val endDateDay = bubbleConfig.countDownEndTime ?: "2022-11-21 16:00:00"
                    val endDate = format.parse(endDateDay)
                    //milliseconds
                    val different = endDate?.time?.minus(mPref.getSystemTime().time) ?: 0L
//                withContext(Main) {
                    countDownTimer?.cancel()
                    showCountdown(different)
//                }
                } else if (bubbleConfig?.isGlobalCountDownActive == false) {
                    binding.countDownBoard.visibility = View.GONE
                    binding.scoreBoard.visibility = View.VISIBLE
                    leftSideBubbleWing()
                    rightSideBubbleWing()
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logException(e)
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
                
                binding.countDay.text = day.toString()
                binding.countHour.text = hour.toString()
                binding.countMin.text = minute.toString()
                binding.countSec.text = second.toString()
            }
            
            override fun onFinish() {
                binding.countDay.text = "0"
                binding.countHour.text = "0"
                binding.countMin.text = "0"
                binding.countSec.text = "0"
                
                leftSideBubbleWing()
                rightSideBubbleWing()
                binding.countDownBoard.visibility = View.GONE
                binding.scoreBoard.visibility = View.VISIBLE
            }
        }.start()
    }
    
    private fun leftSideBubbleWing() {
        binding.matchOne.text = bubbleConfig?.leftSideData?.leftTitle.toString()
        binding.scoreOne.text = bubbleConfig?.leftSideData?.leftSubTitle.toString()
        
        if (bubbleConfig?.leftSideData?.leftType == "running") {
            binding.scoreOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_score_live, 0, 0, 0)
            binding.scoreOne.compoundDrawablePadding = 10
        }
    }
    
    private fun rightSideBubbleWing() {
        binding.matchTwo.text = bubbleConfig?.rightSideData?.rightTitle.toString()
        binding.scoreTwo.text = bubbleConfig?.rightSideData?.rightSubTitle.toString()
        
        if (bubbleConfig?.rightSideData?.rightType == "running") {
            binding.scoreTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_score_live, 0, 0, 0)
            binding.scoreTwo.compoundDrawablePadding = 10
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
//                    val bubbleIconView = (view as ConstraintLayout).getViewById(R.id.bubbleIconView)
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