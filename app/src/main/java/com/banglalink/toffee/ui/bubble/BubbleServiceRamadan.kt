package com.banglalink.toffee.ui.bubble

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import coil.load
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.BubbleViewRamadanLayoutBinding
import com.banglalink.toffee.extension.ifNotNullOrBlank
import com.banglalink.toffee.model.RamadanSchedule
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemGravity
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemTouchEvent
import com.banglalink.toffee.ui.bubble.listener.IBubbleDraggableWindowItemEventListener
import com.banglalink.toffee.ui.bubble.listener.IBubbleInteractionListener
import com.banglalink.toffee.ui.bubble.util.isInBounds
import com.banglalink.toffee.ui.bubble.view.BubbleCloseItem
import com.banglalink.toffee.ui.bubble.view.BubbleDraggableItem
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.*

class BubbleServiceRamadan : BaseBubbleService(), IBubbleDraggableWindowItemEventListener, IBubbleInteractionListener {
    
    private var timeDifference: Long? = null
    private var ramadanSchedule: RamadanSchedule? = null
    private var firstRamadanStartDate: RamadanSchedule? = null
    private lateinit var binding: BubbleViewRamadanLayoutBinding
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
        binding = BubbleViewRamadanLayoutBinding.inflate(LayoutInflater.from(this))
        
        mPref.ramadanScheduleLiveData.value?.let { ramadanSchedules ->
            ramadanSchedules.find { Utils.dateToStr(Utils.getDate(it.sehriStart)) == Utils.dateToStr(mPref.getSystemTime()) }?.let {
                this.ramadanSchedule = it
                countDownTimer?.cancel()
                when {
                    it.isRamadanStart == 0 && it.isEidStart == 0 -> {
                        binding.ramadanLeftImage.visibility = View.VISIBLE
                        it.bubbleLogoUrl.ifNotNullOrBlank {
                            binding.ramadanLeftImage.load(it)
                        }
                        firstRamadanStartDate = ramadanSchedules.find { it.isRamadanStart == 1 }
                        
                        binding.ramadanTitle.text = "রমজানুল মোবারক"
                        val dateTimeDifference = Utils.getDate(firstRamadanStartDate?.sehriStart).time.minus(mPref.getSystemTime().time)
                        showCountdownStartDays(dateTimeDifference)
                    }
                    
                    it.isRamadanStart == 1 && it.isEidStart == 0 -> {
                        ramadanStatusManipulation()
                    }
                    
                    it.isEidStart == 1 -> {
                        setEidMubarakText()
                    }
                }
            }
        }
        
