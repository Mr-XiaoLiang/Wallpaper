package com.lollipop.wallpaper.utils

import android.content.Context
import com.lollipop.wallpaper.utils.SharedPreferencesUtils.get
import com.lollipop.wallpaper.utils.SharedPreferencesUtils.set
import com.lollipop.wallpaper.engine.UsageStatsGroupInfo
import com.lollipop.wallpaper.engine.UsageStatsItemInfo
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

        private val defaultPresetColor: Array<Int> = arrayOf(
            0xFFB0F566.toInt(),
            0xFF4AF2A1.toInt(),
            0xFF5CC9F5.toInt(),
            0xFF6638F0.toInt(),
            0xFFF78AE0.toInt(),
            0xFFE273DB.toInt(),
            0xFFEE6751.toInt(),
            0xFFE9E9E9.toInt(),
            0xFF4584D5.toInt(),
            0xFF4EAA86.toInt(),
            0xFFED9F54.toInt()
        )
    }

    var padding by SettingDelegator(0.1F)

    private var usageStatsGroupInfo by SettingDelegator("")

    private var usageStatsPackageInfo by SettingDelegator("")

    private var presetColorList by SettingDelegator("")

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
        var hasDefaultGroup = false
        list.forEach {
            if (it.key == UsageStatsGroupInfo.DEFAULT_GROUP_KEY) {
                hasDefaultGroup = true
            }
        }
        if (!hasDefaultGroup) {
            list.add(UsageStatsGroupInfo.createDefault(context))
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

    fun getPresetColorList(): List<Int> {
        val list = ArrayList<Int>()
        val jsonInfo = presetColorList
        if (jsonInfo.isNotEmpty()) {
            try {
                val jsonArray = JSONArray(jsonInfo)
                val arrayCount = jsonArray.length()
                for (index in 0 until arrayCount) {
                    jsonArray.optInt(index).let { color ->
                        if (color != 0) {
                            list.add(color)
                        }
                    }
                }
            } catch (e: Throwable) {
                log(e)
            }
        }
        if (list.isEmpty()) {
            list.addAll(defaultPresetColor)
        }
        return list
    }

    fun setPresetColorList(colorList: List<Int>) {
        val jsonArray = JSONArray()
        colorList.forEach {
            jsonArray.put(it)
        }
        presetColorList = jsonArray.toString()
    }

    fun isNeedShowGuide(any: Any): Boolean {
        val name = getGuideKey(any)
        return context[name, true]
    }

    fun onGuideShown(any: Any) {
        val name = getGuideKey(any)
        return context.set(name, false)
    }

    private fun getGuideKey(any: Any): String {
        return any::class.java.name + "guide"
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