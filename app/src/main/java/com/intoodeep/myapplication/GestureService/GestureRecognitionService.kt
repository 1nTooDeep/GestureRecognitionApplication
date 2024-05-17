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
import com.intoodeep.myapplication.GestureRecognition.GestureEvent
import com.intoodeep.myapplication.GestureRecognition.GestureRecognitionModel
import com.intoodeep.myapplication.GestureService.Utils.bitmapToByteBuffer
import com.intoodeep.myapplication.GestureService.Utils.findMaxValueIndex
import com.intoodeep.myapplication.GestureService.Utils.rotateBitmap
import com.intoodeep.myapplication.GestureService.Utils.mapping
import com.intoodeep.myapplication.GestureService.Utils.softmax
import com.intoodeep.myapplication.GestureService.Utils.torchvisionBitmapToTensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.exp
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantReadWriteLock

const val TAG = "GestureRecognitionService"
class GestureRecognitionService: LifecycleService() {
    private lateinit var context: Context
    // camera setting
    private val mainLooperHandler = Handler(Looper.getMainLooper())
    private val sender = BroadcastSender()
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val netThread = HandlerThread("net").apply { start() }
    private val netHandler = Handler(netThread.looper)
    private val cameraHandler = Handler(cameraThread.looper)
    private var cameraID = "0"
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var sp:SharedPreferences
    private val cameraManager: CameraManager by lazy {
        applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private var previewViewSurfaceList = ArrayList<Surface>(30)
    private val dataStore = DataStore(30)
    private var index = 0
    private var last = -2
    private var model = GestureRecognitionModel()
    private val imageReader :ImageReader= ImageReader
        .newInstance(112,112,ImageFormat.YUV_420_888,30)
    private var currentStride = 0


    override fun onCreate() {
        super.onCreate()
        context = this
        sp  = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        openCamera()
        imageReader.setOnImageAvailableListener(object :ImageReader.OnImageAvailableListener{
            @Synchronized
            override fun onImageAvailable(reader: ImageReader?) {
                var image = reader?.acquireNextImage() ?: return
                // YUV_420_888 -> bitmap
                var bitmap = convertImageToColorfulResizedBitmap(image,112,112)
                // bitmap rotation
                bitmap = rotateBitmap(bitmap,-90.0f)
//                val byteBuffer = bitmapToByteBuffer(bitmap)
//                dataStore.put(byteBuffer)
                dataStore.put(bitmap)
                currentStride += 1
                index += 1

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
        stopCaptureSession()

        Toast.makeText(this,"GestureRecognitionService is start.", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG,"Service is started.")
        model.load(context,"model_timesformer.ptl")
        if (checkCameraPermission()) {
            Log.d(TAG,"Permission check success")

            createCaptureSession()
            lifecycleScope.launch {
                delay(500)
                while (true){
                    // 当图片列表存储30张照片后才开始预测
                    delay(100)
                    if(dataStore.getSize() == 30 && currentStride > 5){
                        currentStride = 0
                        // 获取模型预测结果
//                        val output = model.predict(dataStore.getByteBuffer())
                        val output = model.predict(dataStore.getTensor()).toTensor().dataAsFloatArray
                        val weight = floatArrayOf(0.0F, 0.0F, 0.0F, 0.0F, -0.1F, -0.1F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                        val out = output.zip(weight) {a,b ->a+b}.toFloatArray()
                        val prob = softmax(out)
                        val predictResult = findMaxValueIndex(prob)
                        if (sp.contains(predictResult.toString()) && sp.getBoolean(predictResult.toString(),false) && prob[predictResult] > 0.65){
                            mapping(predictResult)
                            sender.sendBroadcast(predictResult)
                            if (predictResult != GestureEvent.NO_GESTURE && predictResult != GestureEvent.DOING_OTHER_THINGS){
                                dataStore.clear()
                            }
                        }
                    }
                }
            }
            // end of lifecycle
            return START_STICKY
        }else{
            Log.d(TAG,"Permission check fail")
            stopSelf()
        }
        return START_STICKY_COMPATIBILITY
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
            captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(30,30))
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
            intent.putExtra("BROADCAST_ACTION",type)
            try {
                this@GestureRecognitionService.sendBroadcast(intent)
            }
            catch (t:Throwable){
                t.printStackTrace()
            }
        }
    }
}