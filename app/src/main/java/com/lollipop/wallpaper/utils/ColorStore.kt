package com.lollipop.wallpaper.utils

import kotlin.math.min

/**
 * @author lollipop
 * @date 2021/6/23 22:30
 * 颜色的管理器仓库
 */
class ColorStore private constructor(private val colorList: ArrayList<Int>) :
    MutableList<Int> by colorList {

    constructor() : this(ArrayList())

    fun saveTo(settings: LSettings) {
        val maxIndex = min(colorList.size, 256)
        settings.setPresetColorList(colorList.subList(0, maxIndex - 1))
    }

    fun reload(settings: LSettings, callback: (after: Boolean) -> Unit) {
        colorList.clear()
        callback(false)
        doAsync {
            colorList.addAll(settings.getPresetColorList())
            onUI {
                callback(true)
            }
        }
    }

    fun put(color: Int, callback: (index: Int) -> Unit) {
        doAsync {
            if (!colorList.contains(color)) {
                colorList.add(0, color)
                onUI {
                    callback(0)
                }
            }
        }
    }

}