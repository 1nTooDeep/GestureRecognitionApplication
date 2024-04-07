package com.intoodeep.myapplication.GestureService

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import androidx.camera.core.ImageAnalysis
import kotlinx.coroutines.Dispatchers
import java.util.Objects
import androidx.lifecycle.lifecycleScope

class CameraMonitoringService : Service() {
    private val mainLooperHandler = Handler(Looper.getMainLooper())

    private val cameraThread = HandlerThread("MyCameraThread").apply { start() }

    private val cameraHandler = Handler(cameraThread.looper)

    private val cameraID = "0"
    private val cameraManager: CameraManager by lazy {
        applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private var cameraDevice: CameraDevice? = null
    private var surface: Surface? = null
    private lateinit var textureView: TextureView
    private lateinit var imageAnalyzer: ImageAnalysis

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        openCameraWithPermissionCheck()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun openCameraWithPermissionCheck() {

    }


}