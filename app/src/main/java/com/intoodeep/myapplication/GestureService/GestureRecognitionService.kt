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
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.intoodeep.myapplication.CameraUtils.convertImageToGrayscaleResizedBitmap
import com.intoodeep.myapplication.GestureRecognition.GestureRecognitionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val TAG = "CustomerService"
class CustomerService: LifecycleService() {
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
    private val bitmapList = ArrayList<Bitmap>(30)
    private val timestampList = ArrayList<Long>(30)
    private val imageList = ArrayList<Image>(30)
    private var currentIndex = -1
    private lateinit var model : GestureRecognitionModel
    val imageReader :ImageReader= ImageReader
        .newInstance(112,112,ImageFormat.YUV_420_888,30)


    // 生命周期
    override fun onCreate() {
        super.onCreate()
        context = baseContext
        imageReader.setOnImageAvailableListener(object :ImageReader.OnImageAvailableListener{
            override fun onImageAvailable(reader: ImageReader?) {
                var image = reader?.acquireNextImage()
                if (image == null) return
                currentIndex = (currentIndex+1) % 30
                val bitmap = convertImageToGrayscaleResizedBitmap(image,112,112)
                if (bitmapList.size<30){
                    bitmapList.add(bitmap)
                    timestampList.add(System.currentTimeMillis())
                    imageList.add(image)
                }else{
                    bitmapList.set(currentIndex,bitmap)
                    timestampList.set(currentIndex,System.currentTimeMillis())
                }
                image.close()

            }
        },cameraHandler)
        Log.d(TAG,"Service is Created.")
        // load model
        model = GestureRecognitionModel(this.context,"C3D_ori.pt")
    }
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return CustomerBinder()
    }

    override fun onDestroy() {
        Log.d(TAG,"Service is destroyed.")
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG,"Service is started.")
        if (checkCameraPermission()) {
            Log.d(TAG,"Permission check success")
            openCamera()
            createCaptureSession()
        }
        lifecycleScope.launch {
            while (true){
                delay(1000)

                Log.d(TAG,"sendBroadcast")
                // 当图片列表存储30张照片后才开始预测
                if(imageList.size == 30){
                    // img to tensor

                    // 获取模型预测结果
                    var output = model.randomPredict()
                    val index = MaxIndex(output)
                    val intent = Intent("type")
                    intent.setPackage("com.intoodeep.myapplication")
                    intent.putExtra("type",index)
                    sendBroadcast(intent)
                }
            }
        }
        return START_STICKY
    }

    // Binder class
    inner class CustomerBinder: Binder(){
        val service = this@CustomerService
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
        cameraManager.openCamera(cameraID, cameraStateCallback, cameraHandler)
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.P)
    private fun createCaptureSession() = lifecycleScope.launch(Dispatchers.Main) {
        previewViewSurfaceList.add(imageReader.surface)
        Log.d(TAG,"createCaptureSession")
        cameraDevice.createCaptureSession(previewViewSurfaceList,sessionStateCallback,cameraHandler)
    }
    // 创建捕捉请求
    @RequiresApi(Build.VERSION_CODES.S)
    private fun createCaptureRequest() {
        try{
            val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            captureRequest.set(CaptureRequest.SCALER_ROTATE_AND_CROP, 90)
            var target = imageReader.surface
            captureRequest.addTarget(target)
            cameraCaptureSession.setRepeatingRequest(
                captureRequest.build(),null,cameraHandler
            )
            Log.d(TAG,"Capture!")
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
        override fun onError(camera: CameraDevice, error: Int) {}
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
    fun <T : Comparable<T>> MaxIndex(list: List<T>): Int? {
        if (list.isEmpty()) {
            return null
        }
        var maxElement: T = list[0]
        var maxIndex = 0
        for ((index, element) in list.withIndex()) {
            if (element > maxElement) {
                maxElement = element
                maxIndex = index
            }
        }
        return maxIndex
    }

}