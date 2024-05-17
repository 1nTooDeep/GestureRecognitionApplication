package com.intoodeep.myapplication.composable

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.intoodeep.myapplication.GestureService.GestureRecognitionService
import com.intoodeep.myapplication.MainActivity
import com.intoodeep.myapplication.R
import com.intoodeep.myapplication.TAG

@Composable
fun ServiceSwitch(context: Context?,activity: Activity){
    if(context!=null){
        var state = remember{ mutableStateOf(false) }
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        ){
            Text(
                text = context.resources.getString(R.string.main_service_name),
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(15.dp)

            )
            Switch(
                checked = state.value,
                onCheckedChange = {
                    Log.d(TAG,it.toString())
                    checkPermission(context,activity)
                    if (it) {
                        try {
                            val startGestureRecognitionService= Intent(context,
                                GestureRecognitionService::class.java)
                            context.startService(startGestureRecognitionService)
                        }
                        catch  (t:Throwable){
                            t.printStackTrace()
                        }
                    }
                    else {
                        try {
                            val stoptGestureRecognitionService= Intent(context,
                                GestureRecognitionService::class.java)
                            context.stopService(stoptGestureRecognitionService)
                        }
                        catch (t:Throwable){
                            t.printStackTrace()
                        }
                    }
                    state.value = !state.value
                },
//        interactionSource = state
                thumbContent = {
                    if (state.value) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier
                                .size(SwitchDefaults.IconSize + 8.dp),
                        )
                    }
                },
            )
        }
    }

}
private fun checkPermission(context: Context,activity: Activity){
    if(!hasCameraPermission(context)){
        requestCameraPermission(activity)
    }
}
fun requestCameraPermission(activity: Activity) {
    requestPermissions(
        activity,
        arrayOf(Manifest.permission.CAMERA),
        1
    )
}
fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}