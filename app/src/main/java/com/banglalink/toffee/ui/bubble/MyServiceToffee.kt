package com.banglalink.toffee.ui.bubble

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.bubble.floatie.*
import com.banglalink.toffee.ui.bubble.floatie.view.FloatieDraggableItem
import com.banglalink.toffee.ui.bubble.floatie.view.FloatieRemoveItem
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MyServiceToffee: FloatieService(), IFloatieDraggableWindowItemEventListener,
    IFloatieInteractionListener {

    override fun createFloatie(): Floatie {
        return Floatie.Builder()
            .with(this)
            .setDraggableItem(createDraggableItem())
            .setRemoveItem(createRemoveItem())
            .setListener(this)
            .build()
    }

    private fun createDraggableItem(): FloatieDraggableItem {

        val draggableViewLayout = LayoutInflater.from(this).inflate(R.layout.draggable_view_toffee, null)


//        val textView = draggableViewLayout.findViewById(R.id.textView11) as TextView
//        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_rectangle_39, 0, 0, 0)
//        textView.compoundDrawablePadding = 10

        val currentTime = Calendar.getInstance().time
        val endDateDay = "21/11/2022 16:00:00"
        val format1 = SimpleDateFormat("dd/MM/yyyy hh:mm:ss",Locale.getDefault())
        val endDate = format1.parse(endDateDay)
        //milliseconds
        val different = endDate?.time?.minus(currentTime.time)

        object : CountDownTimer(different!!, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var millisUntilFinished = millisUntilFinished
                val day = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                millisUntilFinished -= TimeUnit.DAYS.toMillis(day)
                val hour = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                millisUntilFinished -= TimeUnit.HOURS.toMillis(hour)
                val minute = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minute)
                val second = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

                val textView3 = draggableViewLayout.findViewById<TextView>(R.id.textView3)
                textView3.text = day.toString()

                val textView2 = draggableViewLayout.findViewById<TextView>(R.id.textView2)
                textView2.text = hour.toString()

                val textView6 = draggableViewLayout.findViewById<TextView>(R.id.textView6)
                textView6.text = minute.toString()

                val textView = draggableViewLayout.findViewById<TextView>(R.id.textView)
                textView.text = second.toString()

            }

            override fun onFinish() {
                val textView3 = draggableViewLayout.findViewById<TextView>(R.id.textView3)
                textView3.text = "0"

                val textView2 = draggableViewLayout.findViewById<TextView>(R.id.textView2)
                textView2.text = "0"

                val textView6 = draggableViewLayout.findViewById<TextView>(R.id.textView6)
                textView6.text = "0"

                val textView = draggableViewLayout.findViewById<TextView>(R.id.textView)
                textView.text = "0"
            }
        }.start()

//        val cardView2 = draggableViewLayout.findViewById<CardView>(R.id.cardView2)
//        cardView2.setOnClickListener {
//            val uriUrl: Uri = Uri.parse("https://toffeelive.com/")
//            val intent = Intent(Intent.ACTION_VIEW, uriUrl)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.setPackage("com.android.chrome")
//            startActivity(intent)
//        }

        return FloatieDraggableItem.Builder()
            .setLayout(draggableViewLayout)
            .setGravity(DraggableWindowItemGravity.TOP_LEFT)
            .setListener(this)
            .build()
    }

    private fun createRemoveItem(): FloatieRemoveItem {
        return FloatieRemoveItem.Builder()
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
                val uriUrl: Uri = Uri.parse("https://toffeelive.com/")
                val intent = Intent(Intent.ACTION_VIEW, uriUrl)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setPackage("com.android.chrome")
                startActivity(intent)
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
        removeItem: FloatieRemoveItem,
        draggableItem: FloatieDraggableItem,
    ) {
//        val imageView = draggableItem.view.findViewById<ImageView>(R.id.draggable_view)
//        imageView.setImageDrawable(getDrawable(R.drawable.title))
    }

    override fun onNotOverlappingRemoveItemOnDrag(
        removeItem: FloatieRemoveItem,
        draggableItem: FloatieDraggableItem,
    ) {
//        val imageView = draggableItem.view.findViewById<ImageView>(R.id.draggable_view)
//        imageView.setImageDrawable(getDrawable(R.drawable.title))
    }

    override fun onDropInRemoveItem(
        removeItem: FloatieRemoveItem,
        draggableItem: FloatieDraggableItem,
    ) {
        // Nothing to do
    }
}