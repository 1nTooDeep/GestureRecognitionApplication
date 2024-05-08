package com.intoodeep.myapplication.GestureService

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.intoodeep.myapplication.CameraUtils.convertImageToColorfulResizedBitmap
import com.intoodeep.myapplication.GestureRecognition.GestureRecognitionModel
import com.intoodeep.myapplication.GestureService.Utils.findMaxValueIndex
import com.intoodeep.myapplication.GestureService.Utils.rotateBitmap
import com.intoodeep.myapplication.GestureService.Utils.mapping
import com.intoodeep.myapplication.GestureService.Utils.softmax
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.exp
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantReadWriteLock

const val TAG = "GestureRecognitionService"
class GestureRecognitionService: LifecycleService() {
    private lateinit var context: Context
    // camera setting
    private val mainLooperHandler = Handler(Looper.getMainLooper())
    private val sender = BroadcastSender()
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)
    private var cameraID = "0"
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var sp:SharedPreferences
    private val cameraManager: CameraManager by lazy {
        applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private var previewViewSurfaceList = ArrayList<Surface>(30)
    private val bitmapList = LinkedList<Bitmap>()
    private val imageList = LinkedList<Image>()
    private val indexList = LinkedList<Int>()
    private var index = 0
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
        sp  = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        imageReader.setOnImageAvailableListener(object :ImageReader.OnImageAvailableListener{
            @Synchronized
            override fun onImageAvailable(reader: ImageReader?) {
                var image = reader?.acquireNextImage() ?: return
                currentIndex = (currentIndex+1) % 30
                val bitmap = convertImageToColorfulResizedBitmap(image,112,112)
                writeLock.lock()
                if (imageList.size<30){
                    indexList.add(index)
                    bitmapList.add(rotateBitmap(bitmap,-90.0f))
                    imageList.add(image)
                }else{
                    bitmapList.removeFirst()
                    bitmapList.add(rotateBitmap(bitmap,-90.0f))
                    indexList.removeFirst()
                    indexList.add(index)
                }
                currentStride += 1
                index += 1
                writeLock.unlock()

                image.close()
            }
        },cameraHandler)
        Toast.makeText(this,"GestureRecognitionService is start.", Toast.LENGTH_SHORT).show()
    }
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return GestureRecognitionServiceBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice.close()
        Toast.makeText(this,"GestureRecognitionService is start.", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG,"Service is started.")
        model.load(context,"C3D.pt")
        if (checkCameraPermission()) {
            Log.d(TAG,"Permission check success")
            openCamera()
            createCaptureSession()
            lifecycleScope.launch {
                delay(500)
                while (true){
                    // 当图片列表存储30张照片后才开始预测
                    delay(100)
                    if(bitmapList.size == 30 && currentStride > 5){
                        writeLock.lock()
                        currentStride = 0
                        val clone = bitmapList.clone() as LinkedList<Bitmap>
                        writeLock.unlock()
                        // 获取模型预测结果
                        val output = model.predict(clone)
                        val prob = softmax(output.toTensor().dataAsFloatArray)
                        val predictResult = findMaxValueIndex(prob)
                        if (sp.contains(predictResult.toString()) && sp.getBoolean(predictResult.toString(),false) && prob[predictResult] > 0.70){
                            mapping(predictResult)
                            sender.sendBroadcast(predictResult)
//                            Log.d(TAG,softmax(output.toTensor().dataAsFloatArray).asList().toString())
//                            val intent = Intent("type")
//                            intent.setPackage("com.intoodeep.myapplication")
//                            intent.putExtra("BROADCAST_ACTION",predictResult)
//                            try {
//                                sendBroadcast(intent)
//                            }
//                            catch (t:Throwable){
//                                t.printStackTrace()
//                            }
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
        cameraDevice.createCaptureSession(previewViewSurfaceList,sessionStateCallback,cameraHandler)
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
            captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(20,20))
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


    inner class GestureRecognitionServiceBinder: Binder(){
        val service = this@GestureRecognitionService
    }
    inner class BroadcastSender {
        fun sendBroadcast(type:Int){
            val intent = Intent("type")
            intent.setPackage("com.intoodeep.myapplication")
            intent.putExtra("BROADCAST_ACTION",index)
            try {
                this@GestureRecognitionService.sendBroadcast(intent)
            }
            catch (t:Throwable){
                t.printStackTrace()
            }
        }
    }
}