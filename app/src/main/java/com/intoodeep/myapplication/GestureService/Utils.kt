package com.intoodeep.myapplication.GestureService.Utils

import android.graphics.Bitmap
import android.graphics.Matrix


public fun findMaxValueIndex(arr: FloatArray): Int {
    if (arr.isEmpty()) {
        throw IllegalArgumentException("Input array must not be empty.")
    }

    var maxIndex = 0
    var maxValue = arr[0]

    for (i in 1 until arr.size) {
        if (arr[i] > maxValue) {
            maxIndex = i
            maxValue = arr[i]
        }
    }

    return maxIndex
}
fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height,
        matrix, true
    )
}