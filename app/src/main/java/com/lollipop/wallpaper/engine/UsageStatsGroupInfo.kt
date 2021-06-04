package com.lollipop.wallpaper.engine

import android.content.Context
import android.graphics.Color
import com.lollipop.wallpaper.R

/**
 * @author lollipop
 * @date 2021/6/2 23:24
 * 使用情况的组信息
 */
data class UsageStatsGroupInfo(
    val key: String,
    val name: String,
    val color: Int,
) {
    companion object {
        const val DEFAULT_GROUP_KEY = "DEFAULT_OTHER"
        private const val DEFAULT_GROUP_NAME = R.string.default_group_name
        private const val DEFAULT_GROUP_COLOR = Color.GRAY

        fun createDefault(context: Context): UsageStatsGroupInfo {
            return UsageStatsGroupInfo(
                DEFAULT_GROUP_KEY,
                context.getString(DEFAULT_GROUP_NAME),
                DEFAULT_GROUP_COLOR
            )
        }
    }
}

data class UsageStatsItemInfo(
    val groupKey: String,
    val packageName: String
)