package com.intoodeep.myapplication.BroadcastReceiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.intoodeep.myapplication.GestureRecognition.GestureEvent
import com.intoodeep.myapplication.GestureService.GestureControlAccessibilityService

class Receiver(gestureControlAccessibilityService:GestureControlAccessibilityService):BroadcastReceiver() {
    val gestureControlAccessibilityService:GestureControlAccessibilityService
    init {
        this.gestureControlAccessibilityService = gestureControlAccessibilityService
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onReceive(context: Context?, intent: Intent?) {
        val type = intent?.getIntExtra("type",1)
        Log.d("Receiver",type.toString())
        if (context == null)return
        if (isAccessibilityServiceEnabled(context)){
            when(type){
                GestureEvent.NO_GESTURE->{}
                GestureEvent.DOING_OTHER_THINGS->{}
                GestureEvent.SWIPING_DOWN->{this.gestureControlAccessibilityService.takeScreenShot()}
                GestureEvent.SWIPING_RIGHT->{this.gestureControlAccessibilityService.goBack()}
                GestureEvent.SWIPING_UP->{this.gestureControlAccessibilityService.goToHome()}
                GestureEvent.SLIDING_TWO_FINGERS_DOWN->{this.gestureControlAccessibilityService.slideDown()}
                GestureEvent.SLIDING_TWO_FINGERS_LEFT->{this.gestureControlAccessibilityService.slideLeft()}
                GestureEvent.SLIDING_TWO_FINGERS_RIGHT->{this.gestureControlAccessibilityService.slideRight()}
                GestureEvent.SLIDING_TWO_FINGERS_UP->{this.gestureControlAccessibilityService.slideUp()}
                GestureEvent.ZOOMING_IN_WITH_TWO_FINGERS->{this.gestureControlAccessibilityService.zoomIn()}
                GestureEvent.ZOOMING_OUT_WITH_TWO_FINGERS->{this.gestureControlAccessibilityService.zoomOut()}
            }
        }
    }
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, GestureControlAccessibilityService::class.java)

        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
            ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)

            if (enabledService != null && enabledService == expectedComponentName)
                return true
        }
        return false
    }
}