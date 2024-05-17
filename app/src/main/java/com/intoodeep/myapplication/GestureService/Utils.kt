package com.intoodeep.myapplication.GestureService.Utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.intoodeep.myapplication.GestureService.TAG
import org.pytorch.MemoryFormat
import org.pytorch.torchvision.TensorImageUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp

val SIZE_OF_FLOAT = 4

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
    return floatArray.map { exp(it.toDouble()) / sum }.toDoubleArray()
}
fun torchvisionBitmapToTensor(bitmap: Bitmap){

    val mean = floatArrayOf(0.2674f, 0.2676f, 0.2648f)
    val std = floatArrayOf(0.4377f, 0.4047f, 0.3925f)
    val tensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,mean,std,MemoryFormat.CONTIGUOUS)
    Log.d("Shape","${tensor.shape()}")
}
fun bitmapToByteBuffer(bitmap: Bitmap):ByteBuffer{
    val mean = floatArrayOf(0.2674f, 0.2676f, 0.2648f)
    val std = floatArrayOf(0.4377f, 0.4047f, 0.3925f)
    val rBuffer = ByteBuffer.allocateDirect(bitmap.width * bitmap.height * SIZE_OF_FLOAT )
    val gBuffer = ByteBuffer.allocateDirect(bitmap.width * bitmap.height * SIZE_OF_FLOAT )
    val bBuffer = ByteBuffer.allocateDirect(bitmap.width * bitmap.height * SIZE_OF_FLOAT )
    rBuffer.order(ByteOrder.LITTLE_ENDIAN)
    gBuffer.order(ByteOrder.LITTLE_ENDIAN)
    bBuffer.order(ByteOrder.LITTLE_ENDIAN)

    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    for(i in pixels.indices){
        val argb = pixels[i]
        val b = ((argb and 0xFF) / 255f) - mean[2]// Convert B from int8 to float32
        val g = ((argb shr 8 and 0xFF) / 255f) - mean[1] // Convert G from int8 to float32
        val r = ((argb shr 16 and 0xFF) / 255f)  - mean[0]// Convert R from int8 to float32
        bBuffer.putFloat(b / std[2]) // Put B as float32
        gBuffer.putFloat(g / std[1]) // Put G as float32
        rBuffer.putFloat(r / std[0]) // Put R as float32
    }
    val concatenatedBuffer = ByteBuffer.allocateDirect(bBuffer.capacity() + gBuffer.capacity() + rBuffer.capacity())
    concatenatedBuffer.order(ByteOrder.LITTLE_ENDIAN)
    bBuffer.rewind()
    gBuffer.rewind()
    rBuffer.rewind()
    concatenatedBuffer.put(bBuffer)
    concatenatedBuffer.put(gBuffer)
    concatenatedBuffer.put(rBuffer)
    concatenatedBuffer.rewind()
    return concatenatedBuffer
}
