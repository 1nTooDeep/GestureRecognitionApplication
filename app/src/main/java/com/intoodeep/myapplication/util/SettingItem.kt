package com.intoodeep.myapplication.util

import android.content.res.TypedArray
import android.graphics.drawable.Drawable

data class SettingItem (
    val id:Int,
    val name:String,
    var state : Boolean=false,
    val type:Int,
    val icon: Drawable?

){
    companion object {
        fun build(
            ids: IntArray,
            names: Array<String>,
            states: IntArray,
            types: IntArray,
            obtainTypedArray: TypedArray
        ): List<SettingItem> {
            val booleanSettingItems = ArrayList<SettingItem>()
            for (i in ids.indices) {
                val id = ids[i]
                val name = names[i]
                val state = states[i] != 0 // 将整数状态转换为布尔值
                val type = types[i]
                val icon = obtainTypedArray.getDrawable(i)
                val booleanSettingItem = SettingItem(id, name, state,type,icon)
                booleanSettingItems.add(booleanSettingItem)
            }
            return booleanSettingItems
        }
    }
}