package com.banglalink.toffee.ui.bubble

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.R.id
import com.banglalink.toffee.analytics.ToffeeAnalytics
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

    override fun createBubble(): Bubble {
        return Bubble.Builder()
            .with(this)
            .setDraggableItem(createDraggableItem())
            .setRemoveItem(createRemoveItem())
            .setListener(this)
            .build()
    }
    
    private fun createDraggableItem(): BubbleDraggableItem {
        val draggableViewLayout = LayoutInflater.from(this).inflate(R.layout.draggable_view_toffee, null)
        
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
                if (bubbleConfig?.isGlobalCountDownActive == true) {
                    val bubbleImageView = draggableViewLayout.findViewById<ImageView>(R.id.draggable_view_image)
                    bubbleConfig.adIconUrl?.ifNotBlank {
                        bubbleImageView.load(it)
                    }
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val endDateDay = bubbleConfig.countDownEndTime ?: "2022-11-21 16:00:00"
                    val endDate = format.parse(endDateDay)
                    //milliseconds
                    val different = endDate?.time?.minus(mPref.getSystemTime().time) ?: 0L
//                withContext(Main) {
                    countDownTimer?.cancel()
                    showCountdown(different, draggableViewLayout)
//                }
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logException(e)
            }
            else if (bubbleConfig?.isGlobalCountDownActive == false) {
                val countDownBoardGroup = draggableViewLayout.findViewById<Group>(R.id.countDownBoard)
                countDownBoardGroup.visibility= View.GONE
                val scoreBoardGroup = draggableViewLayout.findViewById<Group>(R.id.scoreBoard)
                scoreBoardGroup.visibility= View.VISIBLE
                val bubbleImageView = draggableViewLayout.findViewById<ImageView>(R.id.draggable_view_image)
                bubbleConfig.adIconUrl?.ifNotBlank {
                    bubbleImageView.load(it)
                }
                leftSideBubbleWing(draggableViewLayout)
                rightSideBubbleWing(draggableViewLayout)
            }
        }
        return BubbleDraggableItem.Builder()
            .setLayout(draggableViewLayout)
            .setGravity(DraggableWindowItemGravity.BOTTOM_RIGHT)
            .setListener(this)
            .build()
    }
    
    private fun showCountdown(different: Long, draggableViewLayout: View) {
        countDownTimer = object : CountDownTimer(different, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var millisUntilFinished = millisUntilFinished
                val day = MILLISECONDS.toDays(millisUntilFinished)
                millisUntilFinished -= DAYS.toMillis(day)
                val hour = MILLISECONDS.toHours(millisUntilFinished)
                millisUntilFinished -= HOURS.toMillis(hour)
                val minute = MILLISECONDS.toMinutes(millisUntilFinished)
                millisUntilFinished -= MINUTES.toMillis(minute)
                val second = MILLISECONDS.toSeconds(millisUntilFinished)
                
                val dayCount = draggableViewLayout.findViewById<TextView>(id.countDay)
                dayCount.text = day.toString()
                val hourCount = draggableViewLayout.findViewById<TextView>(id.countHour)
                hourCount.text = hour.toString()
                val minCount = draggableViewLayout.findViewById<TextView>(id.countMin)
                minCount.text = minute.toString()
                val secCount = draggableViewLayout.findViewById<TextView>(id.countSec)
                secCount.text = second.toString()
            }
            
            override fun onFinish() {
                val countDownBoardGroup = draggableViewLayout.findViewById<Group>(R.id.countDownBoard)
                countDownBoardGroup.visibility= View.GONE
                val scoreBoardGroup = draggableViewLayout.findViewById<Group>(R.id.scoreBoard)
                scoreBoardGroup.visibility= View.VISIBLE
                val bubbleImageView = draggableViewLayout.findViewById<ImageView>(R.id.draggable_view_image)
                bubbleConfig?.adIconUrl?.ifNotBlank {
                    bubbleImageView.load(it)
                }
                leftSideBubbleWing(draggableViewLayout)
                rightSideBubbleWing(draggableViewLayout)

                val daysCounts = draggableViewLayout.findViewById<TextView>(id.countDay)
                daysCounts.text = "0"
                val hoursCounts = draggableViewLayout.findViewById<TextView>(id.countHour)
                hoursCounts.text = "0"
                val MinsCounts = draggableViewLayout.findViewById<TextView>(id.countMin)
                MinsCounts.text = "0"
                val SecCounts = draggableViewLayout.findViewById<TextView>(id.countSec)
                SecCounts.text = "0"
            }
        }.start()
    }

    private fun leftSideBubbleWing(draggableViewLayout: View){
        val leftSideTitle = draggableViewLayout.findViewById<TextView>(id.matchOne)
        leftSideTitle.text = bubbleConfig?.leftSideData?.leftTitle.toString()
        val leftSideSubTitle = draggableViewLayout.findViewById<TextView>(id.scoreOne)
        leftSideSubTitle.text = bubbleConfig?.leftSideData?.leftSubTitle.toString()

        if (bubbleConfig?.leftSideData?.leftType == "running"){
            leftSideSubTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_score_live, 0, 0, 0)
            leftSideSubTitle.compoundDrawablePadding = 10
        }
    }

    private fun rightSideBubbleWing(draggableViewLayout: View){
        val rightSideTitle = draggableViewLayout.findViewById<TextView>(id.matchTwo)
        rightSideTitle.text = bubbleConfig?.rightSideData?.rightTitle.toString()
        val rightSideSubTitle = draggableViewLayout.findViewById<TextView>(id.scoreTwo)
        rightSideSubTitle.text = bubbleConfig?.rightSideData?.rightSubTitle.toString()

        if (bubbleConfig?.rightSideData?.rightType == "running"){
            rightSideSubTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_score_live, 0, 0, 0)
            rightSideSubTitle.compoundDrawablePadding = 10
        }
    }
    
    private fun createRemoveItem(): BubbleCloseItem {
        return BubbleCloseItem.Builder().with(this).setShouldFollowDrag(true).setExpandable(true).build()
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
                    val isTouched = bubbleIconView.isInBounds(currentTouchPoint.x, currentTouchPoint.y)
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