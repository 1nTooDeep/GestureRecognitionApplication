package com.intoodeep.myapplication.GestureService.Utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.intoodeep.myapplication.GestureService.TAG


public fun findMaxValueIndex(arr: DoubleArray): Int {
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
fun mapping(type:Int){
    when(type){
        0->{Log.d(TAG,"两指上划")}
        1->{Log.d(TAG,"两指左划")}
        2->{Log.d(TAG,"两指右划")}
        3->{Log.d(TAG,"两指下划")}
        4->{Log.d(TAG,"两指缩小")}
        5->{Log.d(TAG,"两指放大")}
        6->{Log.d(TAG,"全手右划")}
        7->{Log.d(TAG,"全指上滑")}
        8->{Log.d(TAG,"全指下滑")}
        9->{Log.d(TAG,"向上大拇哥")}
        10->{Log.d(TAG,"向下大拇哥")}
        11->{Log.d(TAG,"停止手势")}
        12->{Log.d(TAG,"做其他")}
        13->{Log.d(TAG,"没手势")}
    }
}
fun softmax(floatArray: FloatArray):DoubleArray{
    val exponentials = floatArray.map { Math.exp(it.toDouble()) }
    val sum = exponentials.sum()

    // 计算softmax值
    return floatArray.map { Math.exp(it.toDouble()) / sum }.toDoubleArray()
}