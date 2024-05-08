package com.intoodeep.myapplication.ui.theme.composable

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.intoodeep.myapplication.ui.theme.displayFontFamily

@Composable
fun BuildBooleanSettingItem(
    context: Context,
    itemList:List<BooleanSettingItem>,
    sharedPreferences: SharedPreferences,
    states: List<Boolean>? = null,

){
    val editor = sharedPreferences.edit()
    val switchStates = remember {
        mutableStateListOf<Boolean>().apply {
            addAll(itemList.map { item ->
                sharedPreferences.getBoolean(item.id.toString(), false)
            })
        }
    }
    LazyColumn (
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
    ){
        items(itemList.size){index ->
            val item = itemList[index]
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(70.dp),
                shape = RoundedCornerShape(9.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                border = BorderStroke(1.dp, Color.LightGray),
                colors = CardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ){
                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding()
                        .fillMaxWidth()
                ){
                    Text (
                        text = item.name,
                        modifier = Modifier
                            .padding(start = 20.dp, top = 20.dp, bottom = 20.dp)
                            .width(100.dp),
                        fontFamily = displayFontFamily
                    )
                    val icon = item.icon

                    if (icon != null) {
                        Icon(
                            bitmap = icon.toBitmap(100,100).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                    CustomSwitch(
                        modifier = Modifier
                            .padding(start = 180.dp, end = 25.dp)
                        ,
                        checked = switchStates[index],
                        onCheckedChange = {
                            switchStates[index] = it
                            editor.putBoolean(item.id.toString(),it)
                            editor.apply {
                                this.commit()
                            }
                            Log.d("SharedPreferences", "Changes committed to SharedPreferences")
                        }
                    )
                }

            }
        }
    }
}