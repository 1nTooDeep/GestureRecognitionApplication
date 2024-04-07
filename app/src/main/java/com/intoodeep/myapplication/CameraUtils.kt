package com.intoodeep.myapplication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import android.util.Size
import android.view.TextureView

object CameraUtils {

    /** Return the biggest preview size available which is smaller than the window */
    private fun findBestPreviewSize(windowSize: Size, characteristics: CameraCharacteristics):
            Size {
        val supportedPreviewSizes: List<Size> =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(SurfaceTexture::class.java)
                ?.filter { SizeComparator.compare(it, windowSize) >= 0 }
                ?.sortedWith(SizeComparator)
                ?: emptyList()

        return supportedPreviewSizes.getOrElse(0) { Size(0, 0) }
    }

    /**
     * Returns a new SurfaceTexture that will be the target for the camera preview
     */
    fun buildTargetTexture(
        containerView: TextureView,
        characteristics: CameraCharacteristics
    ): SurfaceTexture? {

        /*** Codelab --> Change this function to handle viewfinder rotation and scaling ***/

        val windowSize = Size(containerView.width, containerView.height)
        val previewSize = findBestPreviewSize(windowSize, characteristics)

        return containerView.surfaceTexture?.apply {
            setDefaultBufferSize(previewSize.width, previewSize.height)
        }
    }
    fun convertImageToGrayscaleResizedBitmap(image: Image, newWidth: Int, newHeight: Int): Bitmap {
        val srcWidth = image.width
        val srcHeight = image.height
        val planes = image.planes

        // 创建一个临时ARGB_8888格式的Bitmap用于存储YUV_420_888格式的灰度图像
        val rgbBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)

        // 将YUV_420_888格式转换为ARGB_8888格式的灰度Bitmap
        val yBuffer = planes[0].buffer
        val yStride = planes[0].rowStride
        val uvStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride

        for (y in 0 until srcHeight) {
            val yIndex = y * yStride
            val uvRowStart = if (y % 2 == 0) 0 else uvStride / 2 // 因为YUV_420_888格式的UV平面每两行共享一行数据
            for (x in 0 until srcWidth) {
                val uvIndex = uvRowStart + x * uvPixelStride
                val grey = (yBuffer[yIndex + x].toInt() and 0xFF) // Y分量作为灰度值
                rgbBitmap.setPixel(x, y, Color.argb(255, grey, grey, grey))
            }
        }

        // 将ARGB_8888格式的灰度Bitmap转换为指定大小的灰度Bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(rgbBitmap, newWidth, newHeight, true)

        // 释放原始Image，防止内存泄漏
        image.close()

        return resizedBitmap
    }
    fun bitMaplistToTensor(bitmapList:ArrayList<Bitmap>){

    }
}

internal object SizeComparator : Comparator<Size> {
    override fun compare(a: Size, b: Size): Int {
        return b.height * b.width - a.width * a.height
    }
}