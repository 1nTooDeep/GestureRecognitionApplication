package com.intoodeep.myapplication.ui.theme.composable

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intoodeep.myapplication.HelpActivity
import com.intoodeep.myapplication.MappingActivity

@Composable
fun Help(context: Context?){
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .padding(bottom = 20.dp)
            .fillMaxWidth()
    ){
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 2.dp, end = 2.dp)

        )
        Text(
            text = "帮助",
            fontSize = 20.sp,
            modifier = Modifier.clickable(
                onClick = {
                    val intent = Intent(context, HelpActivity::class.java)
                    context?.startActivity(intent)
                }
            )
        )
    }
}
