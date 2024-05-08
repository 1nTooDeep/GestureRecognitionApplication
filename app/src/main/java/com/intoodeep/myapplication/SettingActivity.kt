package com.intoodeep.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.intoodeep.myapplication.ui.theme.ApplicationTheme
import com.intoodeep.myapplication.ui.theme.composable.BooleanSettingItem
import com.intoodeep.myapplication.ui.theme.composable.BuildBooleanSettingItem
import com.intoodeep.myapplication.ui.theme.composable.Header

val tag = "MappingActivity"
class MappingActivity : AppCompatActivity() {
    private lateinit var sp: SharedPreferences
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
                    val itemList = BooleanSettingItem.build(
                        this.resources.getIntArray(R.array.default_setting_id),
                        this.resources.getStringArray(R.array.setting_name),
                        this.resources.getIntArray(R.array.default_state),
                        this.resources.obtainTypedArray(R.array.icon)
                    )
                    BuildBooleanSettingItem(this,itemList,sp)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag,"onResume")
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
}
