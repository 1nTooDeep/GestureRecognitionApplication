package com.intoodeep.myapplication.util

import android.content.res.TypedArray
import android.graphics.drawable.Drawable

data class AppInfo(
    val name:String,
    val icon: Drawable,
){
    companion object {
        fun build(
            ids: IntArray,
            names: Array<String>,
            icons:TypedArray
        ): List<AppInfo> {
            val appInfos = ArrayList<AppInfo>()
            for (i in ids.indices) {
                val name = names[i]
                val icon = icons.getDrawable(i)
                val appInfo = icon?.let { AppInfo(name, it) }
                if (appInfo != null) {
                    appInfos.add(appInfo)
                }
            }
            return appInfos
        }
    }
}

