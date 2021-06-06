package com.lollipop.wallpaper.utils

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.util.ArrayMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.lollipop.wallpaper.engine.UsageStatsGroupInfo
import com.lollipop.wallpaper.engine.UsageStatsItemInfo

/**
 * @author lollipop
 * @date 2021/6/2 22:28
 * 包使用状态辅助工具
 */
class PackageUsageHelper(private val context: Context) {

    companion object {
        /**
         * 默认是3点开始
         */
        const val DEFAULT_LOAD_TIME_OFFSET = 1000L * 60 * 60 * 3

        /**
         * 一天的时间
         */
        const val ONE_DAY = 1000L * 60 * 60 * 24

        private const val LOCK_KEY = "AppInfoLock"

        private const val TIME_MODE_KEEP_LENGTH = true

        /**
         * 应用原始信息
         */
        private val appResolveInfo = ArrayList<AppResolveInfo>()

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

        /**
         * 获取包名对应的应用名称
         */
        fun getLabel(context: Context, packageName: String): CharSequence {
            synchronized(LOCK_KEY) {
                for (info in appResolveInfo) {
                    if (packageName == info.pkgName) {
                        return info.getLabel(context.packageManager)
                    }
                }
                return ""
            }
        }

        /**
         * 获取包名对应的图标
         */
        fun loadIcon(context: Context, packageName: String): Drawable? {
            synchronized(LOCK_KEY) {
                for (info in appResolveInfo) {
                    if (packageName == info.pkgName) {
                        return info.loadIcon(context.packageManager)
                    }
                }
                return null
            }
        }

        private var needReloadAppInfo = true

        /**
         * 注册应用安装包变化的监听器
         * 主要用于软件服务长时间运行的情况下，需要更新包信息的场景
         */
        fun registerPackageChangeReceiver(context: Context): BroadcastReceiver {
            // 广播声明
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    needReloadAppInfo = true
                }
            }
            // 广播注册
            context.registerReceiver(
                receiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addAction(Intent.ACTION_PACKAGE_REPLACED)
                    addAction(Intent.ACTION_PACKAGE_CHANGED)
                }
            )
            // 关联生命周期，解除广播注册
            if (context is LifecycleOwner) {
                context.lifecycle.addObserver(object : LifecycleObserver {
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    fun onDestroy() {
                        context.unregisterReceiver(receiver)
                        context.lifecycle.removeObserver(this)
                    }
                })
            }
            return receiver
        }
    }

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

    /**
     * app信息的集合
     */
    val appInfoList = ArrayList<AppInfo>()

    /**
     * 设定统计时间分割线的偏移量
     */
    var loadTimeOffset = DEFAULT_LOAD_TIME_OFFSET

    private var onAppInfoChanged = true

    /**
     * 加载数据
     */
    fun loadUsageData() {
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

        val result = usageStatsManager.queryAndAggregateUsageStats(getStartTime(now), now)

        usageStatsList.addAll(result.values)

        if (usageStatsList.isEmpty()) {
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
     * 加载APP的信息
     */
    fun loadAppInfo() {
        synchronized(LOCK_KEY) {
            if (appResolveInfo.isEmpty() || needReloadAppInfo) {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN)
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                val appList = pm.queryIntentActivities(mainIntent, 0)
                appResolveInfo.clear()
                for (info in appList) {
                    appResolveInfo.add(AppResolveInfo(info))
                }
                onAppInfoChanged = true
            }
        }
        val tempAppInfoList = ArrayList<AppInfo>()
        tempAppInfoList.addAll(appInfoList)
        appInfoList.clear()

        val iconCache: HashMap<String, Drawable>?
        if (onAppInfoChanged) {
            // app的信息重新加载了，那么也丢掉图标的缓存
            iconCache = null
        } else {
            iconCache = HashMap()
            tempAppInfoList.forEach { appInfo ->
                iconCache[appInfo.packageName] = appInfo.icon
            }
        }
        tempAppInfoList.clear()
        val packageManager = context.packageManager
        appResolveInfo.forEach { appResolveInfo ->
            val pkgName = appResolveInfo.pkgName
            val usageTime = getUsageTimeByPackage(pkgName)
            val label = appResolveInfo.getLabel(packageManager)
            val icon = iconCache?.get(pkgName) ?: appResolveInfo.loadIcon(packageManager)
            tempAppInfoList.add(AppInfo(pkgName, icon, label, usageTime))
        }
        appInfoList.clear()
        appInfoList.addAll(tempAppInfoList)
        onAppInfoChanged = false
    }

    private fun getStartTime(now: Long): Long {
        return if (TIME_MODE_KEEP_LENGTH) {
            now - ONE_DAY
        } else {
            val duration = now % ONE_DAY
            if (duration < loadTimeOffset) {
                // 不到偏移时间就往前一天
                now - duration + loadTimeOffset - ONE_DAY
            } else {
                now - duration + loadTimeOffset
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
        return UsageStatsGroupInfo.DEFAULT_GROUP_KEY
    }

    private fun getUsageTimeByPackage(packageName: String): Long {
        if (usageStatsList.isEmpty()) {
            return 0L
        }
        return usageStatsList.find { it.packageName == packageName }?.totalTimeInForeground ?: 0
    }

    private class AppResolveInfo(
        val resolveInfo: ResolveInfo,
        var label: CharSequence = ""
    ) {
        val pkgName: String = resolveInfo.activityInfo.packageName

        fun getLabel(packageManager: PackageManager): CharSequence {
            if (label.isEmpty()) {
                val newLabel = resolveInfo.loadLabel(packageManager)
                label = newLabel
            }
            return label
        }

        fun loadIcon(packageManager: PackageManager): Drawable {
            return resolveInfo.loadIcon(packageManager)
        }

    }

    data class AppInfo(
        val packageName: String,
        val icon: Drawable,
        val label: CharSequence,
        val usageTime: Long
    )

}