        mPref.bubbleVisibilityLiveData.observeForever {
            it?.let {
                binding.root.isVisible = it
            }
        }
        mPref.startBubbleService.observeForever {
            if (!it) {
                stopSelf()
            }
        }
        return BubbleDraggableItem.Builder()
            .setLayout(binding.root)
            .setGravity(DraggableWindowItemGravity.BOTTOM_RIGHT)
            .setListener(this)
            .build()
    }
    
    private fun showCountdownStartDays(dateTimeDifference: Long) {
        countDownTimer = object : CountDownTimer(dateTimeDifference, 10_000) {
            override fun onTick(millisUntilFinished: Long) {
                setCountDownTime()
            }
            
            override fun onFinish() {
                setCountDownTime()
            }
        }.start()
    }
    @SuppressLint("SetTextI18n")
    private fun setCountDownTime() {
        runCatching {
            val countDownEndTime: String = firstRamadanStartDate?.sehriStart ?: "2023-04-01 16:00:00"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
            val currentDate = mPref.getSystemTime()
            val startDay: Date = dateFormat.parse(dateFormat.format(mPref.getSystemTime())) ?: currentDate
            val endDay: Date = dateFormat.parse(countDownEndTime) ?: currentDate
            val remainingDays = DAYS.convert(endDay.time - startDay.time, MILLISECONDS)
            if (remainingDays < 1) {
                val calendar = Calendar.getInstance()
                calendar.time = mPref.getSystemTime()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrow = calendar.time
                
                ramadanSchedule = Utils.dateToStr(tomorrow, "yyyy-MM-dd")?.let { nextDay ->
                    mPref.ramadanScheduleLiveData.value?.find { it.sehriStart?.contains(nextDay) ?: false }
                }
                
                ramadanStatusManipulation()
                return@runCatching
            }
            binding.ramadanTitleBold.text = "শুরু হচ্ছে ${remainingDays.toString().enDigitToBn()} দিনে"
        }.onFailure {
            ToffeeAnalytics.logException(it)
        }
    }
    
    private fun ramadanStatusManipulation() {
        var sehriEndTime: String = ramadanSchedule?.sehriStart ?: "2023-04-01 16:00:00"
        var endSehriDate = Utils.getDate(sehriEndTime)
        var ifterEndTime: String = ramadanSchedule?.iftarStart ?: "2023-04-01 16:00:00"
        var endIfterDate = Utils.getDate(ifterEndTime)
        
        coroutineScope.launch {
            //Get Sehri and Iftar time of Next Day
            if (mPref.getSystemTime().time > endIfterDate.time) {
                val calendar = Calendar.getInstance()
                calendar.time = mPref.getSystemTime()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrow = calendar.time
                
                ramadanSchedule = Utils.dateToStr(tomorrow, "yyyy-MM-dd")?.let { nextDay ->
                    mPref.ramadanScheduleLiveData.value?.find { it.sehriStart?.contains(nextDay) ?: false }
                }
                
                sehriEndTime = ramadanSchedule?.sehriStart ?: "2023-04-01 16:00:00"
                endSehriDate = Utils.getDate(sehriEndTime)
                
                ifterEndTime = ramadanSchedule?.iftarStart ?: "2023-04-01 16:00:00"
                endIfterDate = Utils.getDate(ifterEndTime)
            }
            
            withContext(Dispatchers.Main) {
                if (mPref.getSystemTime().time <= endSehriDate.time && ramadanSchedule?.isEidStart != 1) {
                    showCountdownSehriTime()
                } else {
                    if (mPref.getSystemTime().time <= endIfterDate.time && ramadanSchedule?.isEidStart != 1) {
                        showCountdownIfterTime()
                    }
                }
                if (ramadanSchedule?.isEidStart == 1) {
                    setEidMubarakText()
                }
            }
        }
    }
    
    private fun showCountdownIfterTime() {
        runCatching {
            val countDownEndTime: String = ramadanSchedule?.iftarStart ?: "2023-04-01 16:00:00"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
            val endDate = dateFormat.parse(countDownEndTime)
            //milliseconds
            timeDifference = endDate?.time?.minus(mPref.getSystemTime().time) ?: 0L
            countDownTimer?.cancel()
        }.onFailure {
            ToffeeAnalytics.logException(it)
        }
        
        countDownTimer = object : CountDownTimer(timeDifference!!, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                var untilFinished = millisUntilFinished
                val day = MILLISECONDS.toDays(untilFinished)
                untilFinished -= DAYS.toMillis(day)
                val hour = MILLISECONDS.toHours(untilFinished)
                untilFinished -= TimeUnit.HOURS.toMillis(hour)
                val minute = MILLISECONDS.toMinutes(untilFinished)
                untilFinished -= TimeUnit.MINUTES.toMillis(minute)
                val second = MILLISECONDS.toSeconds(untilFinished)
                val hoursInBangla = String.format("%02d", hour).enDigitToBn()
                val minutesInBangla = String.format("%02d", minute).enDigitToBn()
                val secondsInBangla = String.format("%02d", second).enDigitToBn()
                
                binding.ramadanLeftImage.visibility = View.GONE
                binding.ramadanTitle.text = "ইফতারের সময় বাকি"
                binding.ramadanTitleBold.text = "ঢাকা $hoursInBangla : $minutesInBangla : $secondsInBangla সে:"
            }
            
            override fun onFinish() {
                binding.ramadanLeftImage.visibility = View.GONE
                binding.ramadanTitle.text = "ইফতারের সময় বাকি"
                binding.ramadanTitleBold.text = "ঢাকা ০০ : ০০ : ০০ সে:"
                
                coroutineScope.launch {
                    delay(1_000 * 60 * 10) // 10 mins
                    ramadanStatusManipulation()
                }
            }
        }.start()
    }
    
    private fun showCountdownSehriTime() {
        runCatching {
            val countDownEndTime: String = ramadanSchedule?.sehriStart ?: "2023-04-01 16:00:00"
            val endDate = Utils.getDate(countDownEndTime)
            //milliseconds
            timeDifference = endDate.time.minus(mPref.getSystemTime().time) ?: 0L
            countDownTimer?.cancel()
        }.onFailure {
            ToffeeAnalytics.logException(it)
        }
        
        countDownTimer = object : CountDownTimer(timeDifference!!, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                var untilFinished = millisUntilFinished
                val day = MILLISECONDS.toDays(untilFinished)
                untilFinished -= DAYS.toMillis(day)
                val hour = MILLISECONDS.toHours(untilFinished)
                untilFinished -= TimeUnit.HOURS.toMillis(hour)
                val minute = MILLISECONDS.toMinutes(untilFinished)
                untilFinished -= TimeUnit.MINUTES.toMillis(minute)
                val second = MILLISECONDS.toSeconds(untilFinished)
                val hoursInBangla = String.format("%02d", hour).enDigitToBn()
                val minutesInBangla = String.format("%02d", minute).enDigitToBn()
                val secondsInBangla = String.format("%02d", second).enDigitToBn()
                
                binding.ramadanLeftImage.visibility = View.GONE
                binding.ramadanTitle.text = "সাহরীর সময় বাকি "
                binding.ramadanTitleBold.text = "ঢাকা $hoursInBangla : $minutesInBangla : $secondsInBangla সে:"
            }
            
            override fun onFinish() {
                binding.ramadanLeftImage.visibility = View.GONE
                binding.ramadanTitle.text = "সাহরীর সময় বাকি "
                binding.ramadanTitleBold.text = "ঢাকা ০০ : ০০ : ০০ সে:"
                
                coroutineScope.launch {
                    delay(1_000 * 60 * 10) // 10 mins
                    ramadanStatusManipulation()
                }
            }
        }.start()
    }
    
    private fun setEidMubarakText() {
        runCatching {
            binding.ramadanTitle.visibility = View.GONE
            binding.ramadanLeftImage.visibility = View.VISIBLE
            ramadanSchedule?.bubbleLogoUrl.ifNotNullOrBlank {
                binding.ramadanLeftImage.load(it)
            }
            binding.ramadanTitleBold.text = "ঈদ মোবারক"
        }.onFailure {
            ToffeeAnalytics.logException(it)
        }
    }
    
    fun String.enDigitToBn(): String {
        val bnDigits = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return this.map { bnDigits[it.toString().toInt()] }.joinToString("")
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
                        val uriUrl: Uri = Uri.parse(mPref.ramadanBubbleDeepLink)
                        val intent = Intent(Intent.ACTION_VIEW, uriUrl)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
    
    override fun onOverlappingRemoveItemOnDrag(
        removeItem: BubbleCloseItem,
        draggableItem: BubbleDraggableItem,
    ) {
//        val imageView = draggableItem.view.findViewById<ImageView>(R.id.draggable_view)
//        imageView.setImageDrawable(getDrawable(R.drawable.title))
    }
    
    override fun onNotOverlappingRemoveItemOnDrag(
        removeItem: BubbleCloseItem,
        draggableItem: BubbleDraggableItem,
    ) {
//        val imageView = draggableItem.view.findViewById<ImageView>(R.id.draggable_view)
//        imageView.setImageDrawable(getDrawable(R.drawable.title))
    }
    
    override fun onDropInRemoveItem(
        removeItem: BubbleCloseItem,
        draggableItem: BubbleDraggableItem,
    ) {
        // Nothing to do
    }
}