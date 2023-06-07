package com.banglalink.toffee.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

/**
 * Rotate an image if required.
 * @param img
 * @param selectedImage
 * @return
 */
private fun rotateImageIfRequired(context: Context, img: Bitmap?, selectedImage: Uri): Bitmap? {

    // Detect rotation
    var rotation = 0
    try {
        rotation = getRotation(context, selectedImage)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    
    return if (rotation != 0) {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        val rotatedImg = Bitmap.createBitmap(img!!, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        rotatedImg
    } else {
        img
    }
}

/**
 * Get the rotation of the last image added.
 * @param context
 * @param selectedImage
 * @return
 */
@Throws(IOException::class)
fun getRotation(context: Context, selectedImage: Uri): Int {

    var rotation = 0

    val ei = ExifInterface(selectedImage.path!!)
    val orientation = ei.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )

    when (orientation) {

        ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90

        ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180

        ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
    }
    return rotation
}

private const val MAX_HEIGHT = 1024
private const val MAX_WIDTH = 1024
@Throws(IOException::class)
fun decodeSampledBitmap(context: Context, selectedImage: Uri): Bitmap? {

    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    var imageStream = context.contentResolver.openInputStream(selectedImage)
    BitmapFactory.decodeStream(imageStream, null, options)
    imageStream!!.close()

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    imageStream = context.contentResolver.openInputStream(selectedImage)
    var img = BitmapFactory.decodeStream(imageStream, null, options)

    img = rotateImageIfRequired(context, img, selectedImage)
    return img
}


/**
 * Calculate an inSampleSize for use in a [BitmapFactory.Options] object when decoding
 * bitmaps using the decode* methods from [BitmapFactory]. This implementation calculates
 * the closest inSampleSize that will result in the final decoded bitmap having a width and
 * height equal to or larger than the requested width and height. This implementation does not
 * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
 * results in a larger bitmap which isn't as useful for caching purposes.
 *
 * @param options   An options object with out* params already populated (run through a decode*
 * method with inJustDecodeBounds==true
 * @param reqWidth  The requested width of the resulting bitmap
 * @param reqHeight The requested height of the resulting bitmap
 * @return The value to be used for inSampleSize
 */
private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int, reqHeight: Int
): Int {

    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        // Calculate ratios of height and width to requested height and width
        val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
        val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

        // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
        // with both dimensions larger than or equal to the requested height and width.
        inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

        // This offers some additional logic in case the image has a strange
        // aspect ratio. For example, a panorama may have a much larger
        // width than height. In these cases the total pixels might still
        // end up being too large to fit comfortably in memory, so we should
        // be more aggressive with sample down the image (=larger inSampleSize).

        val totalPixels = (width * height).toFloat()

        // Anything more than 2x the requested pixels we'll sample down further
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
    }
    return inSampleSize
}
fun <T : Drawable> T.bytesEqualTo(t: T?) = toBitmap().bytesEqualTo(t?.toBitmap(), true)

fun <T : Drawable> T.pixelsEqualTo(t: T?) = toBitmap().pixelsEqualTo(t?.toBitmap(), true)

fun Bitmap.bytesEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) = otherBitmap?.let { other ->
    if (width == other.width && height == other.height) {
        val res = toBytes().contentEquals(other.toBytes())
        if (shouldRecycle) {
            doRecycle().also { otherBitmap.doRecycle() }
        }
        res
    } else false
} ?: kotlin.run { false }

fun Bitmap.pixelsEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) = otherBitmap?.let { other ->
    if (width == other.width && height == other.height) {
        val res = Arrays.equals(toPixels(), other.toPixels())
        if (shouldRecycle) {
            doRecycle().also { otherBitmap.doRecycle() }
        }
        res
    } else false
} ?: kotlin.run { false }

fun Bitmap.doRecycle() {
    if (!isRecycled) recycle()
}

fun <T : Drawable> T.toBitmap(): Bitmap {
    if (this is BitmapDrawable) return bitmap
    
    val drawable: Drawable = this
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().use { stream ->
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    stream.toByteArray()
}

fun Bitmap.toPixels() = IntArray(width * height).apply { getPixels(this, 0, width, 0, 0, width, height) }