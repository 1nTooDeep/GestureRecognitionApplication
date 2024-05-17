package com.intoodeep.myapplication.GestureService

import android.accessibilityservice.AccessibilityService
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
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
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
        Toast.makeText(this,"AccessibilityService is start.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("AccessibilityService is destroy.")
        unregisterReceiver(receiver)
        Toast.makeText(this,"Accessibility Destroy", Toast.LENGTH_SHORT).show()
    }

//    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
//        return super.bindService(service, conn, flags)
//    }

    // 上划
    fun slideUp(){
        Log.d(TAG,"slideUp")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f,height / 2.0f - 400)
        val stroke = GestureDescription.StrokeDescription(path,0,100)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
        Toast.makeText(this,"上滑", Toast.LENGTH_SHORT).show()
    }
    // 下滑
     fun slideDown(){
        Log.d(TAG,"slideDown")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f,height / 2.0f + 400)
        val stroke = GestureDescription.StrokeDescription(path,0,100)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
        Toast.makeText(this,"下滑", Toast.LENGTH_SHORT).show()
    }
     fun slideRight(){
         Log.d(TAG,"slideRight")
         val path = Path()
         val gestureBuild = GestureDescription.Builder()
         path.moveTo(width / 2.0f,height / 2.0f)
         path.lineTo(width / 2.0f + 400 ,height / 2.0f )
         val stroke = GestureDescription.StrokeDescription(path,0,100)
         gestureBuild.addStroke(stroke)
         gestureBuild.build()
         this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
         Toast.makeText(this,"右滑", Toast.LENGTH_SHORT).show()
    }
     fun slideLeft() {
        Log.d(TAG,"slideLeft")
        val path = Path()
        val gestureBuild = GestureDescription.Builder()
        path.moveTo(width / 2.0f,height / 2.0f)
        path.lineTo(width / 2.0f - 400 ,height / 2.0f )
        val stroke = GestureDescription.StrokeDescription(path,0,100)
        gestureBuild.addStroke(stroke)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
         Toast.makeText(this,"左滑", Toast.LENGTH_SHORT).show()
    }
    // 放大
    fun zoomIn(){
        Log.d(TAG,"ZoomIn")
        val path_1 = Path()
        path_1.moveTo(width / 2.0f,height / 2.0f)
        path_1.lineTo(width / 2.0f + 400,height / 2.0f + 400)
        val path_2 = Path()
        path_2.moveTo(width / 2.0f,height / 2.0f)
        path_2.lineTo(width / 2.0f - 400,height / 2.0f - 400)
        val stroke_1 = GestureDescription.StrokeDescription(path_1,0,100)
        val stroke_2 = GestureDescription.StrokeDescription(path_2,0,100)
        val gestureBuild = GestureDescription.Builder()
        gestureBuild.addStroke(stroke_1)
        gestureBuild.addStroke(stroke_2)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
        Toast.makeText(this,"缩小", Toast.LENGTH_SHORT).show()
    }
    // 缩小
    fun zoomOut(){
        Log.d(TAG,"ZoomIn")
        val path_1 = Path()
        path_1.moveTo(width / 2.0f - 400,height / 2.0f - 400)
        path_1.lineTo(width / 2.0f,height / 2.0f)
        val path_2 = Path()
        path_2.moveTo(width / 2.0f + 400,height / 2.0f + 400)
        path_2.lineTo(width / 2.0f,height / 2.0f)
        val stroke_1 = GestureDescription.StrokeDescription(path_1,0,100)
        val stroke_2 = GestureDescription.StrokeDescription(path_2,0,100)
        val gestureBuild = GestureDescription.Builder()
        gestureBuild.addStroke(stroke_1)
        gestureBuild.addStroke(stroke_2)
        gestureBuild.build()
        this.dispatchGesture(gestureBuild.build(),gestureResultCallback,gestureHandler)
        Toast.makeText(this,"放大", Toast.LENGTH_SHORT).show()
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
        }
        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
        }
    }
    private fun openApp(name:String){
        val intent = Intent()
    }
    private fun stopMedia(){
        val eventTime = SystemClock.uptimeMillis()
        val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null)
        val downEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            0
        )
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent)
        sendOrderedBroadcast(downIntent, null)
    }
    inner class Receiver() : BroadcastReceiver() {
        var lastTime = System.currentTimeMillis()
        init {
            Log.d("BroadcastReceiver","On init.")
        }
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent?.getIntExtra("BROADCAST_ACTION",-1)
//            Log.d(TAG,"recieve $type")
            lastTime = System.currentTimeMillis()
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
                GestureEvent.THUMB_UP->{this@GestureControlAccessibilityService.openApp("")}
                GestureEvent.THUMB_DOWN->{this@GestureControlAccessibilityService.openApp("")}
//                GestureEvent.DRUMMING_FINGERS->{this@GestureControlAccessibilityService.openApp("")}
                GestureEvent.STOP_SIGN->{this@GestureControlAccessibilityService.stopMedia()}
                GestureEvent.ZOOMING_IN_WITH_TWO_FINGERS->{this@GestureControlAccessibilityService.zoomIn()}
                GestureEvent.ZOOMING_OUT_WITH_TWO_FINGERS->{this@GestureControlAccessibilityService.zoomOut()}
                }

        }

    }
}