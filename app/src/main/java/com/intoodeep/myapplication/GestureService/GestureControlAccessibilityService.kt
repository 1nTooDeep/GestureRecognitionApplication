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
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.hardware.display.DisplayManagerCompat

import com.intoodeep.myapplication.GestureRecognition.GestureEvent


class GestureControlAccessibilityService: AccessibilityService() {
    val TAG = "GestureControlAccessibilityService"
    private val gestureThread = HandlerThread("GestureControlAccessibilityServiceThread").apply { start() }
    private val gestureHandler = Handler(gestureThread.looper)
    private var lastExcuteTime = System.currentTimeMillis()
    private val receiver = Receiver()
    var height = 0
    var width = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }
    override fun onInterrupt() {
        Toast.makeText(this,"Accessibility Interrupt", Toast.LENGTH_SHORT).show()
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onServiceConnected() {
        super.onServiceConnected()
        val intentFilter = IntentFilter()
        intentFilter.addAction("type")
        registerReceiver(receiver,intentFilter, RECEIVER_NOT_EXPORTED)
        Toast.makeText(this,"Accessibility Connect", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate() {
        super.onCreate()
        println("AccessibilityService is start.")
        val display = DisplayManagerCompat.getInstance(this).displays[0]
        val p = Point()
        display.getRealSize(p)
        height = p.y
        width = p.x
        Log.d(TAG, "$height  $width")
        Toast.makeText(this,"AccessibilityService is start.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("AccessibilityService is destroy.")
        unregisterReceiver(receiver)
        Toast.makeText(this,"Accessibility Destroy", Toast.LENGTH_SHORT).show()
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


    inner class Receiver() : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent?.getIntExtra("BROADCAST_ACTION",10)
            Log.d("Receiver",type.toString())
            when(type){
                GestureEvent.NO_GESTURE->{}
                GestureEvent.DOING_OTHER_THINGS->{}
                GestureEvent.SWIPING_DOWN->{this@GestureControlAccessibilityService.takeScreenShot()}
                GestureEvent.SWIPING_RIGHT->{this@GestureControlAccessibilityService.goBack()}
                GestureEvent.SWIPING_UP->{this@GestureControlAccessibilityService.goToHome()}
                GestureEvent.SLIDING_TWO_FINGERS_DOWN->{this@GestureControlAccessibilityService.slideDown()}
                GestureEvent.SLIDING_TWO_FINGERS_LEFT->{this@GestureControlAccessibilityService.slideLeft()}
                GestureEvent.SLIDING_TWO_FINGERS_RIGHT->{this@GestureControlAccessibilityService.slideRight()}
                GestureEvent.SLIDING_TWO_FINGERS_UP->{this@GestureControlAccessibilityService.slideUp()}
                GestureEvent.ZOOMING_IN_WITH_TWO_FINGERS->{this@GestureControlAccessibilityService.zoomIn()}
                GestureEvent.ZOOMING_OUT_WITH_TWO_FINGERS->{this@GestureControlAccessibilityService.zoomOut()}
                }

        }

    }
}