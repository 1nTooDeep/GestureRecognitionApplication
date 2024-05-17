package com.intoodeep.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.intoodeep.myapplication.composable.BuildBooleanSettingItem
import com.intoodeep.myapplication.composable.Header
import com.intoodeep.myapplication.ui.theme.ApplicationTheme
import com.intoodeep.myapplication.util.SettingItem

val tag = "MappingActivity"
class SettingActivity : AppCompatActivity() {
    private lateinit var sp: SharedPreferences
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag,"onCreate")
        sp = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        if (!isInited(sp)){
            initSharedPreferences(sp)
        }
        enableEdgeToEdge()

        setContent {
            ApplicationTheme {
                Header(
                    context = this,
                    text = applicationContext.resources.getString(R.string.setting),
                    modifier = Modifier,
                    onNavigationIconClick = {
                        val intend = Intent(this,MainActivity::class.java)
                        startActivity(intend)
                    },
                )
                Surface (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp)
                    ,
                ){
                    val booleanItemList = SettingItem.build(
                        this.resources.getIntArray(R.array.default_setting_id),
                        this.resources.getStringArray(R.array.setting_name),
                        this.resources.getIntArray(R.array.default_state),
                        this.resources.getIntArray(R.array.setting_type),
                        this.resources.obtainTypedArray(R.array.icon)
                    )

                    BuildBooleanSettingItem(this,booleanItemList,sp)

                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag,"onResume")
        checkPackage(this )
    }
    private fun isInited(sp:SharedPreferences):Boolean{
        Log.w(tag,"isInited")
        var ids = this.resources.getIntArray(R.array.default_setting_id)
        for(id in ids){
            if (!sp.contains(id.toString()))return false
        }
        return true
    }
    private fun initSharedPreferences(sp:SharedPreferences):Unit{
        var ids = this.resources.getIntArray(R.array.default_setting_id)
        val editor = sp.edit()
        for(id in ids){
            editor.putBoolean(id.toString(),false)
        }
        editor.apply {
            this.commit()
        }
    }
    private fun checkPackage(context: Context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.INSTALL_PACKAGES),1 )
        }
    }
}
