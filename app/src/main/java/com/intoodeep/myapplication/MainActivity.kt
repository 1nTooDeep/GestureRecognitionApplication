package com.intoodeep.myapplication

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.SurfaceView
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.createBitmap
import com.intoodeep.myapplication.GestureRecognition.GestureRecognitionModel
import com.intoodeep.myapplication.GestureService.CustomerService
import kotlinx.coroutines.flow.MutableStateFlow

const val TAG = "MainActivity"
const val CAMERA_PERMISSION_REQUEST_CODE = 1992
class MainActivity : ComponentActivity() {
    lateinit var surfaceView: SurfaceView
    val model = GestureRecognitionModel()


    @SuppressLint("MissingInflatedId", "ResourceType", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        surfaceView = findViewById(R.id.surfaceView)
        val startCustomerService = Intent(this,CustomerService::class.java)
        startService(startCustomerService)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"onStart")

        val botton = findViewById<Button>(R.id.servicebotton)
        botton.setOnClickListener {
            Log.d(TAG,"Botton is click")
            var bindIntent = Intent(this,CustomerService::class.java)
            var serviceConnector = object :ServiceConnection{
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                }
                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }
            bindService(bindIntent,serviceConnector, BIND_AUTO_CREATE)
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG,"onRestart")
    }


    private fun checkService(context: Context): MutableStateFlow<Boolean> {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val services = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        var accessibilityServiceClassName = "com.intoodeep.myapplication/.GestureService.GestureControlAccessibilityService"
        var flag = services.any { it.id ==  accessibilityServiceClassName}
        return MutableStateFlow(flag)
    }
    private fun openCameraWithPermissionCheck() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }


}

//override fun onResume() {
//    super.onResume()
//    Log.d(TAG,"onResume")
//
//}
//
//override fun onPause() {
//    super.onPause()
//    Log.d(TAG,"onPause")
//}
//
//override fun onDestroy() {
//    super.onDestroy()
//    Log.d(TAG,"onDestroy")
//}
//
//override fun onStop() {
//    super.onStop()
//    Log.d(TAG,"onStop")
//}