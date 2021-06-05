package com.lollipop.wallpaper.engine

import android.content.Context
import android.graphics.Color
import com.lollipop.wallpaper.R
import java.security.MessageDigest

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

        private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

        private fun md5(input: String): String {
            return printHexBinary(
                MessageDigest
                    .getInstance("MD5")
                    .digest(
                        input.toByteArray()
                    )
            )
        }

        private fun printHexBinary(data: ByteArray): String {
            val r = StringBuilder(data.size * 2)
            data.forEach { b ->
                val i = b.toInt()
                r.append(HEX_CHARS[i shr 4 and 0xF])
                r.append(HEX_CHARS[i and 0xF])
            }
            return r.toString()
        }

        fun generateKey(name: String): String {
            return "K_${md5(name)}"
        }

    }
}

data class UsageStatsItemInfo(
    val groupKey: String,
    val packageName: String
)