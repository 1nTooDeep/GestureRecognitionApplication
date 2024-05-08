package com.intoodeep.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intoodeep.myapplication.ui.theme.ApplicationTheme
import com.intoodeep.myapplication.ui.theme.composable.BuildHelpItem
import com.intoodeep.myapplication.ui.theme.composable.Header
import com.intoodeep.myapplication.ui.theme.composable.Help
import com.intoodeep.myapplication.ui.theme.composable.HelpItem
import com.intoodeep.myapplication.ui.theme.composable.ServiceSwitch
import com.intoodeep.myapplication.ui.theme.composable.StartAlart

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationTheme {
                Header(
                    this,
                    this.resources.getString(R.string.help_title),
                    Modifier,
                    {
                        val intent = Intent(this, MainActivity::class.java)
                        this.startActivity(intent)
                    },
                    false
                )
                Surface (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp)
                    ,
                ){
                    val helplist = HelpItem.build(
                        this.resources.getIntArray(R.array.default_setting_id),
                        this.resources.getStringArray(R.array.setting_name),
                        this.resources.getStringArray(R.array.help_description),
                        this.resources.obtainTypedArray(R.array.icon)
                    )
                    BuildHelpItem(
                        this,
                        helplist
                    )
                }
            }

        }
    }
}