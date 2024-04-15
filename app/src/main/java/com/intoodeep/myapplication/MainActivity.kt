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
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceView
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.createBitmap
import com.intoodeep.myapplication.GestureRecognition.GestureRecognitionModel
import com.intoodeep.myapplication.GestureService.CustomerService
import com.intoodeep.myapplication.GestureService.GestureControlAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.nio.ByteBuffer

const val TAG = "MainActivity"
const val CAMERA_PERMISSION_REQUEST_CODE = 1992
class MainActivity : ComponentActivity() {
    @SuppressLint("MissingInflatedId", "ResourceType", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        // start service
        val startCustomerService = Intent(this,CustomerService::class.java)
        startService(startCustomerService)
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