package com.intoodeep.myapplication.GestureRecognition

import android.content.Context
import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.MemoryFormat
import org.pytorch.Module

import org.pytorch.Tensor
//import org.pytorch.torchvision.TensorImageUtils.bitmapToFloat32Tensor
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.LinkedList


class GestureRecognitionModel() {
    val TAG = "GestureRecognitionModel"
    lateinit var model:Module
    val mean = floatArrayOf(0.2674f, 0.2676f, 0.2648f)
    val std = floatArrayOf(0.4377f, 0.4047f, 0.3925f)
    // 一个Bitmap的容量 W * H * sizeof(float32)
    val rBuffer = ByteBuffer.allocateDirect(112 * 112 * 4 )
    val gBuffer = ByteBuffer.allocateDirect(112 * 112 * 4 )
    val bBuffer = ByteBuffer.allocateDirect(112 * 112 * 4 )
    init {
        rBuffer.order(ByteOrder.LITTLE_ENDIAN)  // Set the byte order to Little-endian
        gBuffer.order(ByteOrder.LITTLE_ENDIAN)  // Set the byte order to Little-endian
        bBuffer.order(ByteOrder.LITTLE_ENDIAN)  // Set the byte order to Little-endian
    }
    fun load(context: Context,name:String){
        this.model = LiteModuleLoader.loadModuleFromAsset(context.assets,name)
    }
    fun predict(bitmaps: LinkedList<Bitmap>): IValue {
        val input = IValue.from(convertBitmapsToTensor(bitmaps))
        return model.forward(input)
    }
    fun convertBitmapsToTensor(bitmaps: LinkedList<Bitmap>): Tensor {
        bBuffer.clear()
        rBuffer.clear()
        gBuffer.clear()
        val byteBuffers = ArrayList<ByteBuffer>()
        for (bitmap in bitmaps) {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            for (i in pixels.indices) {
                val argb = pixels[i]
                val b = ((argb and 0xFF).toFloat() / 255f) - mean[2]// Convert B from int8 to float32
                val g = ((argb shr 8 and 0xFF).toFloat() / 255f) -mean[1] // Convert G from int8 to float32
                val r = ((argb shr 16 and 0xFF).toFloat() / 255f)  -mean[0]// Convert R from int8 to float32
                bBuffer.putFloat(b / std[2]) // Put B as float32
                gBuffer.putFloat(g / std[1]) // Put G as float32
                rBuffer.putFloat(r / std[0]) // Put R as float32
            }
            bBuffer.rewind()
            gBuffer.rewind()
            rBuffer.rewind()
            byteBuffers.add(bBuffer)
            byteBuffers.add(gBuffer)
            byteBuffers.add(rBuffer)
        }

        val concatenatedBuffer = concatenateBuffers(byteBuffers)
        val floatBuffer = concatenatedBuffer.asFloatBuffer()
        // 1 * 30 * 3 * 112 * 112 = 1,128,960
        val shape = longArrayOf(1, 30, 3, 112, 112)
        val memoryFormat = MemoryFormat.CONTIGUOUS
        return Tensor.fromBlob(floatBuffer, shape, memoryFormat)
    }
    private fun concatenateBuffers(buffers: ArrayList<ByteBuffer>): ByteBuffer {
        var capacity = 0
        for (buffer in buffers) {
            capacity += buffer.capacity()
        }
        // double to float32
        val concatenatedBuffer = ByteBuffer.allocateDirect(capacity) // Allocate 4 bytes per float32
        concatenatedBuffer.order(ByteOrder.LITTLE_ENDIAN)
        for (buffer in buffers) {
            concatenatedBuffer.put(buffer)
        }
        concatenatedBuffer.rewind()
        return concatenatedBuffer
    }
    fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName).use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }
        return file.absolutePath
    }


//    fun convertBitmapsToTensorByTorchVision(bitmaps: LinkedList<Bitmap>): Tensor {
//        var tensorArray = ArrayList<Tensor>()
//        for (bitmap in bitmaps){
//            val tensor = bitmapToFloat32Tensor(bitmaps[0],mean,std,MemoryFormat.CONTIGUOUS)
//            tensorArray.add(tensor)
//        }
//        val tensorBuffer = Tensor.allocateFloatBuffer(3 * 30 * 112 * 112)
//        for (tensor in tensorArray){
//            tensorBuffer.put(tensor.dataAsFloatArray)
//        }
//        return Tensor.fromBlob(tensorBuffer, longArrayOf(1,30,3,112,112),MemoryFormat.CONTIGUOUS)
//    }
}


