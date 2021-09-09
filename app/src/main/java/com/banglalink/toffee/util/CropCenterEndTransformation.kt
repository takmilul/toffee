package com.banglalink.toffee.util

import android.graphics.Bitmap
import coil.bitmap.BitmapPool
import coil.size.Size
import jp.wasabeef.transformers.coil.BaseTransformation
import jp.wasabeef.transformers.core.Crop
import jp.wasabeef.transformers.core.bitmapConfig
import jp.wasabeef.transformers.types.GravityHorizontal
import jp.wasabeef.transformers.types.GravityVertical

class CropCenterEndTransformation(ratio: Float) : BaseTransformation(
  Crop(
    aspectRatio = ratio,
    gravityHorizontal = GravityHorizontal.END,
    gravityVertical = GravityVertical.CENTER
  )
) {

  override suspend fun transform(pool: BitmapPool, input: Bitmap, size: Size): Bitmap {
    val calcSize = (transformer as Crop).calculateSize(input)
    val output = pool.get(calcSize.width, calcSize.height, bitmapConfig(input))
    return transformer.transform(input, output)
  }
}