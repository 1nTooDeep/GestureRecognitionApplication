package com.intoodeep.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intoodeep.myapplication.ui.theme.ApplicationTheme
import com.intoodeep.myapplication.ui.theme.composable.Header
import com.intoodeep.myapplication.ui.theme.composable.Help
import com.intoodeep.myapplication.ui.theme.composable.ServiceSwitch
import com.intoodeep.myapplication.ui.theme.composable.StartAlart

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
                        val intent = Intent(this, MappingActivity::class.java)
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
                    ServiceSwitch(context = this)
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
}




