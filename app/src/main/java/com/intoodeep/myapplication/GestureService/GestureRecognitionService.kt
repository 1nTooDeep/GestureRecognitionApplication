package com.intoodeep.myapplication.GestureService

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.intoodeep.myapplication.CameraUtils.convertImageToColorfulResizedBitmap
import com.intoodeep.myapplication.GestureRecognition.GestureRecognitionModel
import com.intoodeep.myapplication.GestureService.Utils.findMaxValueIndex
import com.intoodeep.myapplication.GestureService.Utils.rotateBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Random
import java.util.concurrent.Executor
import java.util.concurrent.locks.ReentrantReadWriteLock

const val TAG = "GestureRecognitionService"
class GestureRecognitionService: LifecycleService() {
    private lateinit var context: Context
    // camera setting
    private val mainLooperHandler = Handler(Looper.getMainLooper())
    private val cameraThread = HandlerThread("MyCameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)
    private var cameraID = "0"
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private val cameraManager: CameraManager by lazy {
        applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    var previewViewSurfaceList = ArrayList<Surface>(30)
    private val bitmapList = LinkedList<Bitmap>()
    private val imageList = LinkedList<Image>()
    private var currentIndex = -1
    private var model = GestureRecognitionModel()
    val imageReader :ImageReader= ImageReader
        .newInstance(112,112,ImageFormat.YUV_420_888,30)
    private val reentrantReadWriteLock = ReentrantReadWriteLock()
    private val writeLock = reentrantReadWriteLock.writeLock()
    private val readLock = reentrantReadWriteLock.readLock()
    private var currentStride = 0
    // 生命周期
    override fun onCreate() {
        super.onCreate()
        context = this
        imageReader.setOnImageAvailableListener(object :ImageReader.OnImageAvailableListener{
            @Synchronized
            override fun onImageAvailable(reader: ImageReader?) {
                var image = reader?.acquireNextImage() ?: return
                currentIndex = (currentIndex+1) % 30
                val bitmap = convertImageToColorfulResizedBitmap(image,112,112)
                writeLock.lock()
                if (imageList.size<30){
                    bitmapList.add(rotateBitmap(bitmap,-90.0f))
                    imageList.add(image)
                }else{
                    bitmapList.removeFirst()
                    bitmapList.add(rotateBitmap(bitmap,-90.0f))
                }
                currentStride += 1
                writeLock.unlock()

                image.close()
            }
        },cameraHandler)
        Log.d(TAG,"Service is Created.")
    }
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return GestureRecognitionServiceBinder()
    }

    override fun onDestroy() {
        Log.d(TAG,"Service is destroyed.")
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG,"Service is started.")
        model.load(context,"model.ptl")
        if (checkCameraPermission()) {
            Log.d(TAG,"Permission check success")
            openCamera()
            createCaptureSession()
            lifecycleScope.launch {
                delay(500)
                while (true){
                    delay(500)
                    // 当图片列表存储30张照片后才开始预测
                    if(bitmapList.size == 30){
                        if (currentStride < 3){
                            continue
                        }
                        writeLock.lock()
                        currentStride = 0
                        val clone = bitmapList.clone() as LinkedList<Bitmap>
                        writeLock.unlock()
                        // 获取模型预测结果
                        val output = model.predict(clone)
                        val predict = findMaxValueIndex(output.toTensor().dataAsFloatArray)
                        Log.d(TAG,predict.toString())
                        val intent = Intent("type")
                        intent.setPackage("com.intoodeep.myapplication")
                        intent.putExtra("BROADCAST_ACTION",predict)
                        try {
                            sendBroadcast(intent)
                        }
                        catch (t:Throwable){
                            t.printStackTrace()
                        }
                    }
                }
            }
            // end of lifecycle
            return START_STICKY
        }
        Log.d(TAG,"Permission check fail")
        return START_STICKY_COMPATIBILITY
    }

    // Binder class
    inner class GestureRecognitionServiceBinder: Binder(){
        val service = this@GestureRecognitionService
    }

    //  相机调用逻辑为: 获得相机权限 -> 打开相机-> 创建捕捉会话 -> 捕捉图片 -> 数据流传输 -> 释放相机

    // 权限验证，通过则打开相机
    private fun checkCameraPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )return true
        return false
    }

    // 打开相机
    @SuppressLint("MissingPermission")
    private fun openCamera() = lifecycleScope.launch(Dispatchers.Main) {
        // 找前置相机
        cameraManager.cameraIdList.forEach {
            val tempCameraCharacteristics = cameraManager.getCameraCharacteristics(it)
            if(tempCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                cameraID = it
        }
        Log.d(TAG,"cameraID:$cameraID")
        var fpsRanges = cameraManager.getCameraCharacteristics(cameraID).get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
        Log.d("FPS", "SYNC_MAX_LATENCY_PER_FRAME_CONTROL: " + fpsRanges.toString())
        cameraManager.openCamera(cameraID, cameraStateCallback, cameraHandler)
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.P)
    private fun createCaptureSession() = lifecycleScope.launch(Dispatchers.Main) {
        previewViewSurfaceList.add(imageReader.surface)
//        val outputConfiguration = OutputConfiguration(0,imageReader.surface)
//        val sessionConfiguration = SessionConfiguration(
//            SessionConfiguration.SESSION_REGULAR,
//            listOf(outputConfiguration),
//            mainExecutor,
//            sessionStateCallback
//            )
//        Log.d(TAG,"createCaptureSession")
        cameraDevice.createCaptureSession(previewViewSurfaceList,sessionStateCallback,cameraHandler)
//        cameraDevice.createCaptureSession(sessionConfiguration)
    }

    private fun stopCaptureSession(){
        cameraDevice.close()
    }
    // 创建捕捉请求
    @RequiresApi(Build.VERSION_CODES.S)
    private fun createCaptureRequest() {
        try{
            val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            captureRequest.set(CaptureRequest.SCALER_ROTATE_AND_CROP, 0)
            captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(10,10))
            val target = imageReader.surface
            captureRequest.addTarget(target)
            cameraCaptureSession.setRepeatingRequest(
                captureRequest.build(),null,cameraHandler
            )
        }
        catch (t: Throwable) {
            Log.e(TAG, "Failed to open camera preview.", t)
        }
    }

    // 相机运行过程中需要的回调对象
    private val cameraStateCallback = object : CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera

            Log.d(TAG,"cameraDevice:${cameraDevice.toString()}")
            mainLooperHandler.post{
            }
        }
        override fun onDisconnected(camera: CameraDevice) {}
        override fun onError(camera: CameraDevice, error: Int) {
            Log.d(TAG,"cameraStateCallback error")
        }
    }
    private val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onConfigured(session: CameraCaptureSession) {
            cameraCaptureSession = session

            createCaptureRequest()
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            cameraCaptureSession = session
        }
    }
    private val captureCallback = object :CameraCaptureSession.CaptureCallback() {
        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) {
            super.onCaptureStarted(session, request, timestamp, frameNumber)
        }
        override fun onCaptureFailed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            failure: CaptureFailure
        ) {
            super.onCaptureFailed(session, request, failure)
        }
    }
}