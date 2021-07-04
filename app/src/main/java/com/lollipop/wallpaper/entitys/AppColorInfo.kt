package com.lollipop.wallpaper.entitys

/**
 * @author lollipop
 * @date 2021/7/4 11:55
 */
data class AppColorInfo(
    val appInfo: AppInfo,
    val colorArray: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppColorInfo

        if (appInfo != other.appInfo) return false
        if (!colorArray.contentEquals(other.colorArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appInfo.hashCode()
        result = 31 * result + colorArray.contentHashCode()
        return result
    }
}