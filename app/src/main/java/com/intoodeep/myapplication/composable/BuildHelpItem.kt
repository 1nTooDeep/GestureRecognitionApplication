package com.intoodeep.myapplication.composable

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.intoodeep.myapplication.ui.theme.AppTypography
import com.intoodeep.myapplication.ui.theme.bodyFontFamily
import com.intoodeep.myapplication.ui.theme.displayFontFamily
import com.intoodeep.myapplication.util.HelpItem

@Composable
fun BuildHelpItem(
    context: Context,
    itemList:List<HelpItem>,

    ){
    Box(
        modifier = Modifier
    ){
        LazyColumn (
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
        )   {
            items(itemList.size){index->
                val item = itemList[index]
                Card(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .border(BorderStroke(1.dp, Color.White))
                        .padding(top = 0.dp, bottom = 18.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                ){
                    Text(
                        text = item.name,
                        style = AppTypography.titleMedium,
                        modifier = Modifier
                            .padding(top = 5.dp, start = 10.dp, bottom = 0.dp),
                        fontFamily = displayFontFamily
                    )
                    Text(
                        text = item.description,
                        style = AppTypography.bodyMedium,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp),
                        fontFamily = bodyFontFamily
                    )
                }
            }
        }
    }
}