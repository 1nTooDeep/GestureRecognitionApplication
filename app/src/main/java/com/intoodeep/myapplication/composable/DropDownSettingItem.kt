package com.intoodeep.myapplication.composable

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.intoodeep.myapplication.HelpActivity
import com.intoodeep.myapplication.ui.theme.AppTypography
import com.intoodeep.myapplication.ui.theme.bodyFontFamily
import com.intoodeep.myapplication.ui.theme.displayFontFamily
import com.intoodeep.myapplication.util.AppInfo

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DropDownSettingItem(
    id:Int,
    sp:SharedPreferences
    ){
    val items = arrayOf("无","微信","QQ","支付宝","抖音","淘宝","微博","百度")
    val editor = sp.edit()
    var state by remember { mutableStateOf(false) }
    var text by remember { mutableIntStateOf(0) }
    Text(
        text = items[text],
        modifier = Modifier
            .clickable(
            onClick = {
                state = !state
            }
        )
            .padding(start = 150.dp, end = 5.dp)
            .width(70.dp)
        ,
        textAlign = TextAlign.Center,
        style = AppTypography.titleMedium,
        fontFamily = displayFontFamily
    )
    DropdownMenu(
        expanded = state,
        modifier = Modifier
            .height(200.dp)
            .width(130.dp)
            .background(Color.LightGray)
            ,
        onDismissRequest = { state = !state},
        offset = DpOffset(200.dp,0.dp)
    ){
        for (index in items.indices) { // 使用索引遍历items数组
            val item = items[index]
            Text(
                text = item,
                modifier = Modifier
                    .clickable(
                        onClick = {
                            editor.putString(id.toString(),items[index])
                            Log.d("select", index.toString())
                            text = index // 更新text变量为选中的索引
                            state = !state
                        }
                    )
                    .width(130.dp),
                textAlign = TextAlign.Center
            )
        }
    }

}
