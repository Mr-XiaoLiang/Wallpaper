package com.lollipop.wallpaper.entitys

import android.graphics.drawable.Drawable

/**
 * @author lollipop
 * @date 2021/7/4 11:56
 */
data class AppInfo(
    val packageName: String,
    val icon: Drawable,
    val label: CharSequence,
    val usageTime: Long
)