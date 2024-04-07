package com.intoodeep.myapplication.GestureService

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.intoodeep.myapplication.GestureRecognition.GestureRecognitionModel

class GestureRecognitionService:Service() {
    val model = GestureRecognitionModel()
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
    }
}