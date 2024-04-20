package com.intoodeep.myapplication.GestureRecognition

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import org.pytorch.IValue
import org.pytorch.MemoryFormat
import org.pytorch.Module
import org.pytorch.PyTorchAndroid
import org.pytorch.LiteModuleLoader
import org.pytorch.Tensor
import org.pytorch.DType
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.LinkedList
import com.intoodeep.myapplication.GestureRecognition.GestureRecognitionModel


class GestureRecognitionModel() {
    val TAG = "GestureRecognitionModel"
    lateinit var model:Module
    val dType = DType.FLOAT64
    fun load(context: Context,name:String){
        this.model = LiteModuleLoader.loadModuleFromAsset(context.assets,"model.ptl")
    }
    fun predict(bitmaps:LinkedList<Bitmap>): IValue {
        val input = IValue.from(convertBitmapsToTensor(bitmaps))

        val output : IValue = this.model.forward(input)
        return output
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

    fun convertBitmapsToTensor(bitmaps: LinkedList<Bitmap>): Tensor {
        val byteBuffers = ArrayList<ByteBuffer>()
        for (bitmap in bitmaps) {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            val rgbBuffer = ByteBuffer.allocateDirect(pixels.size * 3 * 4) // Allocate 8 bytes per float64 for each RGB channel
            rgbBuffer.order(ByteOrder.LITTLE_ENDIAN)  // Set the byte order to Little-endian

            for (i in pixels.indices) {
                val argb = pixels[i]
                val b = ((argb and 0xFF).toFloat()).toFloat() // Convert B from int8 to float64
                val g = ((argb shr 8 and 0xFF).toFloat()).toFloat() // Convert G from int8 to float64
                val r = ((argb shr 16 and 0xFF).toFloat()).toFloat() // Convert R from int8 to float64

                rgbBuffer.putFloat(b) // Put B as float64
                rgbBuffer.putFloat(g) // Put G as float64
                rgbBuffer.putFloat(r) // Put R as float64
            }

            rgbBuffer.rewind()
            byteBuffers.add(rgbBuffer)
        }
        val concatenatedBuffer = concatenateBuffers(byteBuffers)
        val floatBuffer = concatenatedBuffer.asFloatBuffer()
        // 1 * 3 * 30 * 112 * 112 = 1,128,960
        val shape = longArrayOf(1, 3, 30, 112, 112)
        val memoryFormat = MemoryFormat.CONTIGUOUS
        return Tensor.fromBlob(floatBuffer, shape, memoryFormat)
    }
    private fun concatenateBuffers(buffers: ArrayList<ByteBuffer>): ByteBuffer {
        var capacity = 0
        for (buffer in buffers) {
            capacity += buffer.remaining()
        }
        // double to float32
        val concatenatedBuffer = ByteBuffer.allocateDirect(capacity) // Allocate 4 bytes per float64
        concatenatedBuffer.order(ByteOrder.LITTLE_ENDIAN)
        for (buffer in buffers) {
            concatenatedBuffer.put(buffer)
        }
        concatenatedBuffer.rewind()

        return concatenatedBuffer
    }
}


