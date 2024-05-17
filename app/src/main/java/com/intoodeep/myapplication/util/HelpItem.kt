package com.intoodeep.myapplication.util

import android.content.res.TypedArray
import android.graphics.drawable.Drawable

data class HelpItem(
    val id:Int,
    val name:String,
    val description:String,
    val icon:Drawable?
) {
    companion object {
        fun build(
            ids: IntArray,
            names: Array<String>,
            descriptions: Array<String>,
            icons:TypedArray
        ): List<HelpItem> {
            val booleanSettingItems = ArrayList<HelpItem>()
            for (i in ids.indices) {
                val id = ids[i]
                val name = names[i]
                val description = descriptions[i]
                val icon = icons.getDrawable(i)
                val helpItem = HelpItem(id, name, description,icon)
                booleanSettingItems.add(helpItem)
            }
            return booleanSettingItems
        }
    }
}