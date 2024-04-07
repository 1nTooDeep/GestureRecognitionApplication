package com.intoodeep.myapplication.GestureService

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.intoodeep.myapplication.MainActivity

class GestureControlAccessibilityService: AccessibilityService() {
    // 当有 AccessibilityEvent 发生时回调此方法
    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SwitchIntDef")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        /* TODO("Not yet implemented")

         */


    }
    // 当某个视图获取焦点时可能需要重写此方法来决定是否执行默认操作
    override fun onInterrupt() {
        TODO("Not yet implemented")
    }
    // 当服务连接到 Accessibility API 时调用
    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        // 可以根据实际需求设置具体的反馈类型和监听事件类型
        info.flags = AccessibilityServiceInfo.DEFAULT

        info.packageNames = emptyArray()

        setServiceInfo(info)
    }

    override fun onCreate() {
        super.onCreate()
        println("AccessibilityService is start.")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("AccessibilityService is destroy.")
    }
    // 上划
    fun SlideUp(){
    }
    // 下滑
    fun SlideDown(){

    }
    // 放大
    fun Enlarge(){

    }
    // 缩小
    fun ZoomOut(){

    }
    // 返回
    fun GoBack(){
        performGlobalAction(GLOBAL_ACTION_BACK)
    }
    // 主页
    fun GoToHome(){
        performGlobalAction(GLOBAL_ACTION_HOME)
    }
    // 截屏
    @RequiresApi(Build.VERSION_CODES.P)
    fun TakeScreenShot(){
        performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }
}