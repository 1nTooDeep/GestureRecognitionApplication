//package com.intoodeep.myapplication
//
//import android.os.Bundle
//import android.util.Log
//import android.util.Size
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.annotation.OptIn
//import androidx.camera.camera2.Camera2Config
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.findViewTreeViewModelStoreOwner
//import com.google.common.util.concurrent.ListenableFuture
//import kotlinx.coroutines.MainScope
//import kotlinx.coroutines.launch
//import org.pytorch.IValue
//import org.pytorch.Module
//import org.pytorch.Tensor
//import org.pytorch.torchvision.TensorImageUtils
//import java.nio.ByteBuffer
//import java.util.concurrent.Executors
//
//typealias ResultListener = (result: ImgClassifier) -> Unit // 图像分析器的返回结果类型，typealias 是取别名
//
//class CameraXFragment : Fragment(), CameraXConfig.Provider {
//    override fun getCameraXConfig(): CameraXConfig {
//        return Camera2Config.defaultConfig()
//    }
//
//    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider> // 相机的控制者
//    private lateinit var imagePreview: Preview // 图像预览
//    private lateinit var imageAnalysis: ImageAnalysis // 图像分析
//    private val executor = Executors.newSingleThreadExecutor() // 后台线程
//    private lateinit var cameraPreviewView: PreviewView // 显示相机的控件
//    private lateinit var module: Module // 模型
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_camera, container, false)
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext()) // 相机控制权
//        cameraPreviewView =   // 显示相机控件
//
//        // 加载图片识别模型
//        try {
//            module = Module.load("C3D.pth")
//        } catch (e: Exception) {
//            Log.e(CameraXFragment::class.java.simpleName, e.toString())
//        }
//
//        // 加载相机
//        cameraPreviewView.post { startCamera() }
//    }
//
//    private fun startCamera() {
//        // 预览
//        imagePreview = Preview.Builder().apply {
//            setTargetAspectRatio(AspectRatio.RATIO_16_9)
//            setTargetRotation(previewView.display.rotation)
//        }.build()
//        imagePreview.setSurfaceProvider(previewView.previewSurfaceProvider)
//
//        // 分析
//        imageAnalysis = ImageAnalysis.Builder().apply {
//            setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            setTargetResolution(Size(224, 224))
//        }.build()
//        imageAnalysis.setAnalyzer(executor, ImageClassificationAnalyzer(module) {
//            MainScope().launch {
//                textView2.text = it.getImageClassification()
//                textView3.text = it.getGarbageIndex().toString()
//                textView4.text = it.getGarbageValue().toString()
//
//            }
//            Log.v(CameraXFragment::class.java.simpleName, it.toString())
//        })
//
//        // 绑定
//        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
//        cameraProviderFuture.addListener(Runnable {
//            val cameraProvider = cameraProviderFuture.get()
//            cameraProvider.bindToLifecycle(this, cameraSelector, imagePreview, imageAnalysis)
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }
//
//    // 图像分类器
//    private class ImageClassificationAnalyzer(module: Module, listener: ResultListener?=null) : ImageAnalysis.Analyzer {
//
//        private val mModule = module
//        private val listeners = ArrayList<ResultListener>().apply { listener?.let { add(it) } }
//
//        private fun ByteBuffer.toByteArray(): ByteArray {
//            rewind()    // Rewind the buffer to zero
//            val data = ByteArray(remaining())
//            get(data)   // Copy the buffer into a byte array
//            return data // Return the byte array
//        }
//
//        @OptIn(ExperimentalGetImage::class)
//        override fun analyze(imageProxy: ImageProxy) {
//            if (listeners.isEmpty()) {
//                imageProxy.close()
//                return
//            }
//
//            val buffer = imageProxy.planes[0].buffer
//            val data = buffer.toByteArray()
//
//            // 图像识别
//            val inputTensorBuffer = Tensor.allocateFloatBuffer(3*224*224) // 输入数据格式设置
//            val inputTensor = Tensor.fromBlob(inputTensorBuffer, longArrayOf(1, 3, 224, 224)) // 转化成tensor
//
//            TensorImageUtils.imageYUV420CenterCropToFloatBuffer( // 加载图片
//                imageProxy.image,0, 224, 224,
//                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
//                TensorImageUtils.TORCHVISION_NORM_STD_RGB,
//                inputTensorBuffer, 0)
//
//            val outputTensor = mModule.forward(IValue.from(inputTensor)).toTensor() // 使用模型进行图像识别
//            val scores = outputTensor.dataAsFloatArray
//            var topScore = 0.0f
//            var topIndex = 0
//            for (index in scores.indices) { // 获取识别结果可能性最大的
//                if (topScore < scores[index]) {
//                    topScore = scores[index]
//                    topIndex = index
//                }
//            }
//
//            // Call all listeners with new value
//            listeners.forEach { it(ImgClassifier(topIndex, topScore)) }
//
//            imageProxy.close()
//        }
//    }
//}