package com.intoodeep.myapplication.ui.theme.composable

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.intoodeep.myapplication.R

@Composable
fun StartAlart(context: Context){
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // 当用户点击对话框以外的地方或者按下系统返回键将会执行的代码
                openDialog.value = false
            },
            title = {
                Text(
                    text = "开启手势识别控制服务",
                )
            },
            text = {
                Text(
                    text = context.resources.getString(R.string.start_alart),
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    },
                ) {
                    Text(
                        context.resources.getString(R.string.confirm),
                        fontWeight = FontWeight.W700,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { System.exit(0) }) {
                    Text(
                        context.resources.getString(R.string.cancel),
                        fontWeight = FontWeight.W700,
                    )
                }
            }
        )
    }
}