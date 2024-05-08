package com.intoodeep.myapplication

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import android.media.Image.Plane
import android.renderscript.Allocation
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.util.Size
import android.view.TextureView


object CameraUtils {
    val input: Allocation? = null
    val out: Allocation? = null
    private val script: ScriptIntrinsicYuvToRGB? = null
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
    fun convertImageToColorfulResizedBitmap(image: Image, newWidth: Int, newHeight: Int): Bitmap {
        image
        val srcWidth = image.width
        val srcHeight = image.height
        val planes = image.planes

        // 创建一个临时ARGB_8888格式的Bitmap用于存储YUV_420_888格式的彩色图像
        val rgbBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)

        // 将YUV_420_888格式转换为ARGB_8888格式的彩色Bitmap
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val yStride = planes[0].rowStride
        val uvStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride

        for (y in 0 until srcHeight) {
            val yIndex = y * yStride
            val uvRowIndex = (y / 2) * uvStride
            for (x in 0 until srcWidth) {
                val uvIndex = uvRowIndex + (x / 2) * uvPixelStride
                val yValue = yBuffer.get(yIndex + x).toInt() and 0xFF
                val uValue = uBuffer.get(uvIndex).toInt() and 0xFF
                val vValue = vBuffer.get(uvIndex).toInt() and 0xFF

                // 使用YUV转换公式将YUV分量转换为RGB分量
                val r = yValue + 1.402 * (vValue - 128)
                val g = yValue - 0.344136 * (uValue - 128) - 0.714136 * (vValue - 128)
                val b = yValue + 1.772 * (uValue - 128)

                // 将RGB值限制在0-255范围内
                val red = Math.max(0, Math.min(255, r.toInt()))
                val green = Math.max(0, Math.min(255, g.toInt()))
                val blue = Math.max(0, Math.min(255, b.toInt()))

                // 将RGB值应用于生成的彩色Bitmap
                rgbBitmap.setPixel(x, y, Color.rgb(red, green, blue))
            }
        }

        // 将ARGB_8888格式的彩色Bitmap转换为指定大小的彩色Bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(rgbBitmap, newWidth, newHeight, true)

        // 释放原始Image，防止内存泄漏
        // image.close()

        return resizedBitmap
    }


}

internal object SizeComparator : Comparator<Size> {
    override fun compare(a: Size, b: Size): Int {
        return b.height * b.width - a.width * a.height
    }
}