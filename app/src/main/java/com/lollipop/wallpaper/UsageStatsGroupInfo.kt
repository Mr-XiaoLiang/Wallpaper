package com.lollipop.wallpaper

/**
 * @author lollipop
 * @date 2021/6/2 23:24
 * 使用情况的组信息
 */
data class UsageStatsGroupInfo(
    val key: String,
    val name: String,
    val color: Int,
)

data class UsageStatsItemInfo(
    val groupKey: String,
    val packageName: String
)