package com.lollipop.wallpaper

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.ArrayMap
import kotlin.collections.ArrayList

/**
 * @author lollipop
 * @date 2021/6/2 22:28
 * 包使用状态辅助工具
 */
class PackageUsageHelper(private val context: Context) {

    /**
     * 原始的应用信息集合
     */
    private val usageStatsList = ArrayList<UsageStats>()

    /**
     * 包与组信息的对应字典
     */
    private val packageGroupMap = ArrayList<UsageStatsItemInfo>()

    /**
     * 组统计信息的合集
     */
    private val groupUsageStats = ArrayMap<String, Long>()

    companion object {
        /**
         * 默认是3点开始
         */
        const val DEFAULT_LOAD_TIME_OFFSET = 1000L * 60 * 60 * 3

        /**
         * 一天的时间
         */
        const val ONE_DAY = 1000L * 60 * 60 * 24

        fun openSettingsPage(context: Context): Boolean {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
                return true
            } catch (e: Throwable) {
                log(e)
            }
            return false
        }
    }

    /**
     * 设定统计时间分割线的偏移量
     */
    var loadTimeOffset = DEFAULT_LOAD_TIME_OFFSET

    /**
     * 加载数据
     */
    fun loadData() {
        usageStatsList.clear()
        groupUsageStats.clear()

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        // 拿不到就不取了
        usageStatsManager ?: return

        if (loadTimeOffset < 0L) {
            loadTimeOffset = 0L
        }
        if (loadTimeOffset > ONE_DAY) {
            loadTimeOffset %= ONE_DAY
        }

        val now = System.currentTimeMillis()
        val duration = now % ONE_DAY
        val startTime = if (duration < loadTimeOffset) {
            // 不到偏移时间就往前一天
            now - duration + loadTimeOffset - ONE_DAY
        } else {
            now - duration + loadTimeOffset
        }

        val result = usageStatsManager.queryAndAggregateUsageStats(startTime, now)

        usageStatsList.addAll(result.values)

        if (usageStatsList.isEmpty() || packageGroupMap.isEmpty()) {
            return
        }
        usageStatsList.forEach { stats ->
            val groupKey = getGroupKeyByPackage(stats.packageName)
            if (groupKey.isNotEmpty()) {
                val timeLength = groupUsageStats[groupKey] ?: 0L
                groupUsageStats[groupKey] = timeLength + stats.totalTimeInForeground
            }
        }
    }

    /**
     * 更新组信息
     */
    fun updateGroupMap(pkgInfo: List<UsageStatsItemInfo>) {
        packageGroupMap.clear()
        packageGroupMap.addAll(pkgInfo)
    }

    val isEmpty: Boolean
        get() {
            return usageStatsList.isEmpty() || groupUsageStats.isEmpty()
        }

    operator fun get(groupKey: String): Long {
        return groupUsageStats[groupKey] ?: 0L
    }

    private fun getGroupKeyByPackage(packageName: String): String {
        packageGroupMap.forEach {
            if (it.packageName == packageName) {
                return it.groupKey
            }
        }
        return ""
    }

}