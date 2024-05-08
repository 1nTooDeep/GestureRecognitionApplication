package com.intoodeep.myapplication.ui.theme.composable

import android.content.res.TypedArray
import android.graphics.drawable.Drawable

data class BooleanSettingItem (
    val id:Int,
    val name:String,
    var state : Boolean=false,
    val icon: Drawable?
){
    companion object {
        fun build(
            ids: IntArray,
            names: Array<String>,
            states: IntArray,
            obtainTypedArray: TypedArray
        ): List<BooleanSettingItem> {
            val booleanSettingItems = ArrayList<BooleanSettingItem>()
            for (i in ids.indices) {
                val id = ids[i]
                val name = names[i]
                val state = states[i] != 0 // 将整数状态转换为布尔值
                val icon = obtainTypedArray.getDrawable(i)
                val booleanSettingItem = BooleanSettingItem(id, name, state,icon)
                booleanSettingItems.add(booleanSettingItem)
            }
            return booleanSettingItems
        }
    }
}