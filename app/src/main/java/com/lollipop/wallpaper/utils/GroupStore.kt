package com.lollipop.wallpaper.utils

import com.lollipop.wallpaper.engine.UsageStatsGroupInfo

/**
 * @author lollipop
 * @date 2021/6/23 22:30
 * 颜色的管理器仓库
 */
class GroupStore private constructor(private val groupList: ArrayList<UsageStatsGroupInfo>) :
    MutableList<UsageStatsGroupInfo> by groupList {

    companion object {
        fun generateKey(name: String): String {
            return UsageStatsGroupInfo.generateKey(name)
        }
    }

    constructor() : this(ArrayList())

    fun saveTo(settings: LSettings) {
        settings.setGroupInfo(groupList)
    }

    fun reload(settings: LSettings, callback: (after: Boolean) -> Unit) {
        groupList.clear()
        callback(false)
        doAsync {
            groupList.addAll(settings.getGroupInfo())
            onUI {
                callback(true)
            }
        }
    }

    fun reloadSync(settings: LSettings) {
        groupList.clear()
        groupList.addAll(settings.getGroupInfo())
    }

    fun change(key: String, name: String, color: Int, callback: ((index: Int) -> Unit)? = null) {
        doAsync {
            val index = changeSync(key, name, color)
            if (callback != null) {
                onUI {
                    callback(index)
                }
            }
        }
    }

    fun changeSync(key: String, name: String, color: Int): Int {
        val info = findByKey(key)
        return if (info != null) {
            val index = groupList.indexOf(info)
            groupList[index] = UsageStatsGroupInfo(key, name, color)
            index
        } else {
            groupList.add(0, UsageStatsGroupInfo(key, name, color))
            0
        }
    }

    fun putSync(name: String, color: Int): String {
        val generateKey = generateKey(name)
        changeSync(generateKey, name, color)
        return generateKey
    }

    fun put(name: String, color: Int, callback: ((index: Int) -> Unit)? = null) {
        change(generateKey(name), name, color, callback)
    }

    fun findByKey(key: String): UsageStatsGroupInfo? {
        groupList.forEach {
            if (it.key == key) {
                return it
            }
        }
        return null
    }

}