package com.lollipop.wallpaper

import android.content.Context
import com.lollipop.wallpaper.SharedPreferencesUtils.get
import com.lollipop.wallpaper.SharedPreferencesUtils.set
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KProperty

/**
 * @author lollipop
 * @date 2021/6/3 23:18
 */
class LSettings private constructor(val context: Context) {

    companion object {
        private const val NAME = "name"
        private const val KEY = "key"
        private const val COLOR = "color"
        private const val PACKAGE = "package"

        fun bind(context: Context): LSettings {
            return LSettings(context)
        }
    }

    var padding by SettingDelegator(0.1F)

    private var usageStatsGroupInfo by SettingDelegator("")

    private var usageStatsPackageInfo by SettingDelegator("")

    fun getGroupInfo(): List<UsageStatsGroupInfo> {
        val list = ArrayList<UsageStatsGroupInfo>()
        val jsonInfo = usageStatsGroupInfo
        if (jsonInfo.isNotEmpty()) {
            try {
                val jsonArray = JSONArray(jsonInfo)
                val arrayCount = jsonArray.length()
                for (index in 0 until arrayCount) {
                    jsonArray.optJSONObject(index)?.let { obj ->
                        val name = obj.optString(NAME)
                        val key = obj.optString(KEY)
                        val color = obj.optInt(COLOR)
                        if (name.isNotEmpty() && key.isNotEmpty()) {
                            list.add(
                                UsageStatsGroupInfo(
                                    name = name,
                                    key = key,
                                    color = color
                                )
                            )
                        }
                    }
                }
            } catch (e: Throwable) {
                log(e)
            }
        }
        return list
    }

    fun setGroupInfo(info: List<UsageStatsGroupInfo>) {
        val jsonArray = JSONArray()
        info.forEach { group ->
            jsonArray.put(JSONObject().apply {
                put(NAME, group.name)
                put(KEY, group.key)
                put(COLOR, group.color)
            })
        }
        usageStatsGroupInfo = jsonArray.toString()
    }

    fun getPackageInfo(): List<UsageStatsItemInfo> {
        val list = ArrayList<UsageStatsItemInfo>()
        val jsonInfo = usageStatsPackageInfo
        if (jsonInfo.isNotEmpty()) {
            try {
                val jsonArray = JSONArray(jsonInfo)
                val arrayCount = jsonArray.length()
                for (index in 0 until arrayCount) {
                    jsonArray.optJSONObject(index)?.let { obj ->
                        val name = obj.optString(PACKAGE)
                        val key = obj.optString(KEY)
                        if (name.isNotEmpty() && key.isNotEmpty()) {
                            list.add(
                                UsageStatsItemInfo(
                                    groupKey = key,
                                    packageName = name
                                )
                            )
                        }
                    }
                }
            } catch (e: Throwable) {
                log(e)
            }
        }
        return list
    }

    fun setPackageInfo(info: List<UsageStatsItemInfo>) {
        val jsonArray = JSONArray()
        info.forEach { group ->
            jsonArray.put(JSONObject().apply {
                put(PACKAGE, group.packageName)
                put(KEY, group.groupKey)
            })
        }
        usageStatsPackageInfo = jsonArray.toString()
    }

    private class SettingDelegator<T>(private val default: T) {

        operator fun getValue(lSettings: LSettings, property: KProperty<*>): T {
            return lSettings.context[property.name, default]
        }

        operator fun setValue(lSettings: LSettings, property: KProperty<*>, fl: T) {
            lSettings.context[property.name] = fl
        }

    }

}