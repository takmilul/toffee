package com.banglalink.toffee.ui.bubble

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import coil.load
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.BubbleViewRamadanLayoutBinding
import com.banglalink.toffee.extension.doIfNotNullOrBlank
import com.banglalink.toffee.model.BubbleConfig
import com.banglalink.toffee.model.RamadanScheduled
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS


class BubbleServiceRamadan : BaseBubbleService(), IBubbleDraggableWindowItemEventListener,
    IBubbleInteractionListener {

    private var timeDifference: Long? = null
    private var bubbleConfig: BubbleConfig? = null
    private var ramadanScheduled: RamadanScheduled? = null
    private val coroutineScope = CoroutineScope(Default)
    private lateinit var binding: BubbleViewRamadanLayoutBinding


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

        mPref.ramadanScheduledConfigLiveData.observeForever { RamadanScheduled ->
            runCatching {
                RamadanScheduled?.let {
                    this.ramadanScheduled = it
                    countDownTimer?.cancel()
                    if (it.isRamadanStart == 0 && it.isEidStart == 0) {
                        it.bubbleLogoUrl.doIfNotNullOrBlank {
                            binding.ramadanLeftImage.load(it)
                        }
                        binding.ramadanTitle.text = "রমজানুল মোবারক"
                        val dateTimeFormat =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val dateTimeDifference = dateTimeFormat.parse(
                            it.sehriStart ?: "2022-11-20 16:00:00"
                        )?.time?.minus(mPref.getSystemTime().time) ?: 0L
                        showCountdownStartDays(dateTimeDifference)

                    } else if (it.isRamadanStart == 1 && it.isEidStart == 0) {
                        ramadanStatusManipulation()

                    } else if (it.isEidStart == 1) {
                        setEidMubarakText()
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
        mPref.startBubbleService.observeForever {
            if (!it) {
                stopSelf()
            }
        }
        if (mPref.bubbleConfigLiveData.value == null) {
            coroutineScope.launch {
                bubbleConfig = bubbleConfigRepository.getLatestConfig()
                mPref.bubbleConfigLiveData.postValue(bubbleConfig)
            }
        }

        if (mPref.ramadanScheduledConfigLiveData.value == null) {
            coroutineScope.launch {
                ramadanScheduled = Utils.dateToStr(mPref.getSystemTime(), "yyyy-MM-dd")
                    ?.let { ramadanBubbleRepository.getAllRamadanItems(it) }
                mPref.ramadanScheduledConfigLiveData.postValue(ramadanScheduled)
            }
        }
        return BubbleDraggableItem.Builder()
            .setLayout(binding.root)
            .setGravity(DraggableWindowItemGravity.BOTTOM_RIGHT)
            .setListener(this)
            .build()
    }

    private fun ramadanStatusManipulation(){
        val dateFormat =
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        var sehriEndTime: String =
            ramadanScheduled?.sehriStart ?: "2023-04-01 16:00:00"
        var endSehriDate = dateFormat.parse(sehriEndTime)

        var ifterEndTime: String =
            ramadanScheduled?.iftarStart ?: "2023-04-01 16:00:00"
        var endIfterDate = dateFormat.parse(ifterEndTime)

        coroutineScope.launch {

            //Get Sehri and Iftar time of Next Day
            if (endIfterDate != null && mPref.getSystemTime().time > endIfterDate.time) {
                Log.i("__ramadan nextDayIfter", "")

                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrow = calendar.time

                ramadanScheduled = Utils.dateToStr(tomorrow, "yyyy-MM-dd")
                    ?.let { ramadanBubbleRepository.getAllRamadanItems(it) }

                sehriEndTime = ramadanScheduled?.sehriStart ?: "2023-04-01 16:00:00"
                endSehriDate = dateFormat.parse(sehriEndTime)

                ifterEndTime = ramadanScheduled?.iftarStart ?: "2023-04-01 16:00:00"
                endIfterDate = dateFormat.parse(ifterEndTime)

                Log.i("__ramadan nextDay", ramadanScheduled.toString())
            }

            withContext(Dispatchers.Main) {
                Log.i("__ramadan system", mPref.getSystemTime().toString())
                Log.i("__ramadan Ifter", endIfterDate.toString())
                Log.i("__ramadan Sehri", endSehriDate.toString())

                if (endSehriDate != null && mPref.getSystemTime().time <= endSehriDate.time) {
                    Log.i("__ramadan SehriDate", "")
                    showCountdownSehriTime()
                } else {
                    if (endIfterDate != null && mPref.getSystemTime().time <= endIfterDate.time) {
                        Log.i("__ramadan ifterDate", "")
                        showCountdownIfterTime()
                    }
                }
            }

        }
    }

    private fun showCountdownIfterTime() {
        runCatching {
            val countDownEndTime: String = ramadanScheduled?.iftarStart ?: "2023-04-01 16:00:00"
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
                binding.ramadanTitleBold.text =
                    "ঢাকা $hoursInBangla : $minutesInBangla : $secondsInBangla সে:"
            }

            override fun onFinish() {
                binding.ramadanLeftImage.visibility = View.GONE
                binding.ramadanTitle.text = "ইফতারের সময় বাকি"
                binding.ramadanTitleBold.text = "ঢাকা ০০ : ০০ : ০০ সে:"

                Handler(Looper.getMainLooper()).postDelayed({
                    ramadanStatusManipulation()
                }, 6000)
            }
        }.start()
    }

    private fun showCountdownSehriTime() {
        runCatching {
            val countDownEndTime: String = ramadanScheduled?.sehriStart ?: "2023-04-01 16:00:00"
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
                binding.ramadanTitle.text = "সাহরীর সময় বাকি "
                binding.ramadanTitleBold.text =
                    "ঢাকা $hoursInBangla : $minutesInBangla : $secondsInBangla সে:"
            }

            override fun onFinish() {
                binding.ramadanLeftImage.visibility = View.GONE
                binding.ramadanTitle.text = "সাহরীর সময় বাকি "
                binding.ramadanTitleBold.text = "ঢাকা ০০ : ০০ : ০০ সে:"

                Handler(Looper.getMainLooper()).postDelayed({
                    ramadanStatusManipulation()
                }, 6000)
            }
        }.start()
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
            val countDownEndTime: String = ramadanScheduled?.sehriStart ?: "2023-04-01 16:00:00"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val startDay: Date =
                dateFormat.parse(dateFormat.format(mPref.getSystemTime())) ?: currentDate
            val endDay: Date = dateFormat.parse(countDownEndTime) ?: currentDate
            val remainingDays = DAYS.convert(endDay.time - startDay.time, MILLISECONDS)
            val remainingDaysText = if (remainingDays < 1L) {
                "০ দিনে"
            } else if (remainingDays == 1L) {
                "১ দিনে"
            } else {
                "${remainingDays.toString().enDigitToBn()} দিনে"
            }
            binding.ramadanTitleBold.text = "শুরু হচ্ছে $remainingDaysText"
        }.onFailure {
            ToffeeAnalytics.logException(it)
        }
    }

    private fun setEidMubarakText() {
        runCatching {
            binding.ramadanTitle.visibility = View.GONE
            ramadanScheduled?.bubbleLogoUrl.doIfNotNullOrBlank {
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
                    val isTouched =
                        binding.bubbleIconView.isInBounds(currentTouchPoint.x, currentTouchPoint.y)
                    if (isTouched) {
                        val uriUrl: Uri =
                            Uri.parse(bubbleConfig?.adForwardUrl?.ifBlank { "https://toffeelive.com?routing=internal&page=home" })
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