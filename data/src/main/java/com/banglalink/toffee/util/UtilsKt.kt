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
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.Display
import android.view.inputmethod.InputMethodManager
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.*
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt


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

    fun strToDate(dateTime: String?, dateFormate: String = "yyyy-MM-d HH:mm:ss"):Date? {

        val df: DateFormat = SimpleDateFormat(dateFormate)
        try {
            if (dateTime != null) {
                return df.parse(dateTime)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }


    fun dateToStr(dateTime: Date?, dateFormate: String = "dd/MM/yyyy"):String? {

        val formater: DateFormat = SimpleDateFormat(dateFormate)

        try {
            if (dateTime != null) {
                return formater.format(dateTime)
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