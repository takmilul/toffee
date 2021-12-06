package com.banglalink.toffee.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.*
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


object UtilsKt {
    fun uploadIdToString(id: Long) = "Toffee_Upload_$id"
    fun isCopyrightUploadId(id: String) = id.contains("_copyright")
    fun stringToUploadId(uploadId: String) = uploadId.filter { it.isDigit() }.toLong()

    fun resizeBitmap(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > 1) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        } else {
            image
        }
    }

    suspend fun contentTypeFromContentUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO + Job()) {
        val type = context.contentResolver.getType(uri)

        if (type.isNullOrBlank()) {
            "application/octet-stream"
        }
        else {
            type
        }
    }

    suspend fun fileNameFromContentUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO + Job()) {
        context.contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
            else {
                null
            }
        } ?: uri.toString().split(File.separator).last()
    }

    suspend fun fileSizeFromContentUri(context: Context, uri: Uri): Long = withContext(Dispatchers.IO + Job()) {
        if(uri.scheme != "content") {
            File(uri.toString()).length()
        }
        else {
            context.contentResolver.query(uri, null, null, null, null)?.use {
                if(it.moveToFirst()) {
                    it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
                }
                else {
                    0L
                }
            } ?: 0L
        }
    }

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            activity.currentFocus?.windowToken,
            0
        )
    }


    fun generateThumbnail(context: Context, filePath: String): Pair<String?, Int>? {
        return try {
            val mmr = MediaMetadataRetriever()
            if(filePath.startsWith("content://")) {
                mmr.setDataSource(context, Uri.parse(filePath))
            } else {
                mmr.setDataSource(filePath)
            }
            val bmp = mmr.frameAtTime
            if(bmp != null) {
                val isHorizontal = if (bmp.width > bmp.height) 1 else 0

                val scaledBmp = resizeBitmap(bmp, 1280, 720)
                val byteArrayOutputStream = ByteArrayOutputStream()
                scaledBmp?.compress(JPEG, 70, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                Pair(Base64.encodeToString(byteArray, Base64.NO_WRAP), isHorizontal)
            } else {
                null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    suspend fun getVideoDuration(context: Context, filePath: String): Long = withContext(Dispatchers.IO + Job()) {
        try{
            val mmr = MediaMetadataRetriever()
            if(filePath.startsWith("content://")) {
                mmr.setDataSource(context, Uri.parse(filePath))
            } else {
                mmr.setDataSource(filePath)
            }
            val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//            TimeUnit.MILLISECONDS
//                .toSeconds(
            duration?.toLong() ?: 0L
//                )
        } catch (ex: Exception) {
            ex.printStackTrace()
            0L
        }
    }

    fun getDurationLongToString(timeMs: Long): String {
        val totalSeconds = round(timeMs / 1000F)
        val seconds = (totalSeconds % 60).toInt()
        val minutes = (totalSeconds / 60 % 60).toInt()
        val hours = (totalSeconds / 3600).toInt()
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        }
        else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    fun getVideoUploadLimit(timeMs: Long): Boolean {
        return (10 > round(timeMs / 1000F) || round(timeMs / 1000F) > 7200)
    }

    fun getLongDuration(str: String?): Long {
        if(str.isNullOrBlank()) return 0L
        val splist = str.split(":").reversed()
        var ret = 0L
        if(splist.isNotEmpty()) {
            ret += splist[0].toLong()
        }
        if(splist.size > 1) {
            ret += splist[1].toLong() * 60L
        }
        if(splist.size > 2) {
            ret += splist[2].toLong() * 3600L
        }
        return ret * 1000L
    }

    fun getScreenWidth(): Int = Resources.getSystem().displayMetrics.widthPixels
    fun getScreenHeight(): Int = Resources.getSystem().displayMetrics.heightPixels
    fun getRealScreenSize(ctx: Context): Point {
        return if (ctx is Activity) {
            val display = getDisplay(ctx)
            val pt = Point()
            display?.getRealSize(pt)
            pt
        } else {
            Point(getScreenWidth(), getScreenHeight())
        }
    }

    private fun getDisplay(context: Activity): Display? {
        return if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.display
        } else {
            @Suppress("DEPRECATION")
            context.windowManager.defaultDisplay
        }
    }

    private var ramSize: Long = -1
    fun getRamSize(ctx: Context): Long {
        if(ramSize > 0) return ramSize
        val memInfo = ActivityManager.MemoryInfo()
        (ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memInfo)
        ramSize = memInfo.totalMem
        return ramSize
    }

    fun getImageSize(ctx: Context, expectedWidth: Int): Point {
        var maxWidth = expectedWidth//(expectedWidth * Resources.getSystem().displayMetrics.density).toInt()//getScreenWidth()
//        if(expectedWidth > getScreenWidth()) maxWidth = getScreenWidth()
        val ram = getRamSize(ctx)
        if(ram <= 1_500_000_000) maxWidth = maxWidth * 2 / 3
        val maxHeight = maxWidth * 9 / 16
        return Point(maxWidth, maxHeight)
    }

    fun isSystemRotationOn(ctx: Context) = Settings.System.getInt(ctx.contentResolver,
        Settings.System.ACCELEROMETER_ROTATION, 0) == 1

    fun strToDate(dateTime: String?, dateFormat: String = "yyyy-MM-d HH:mm:ss"):Date? {
        val df: DateFormat = SimpleDateFormat(dateFormat, Locale.US)
        try {
            if (dateTime != null) {
                return df.parse(dateTime)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }


    fun dateToStr(dateTime: Date?, dateFormat: String = "dd/MM/yyyy"):String? {
        val formatter: DateFormat = SimpleDateFormat(dateFormat, Locale.US)
        try {
            if (dateTime != null) {
                return formatter.format(dateTime)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    fun readFileToBytes(file: File): ByteArray {
        val bytes = ByteArray(file.length().toInt())
        FileInputStream(file).use { fis -> fis.read(bytes) }
        return bytes
    }

    fun getVersionInfo(context: Context): Pair<String, Long>? {
        try {
            val pInfo: PackageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            val version: String = pInfo.versionName
            return Pair(version, PackageInfoCompat.getLongVersionCode(pInfo))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }
}

const val IMAGE_MAX_SIZE = 500000 // 500KB
const val TAG = "SCALE_IMAGE"

fun getInputStream(ctx: Context, uri: String): InputStream? {
    if(uri.startsWith("content://")) {
        return ctx.contentResolver.openInputStream(Uri.parse(uri))
    }
    return FileInputStream(File(uri.substringAfter("file:")))
}

fun getBitmap(ctx: Context, path: String, requiredImageSize: Int): Bitmap? {

//    val uri: Uri = Uri.parse(path.substringAfter("file:/"))
    var inputStream: InputStream?
    return try {
        inputStream = getInputStream(ctx, path)

        // Decode image size
        var options = Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()
        var scale = 1
        while (options.outWidth * options.outHeight * (1 / scale.toDouble().pow(2.0)) > requiredImageSize) {
            scale++
        }
        Log.d(
            TAG,
            "scale = " + scale + ", orig-width: " + options.outWidth + ", orig-height: " + options.outHeight
        )
        var resultBitmap: Bitmap? = null
        inputStream = getInputStream(ctx, path)
        if (scale > 1) {
            scale--
            // scale to max possible inSampleSize that still yields an image
            // larger than target
            options = Options()
            options.inSampleSize = scale
            resultBitmap = BitmapFactory.decodeStream(inputStream, null, options)

            // resize to desired dimensions
            val height = resultBitmap!!.height
            val width = resultBitmap.width
            Log.d(TAG, "1th scale operation dimenions - width: $width, height: $height")
            val y = sqrt(requiredImageSize / (width.toDouble() / height))
            val x = y / height * width
            val scaledBitmap = Bitmap.createScaledBitmap(
                resultBitmap, x.toInt(),
                y.toInt(), true
            )
            resultBitmap.recycle()
            resultBitmap = scaledBitmap
        }
        else {
            resultBitmap = BitmapFactory.decodeStream(inputStream)
        }
        inputStream?.close()
        Log.d(
            TAG, "bitmap size - width: " + resultBitmap!!.width + ", height: " +
                    resultBitmap.height
        )
        resultBitmap
    }
    catch (e: IOException) {
        Log.e(TAG, e.message, e)
        null
    }
}

fun imagePathToBase64(ctx: Context, imagePath: String, requiredImageByteSize: Int = IMAGE_MAX_SIZE): String{
    val bitmap = getBitmap(ctx, imagePath, requiredImageByteSize)
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap?.compress(JPEG, 70, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

val today: String
    get() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"))
        val cal = Calendar.getInstance(TimeZone.getDefault())
        val dateGMT = cal.time
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(dateGMT)
    }


fun getDateTimeText(dateTime: String): String? {
    if (TextUtils.isEmpty(dateTime)) {
        return ""
    }
    var dateTimeNew: String? = ""
    val currentFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) //2016-10-20 06:45:29
    dateTimeNew = try {
        val dateObj = currentFormatter.parse(dateTime)
        val postFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm aaa", Locale.US)
        val newDateStr = postFormatter.format(dateObj!!)
        newDateStr
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        dateTime
    }
    return dateTimeNew
}

fun formatValidityText(date: Date): String? {
    val currentFormatter = SimpleDateFormat("dd MMMM hh:mm aa", Locale.US) //2020-04-25 23:59:59
    return currentFormatter.format(date)
}

fun getDate(dateTime: String?): Date {
    val currentFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) //2020-04-25 23:59:59
    return try {
        currentFormatter.parse(dateTime) ?: Date()
    } catch (e: ParseException) {
        e.printStackTrace()
        Date()
    }
}

fun getDateDiffInDayOrHourOrMinute(stopDate: Long): String {
    val date = Date(stopDate)
    val diff = Date().time - date.time
    var diffMinutes = diff / 60000
    diffMinutes = max(0, diffMinutes)
    if (diffMinutes >= 365 * 24 * 60) {
        return (diffMinutes / (365 * 24 * 60)).toString() + "y"
    }
    if (diffMinutes >= 30 * 24 * 60) {
        return (diffMinutes / (30 * 24 * 60)).toString() + "m"
    }
    if (diffMinutes >= 24 * 60) {
        return (diffMinutes / (24 * 60)).toString() + "d"
    }
    return if (diffMinutes >= 60) {
        (diffMinutes / 60).toString() + "h"
    } else "Just Now!"
    /*if (diffMinutes > 0) {
            return diffMinutes + " minutes";
        }*/
}

fun getDateDiffInDayOrHour(stopDate: Date): String {
    val diff = stopDate.time - Date().time
    var diffHours = diff / 3600000
    diffHours = max(0, diffHours)
    return if (diffHours >= 24) {
        (diffHours / 24).toString() + " days"
    } else "$diffHours hours"
}

fun getFormattedViewsText(viewCount: String): String? {
    if (TextUtils.isEmpty(viewCount) || !TextUtils.isDigitsOnly(viewCount)) return viewCount
    val count = viewCount.toLong()
    return if (count < 1000) viewCount else viewCountFormat(count.toDouble(), 0)
}

fun checkWifiOnAndConnected(context: Context): Boolean {
    val wifiMgr: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON
        val wifiInfo = wifiMgr.connectionInfo
        wifiInfo.networkId != -1
        // Connected to an access point
    } else {
        false // Wi-Fi adapter is OFF
    }
}

fun getTime(dateTime: String): String? {
    val df: DateFormat = SimpleDateFormat("yyyy-MM-d HH:mm:ss", Locale.US)
    val formatter: DateFormat = SimpleDateFormat("hh:mm aa", Locale.US)
    try {
        val date = df.parse(dateTime)
        return formatter.format(date!!)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return ""
}

fun getDay(dateTime: String): String? {
    val df: DateFormat = SimpleDateFormat("yyyy-MM-d HH:mm:ss", Locale.US)
    val formatter: DateFormat = SimpleDateFormat("EEEE", Locale.US)
    try {
        val date = df.parse(dateTime)
        return formatter.format(date!!)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return ""
}

fun formatDate(dateTime: String): String? {
    val df: DateFormat = SimpleDateFormat("yyyy-MM-d HH:mm:ss", Locale.US)
    val formatter: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    try {
        val date = df.parse(dateTime)
        return formatter.format(date!!)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return ""
}

fun setFullScreen(activity: AppCompatActivity, visible: Boolean) {
    val uiOptions = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        or View.SYSTEM_UI_FLAG_IMMERSIVE // Set the content to appear under the system bars so that the
        // content doesn't resize when the system bars hide and show.
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Hide the nav bar and status bar
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    if (!visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val windowLayoutParams = activity.window.attributes
            windowLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            activity.window.attributes = windowLayoutParams
        }
        activity.window.decorView.systemUiVisibility = 0
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val windowLayoutParams = activity.window.attributes
            windowLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            activity.window.attributes = windowLayoutParams
        }
        activity.window.decorView.systemUiVisibility = uiOptions
    }
}

fun isFullScreen(activity: Activity): Boolean {
    return activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != 0
}


fun discardZeroFromDuration(duration: String): String {
    if (TextUtils.isEmpty(duration)) {
        return "00:00"
    }
    return if (duration.length > 5 && duration.startsWith("00:")) {
        duration.substring(3)
    } else {
        duration
    }
}

private val c = charArrayOf('K', 'M', 'B', 'T')

/**
 * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
 *
 * @param n         the number to format
 * @param iteration in fact this is the class from the array c
 * @return a String representing the number n formatted in a cool looking way.
 */
private fun viewCountFormat(n: Double, iteration: Int): String? {
    val d = n.toLong() / 100 / 10.0
    val isRound = d * 10 % 10 == 0.0 //true if the decimal part is equal to 0 (then it's trimmed anyway)
    return (if (d < 1000) //this determines the class, i.e. 'k', 'm' etc
        (if (d > 99.9 || isRound || !isRound && d > 9.99) //this decides whether to trim the decimals
            d.toInt() * 10 / 10 else d.toString() + "" // (int) d * 10 / 10 drops the decimal
            ).toString() + "" + c[iteration] else viewCountFormat(d, iteration + 1))
}

fun readableFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.##").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

fun getDateTime(): String? {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"))
    val cal = Calendar.getInstance(TimeZone.getDefault())
    val dateGMT = cal.time
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return sdf.format(dateGMT)
}