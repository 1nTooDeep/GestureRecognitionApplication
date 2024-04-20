package com.intoodeep.myapplication

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import com.intoodeep.myapplication.GestureService.GestureControlAccessibilityService
import com.intoodeep.myapplication.GestureService.GestureRecognitionService

const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {
    @SuppressLint("MissingInflatedId", "ResourceType", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        // start service
        val startGestureRecognitionService= Intent(this,GestureRecognitionService::class.java)
        startService(startGestureRecognitionService)
        Log.d(TAG,"onCreat")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG,"onRestart")
    }
}