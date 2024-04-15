package com.intoodeep.myapplication.GestureService

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Path
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.lifecycle.LifecycleService
import com.intoodeep.myapplication.BroadcastReceiver.Receiver


class GestureControlAccessibilityService: AccessibilityService() {
    val TAG = "GestureControlAccessibilityService"
    private val gestureThread = HandlerThread("GestureControlAccessibilityServiceThread").apply { start() }
    private val gestureHandler = Handler(gestureThread.looper)
    private var lastExcuteTime = System.currentTimeMillis()
    private val receiver = Receiver(this)
    var height = 0
    var width = 0
    init {
        val metrics = DisplayMetrics()
        height = metrics.heightPixels
        width = metrics.widthPixels
    }
    // 当有 AccessibilityEvent 发生时回调此方法
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null)return
        System.currentTimeMillis()

        if (System.currentTimeMillis() - lastExcuteTime >1000 && event.eventType!=AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            lastExcuteTime = System.currentTimeMillis()
            slideUp()
            Log.d(TAG,event.eventType.toString())
        }

    }
    // 当某个视图获取焦点时可能需要重写此方法来决定是否执行默认操作
    override fun onInterrupt() {
        TODO("Not yet implemented")
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        // 可以根据实际需求设置具体的反馈类型和监听事件类型
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.packageNames = emptyArray()
        setServiceInfo(info)
        Log.d(TAG ,"onServiceConnected")
        val intentFilter = IntentFilter()
        intentFilter.addAction("type")
        registerReceiver(receiver,intentFilter, RECEIVER_NOT_EXPORTED)
        Log.d(TAG,"registerReceiver")
    }

    override fun onCreate() {
        super.onCreate()
        println("AccessibilityService is start.")
        var display = DisplayManagerCompat.getInstance(this).displays.get(0)
        var p = Point()
        display.getRealSize(p)
        height = p.y
        width = p.x
        Log.d(TAG, "$height  $width")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("AccessibilityService is destroy.")
        unregisterReceiver(receiver)
    }

    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        return super.bindService(service, conn, flags)
    }

    // 上划
    fun slideUp(){
        Log.d(TAG,"slideUp")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f,height / 2.0f - 800)
        val stroke = GestureDescription.StrokeDescription(path,0,200)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
    }
    // 下滑
     fun slideDown(){
        Log.d(TAG,"slideDown")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f - 400,height / 2.0f )
        val stroke = GestureDescription.StrokeDescription(path,0,200)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
    }
     fun slideRight(){
        Log.d(TAG,"slideRight")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f + 400,height / 2.0f )
        val stroke = GestureDescription.StrokeDescription(path,0,200)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
    }
     fun slideLeft() {
        Log.d(TAG,"slideLeft")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f,height / 2.0f + 800)
        val stroke = GestureDescription.StrokeDescription(path,0,200)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
    }
    // 放大
    fun zoomIn(){
        Log.d(TAG,"ZoomIn")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f,height / 2.0f + 800)
        val stroke = GestureDescription.StrokeDescription(path,0,200)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
    }
    // 缩小
    fun zoomOut(){
        Log.d(TAG,"ZoomOut")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f,height / 2.0f + 800)
        val stroke = GestureDescription.StrokeDescription(path,0,200)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
    }
    // 返回
    fun goBack(){
        performGlobalAction(GLOBAL_ACTION_BACK)
    }
    // 主页
    fun goToHome(){
        performGlobalAction(GLOBAL_ACTION_HOME)
    }
    // 截屏
    @RequiresApi(Build.VERSION_CODES.P)
    fun takeScreenShot(){
        performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }
    val gestureResultCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?){
            super.onCompleted(gestureDescription)
            Log.d(TAG,"success")
        }
        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            Log.d(TAG,"cancelled")
        }
    }
}