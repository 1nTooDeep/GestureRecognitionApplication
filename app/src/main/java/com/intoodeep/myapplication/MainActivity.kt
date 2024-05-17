package com.intoodeep.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.intoodeep.myapplication.ui.theme.ApplicationTheme
import com.intoodeep.myapplication.composable.Header
import com.intoodeep.myapplication.composable.Help
import com.intoodeep.myapplication.composable.ServiceSwitch
import com.intoodeep.myapplication.composable.StartAlart

const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {
    private lateinit var sp: SharedPreferences
    @SuppressLint("MissingInflatedId", "ResourceType", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sp = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        enableEdgeToEdge()
        setContent {
            ApplicationTheme {
                if (!sp.contains("firstStart")){
                    StartAlart(this)
                    sp.edit().putBoolean("firstStart",true).apply()
                }
                Header(
                    this,
                    this.resources.getString(R.string.title),
                    Modifier,{
                        val intent = Intent(this, SettingActivity::class.java)
                        this.startActivity(intent)
                    },
                    true,

                )
                Surface (
                    modifier = Modifier
                        .fillMaxSize()
//                    .fillMaxHeight()
                        .padding(top = 100.dp)
                    ,
                ){
                    ServiceSwitch(context = this, activity = this)
                    Button(
                        modifier = Modifier
                            .padding(bottom = 80.dp, top = 660.dp, start = 100.dp, end = 100.dp)
                            .size(30.dp,20.dp)
                        ,

                        shape = RoundedCornerShape(12.dp),
                        onClick = {

                        },
                        enabled = true,
//                        border = BorderStroke(2.dp, Brush.sweepGradient())
                    ){
                        Text(
                            text = resources.getString(R.string.click_to_accessibility),
                            modifier = Modifier.clickable(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    startActivity(intent)
                                }
                            )

                        )
                    }
                    Help(context = this)
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG,"onRestart")
    }
    private fun check(){}
    private fun checkPermission(context: Context){
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA),1 )
        }
    }
    private fun checkPackage(context: Context){
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.INSTALL_PACKAGES),1 )
        }
    }
}




