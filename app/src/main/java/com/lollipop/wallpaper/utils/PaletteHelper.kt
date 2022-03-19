package com.lollipop.wallpaper.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import java.lang.StringBuilder
import kotlin.math.min

/**
 * @author lollipop
 * @date 2021/7/2 22:50
 * 调色板辅助工具
 */
class PaletteHelper {

    companion object {

        const val MIN_GROUPING_COUNT = 3
        const val DEFAULT_GROUPING_COUNT = 6
        const val MAX_GROUPING_COUNT = 14

        private const val SAMPLING_SIZE = 96

        private const val MIN_COLOR_OCCURRENCE = 0.05f

        private const val minColorArea = SAMPLING_SIZE * SAMPLING_SIZE * MIN_COLOR_OCCURRENCE

        /**
         * 为颜色分组
         * @param colorArray 颜色的原始数组，它是有序的，返回结果中将会包含原始颜色的序号信息
         * @param groupCount 分组的数量，它也将决定每一组的颜色数量
         * 它的算法在于，计算每个颜色的HSV值中的色相，然后通过色相值进行排序
         * 排序后的色相覆盖范围进行分组，统一色相区域的颜色分为一组。
         * 分组颜色取决于色相区域的中间颜色，饱和度与明度保持为最高
         */
        fun colorGrouping(colorArray: IntArray, groupCount: Int): Array<ColorGroupInfo> {
            if (colorArray.size < 2) {
                return arrayOf()
            }
            // 获取色相并且排序
            val hueArray = getHueArray(colorArray).sortedBy { it.value }

            // 寻找颜色切入点，并且得到色相范围
            val breakthrough = getBreakthrough(hueArray)

            // 范围内计算色相范围步长
            val hueStep = (((breakthrough[1] - breakthrough[0]) * 1F / groupCount) + 0.5F).toInt()

            val groupList = ArrayList<ColorGroupInfo>()

            val tempHsv = FloatArray(3) { 1F }
            val tempChildrenList = ArrayList<ColorChild>()

            // 色相分布范围
            val minHue = breakthrough[0]
            // 最大值+1，增加取值范围
            val maxHue = breakthrough[1] + 1

            var startHue = minHue
            while (startHue < maxHue) {
                // 计算当前片段中的结束位置
                // 采用四舍五入的形式
                val endHue = min(maxHue, startHue + hueStep)

                findChild(tempChildrenList, hueArray, startHue % 360, endHue % 360)

                // 取Children的平均色
                var groupColor = getAverageColor(tempChildrenList)
                if (groupColor == 0) {
                    // 计算当前色相颜色
                    tempHsv[0] = (((endHue + startHue) / 2) % 360).toFloat()
                    groupColor = Color.HSVToColor(tempHsv)
                }

                groupList.add(ColorGroupInfo(groupColor, tempChildrenList.toTypedArray()))

                // 清理缓存
                tempChildrenList.clear()

                // 接着上一个的结尾，避免漏掉
                startHue = endHue
            }
            return groupList.toTypedArray()
        }

        private fun getAverageColor(children: List<ColorChild>): Int {
            if (children.isEmpty()) {
                return Color.TRANSPARENT
            }
            var red = 0L
            var green = 0L
            var blue = 0L
            children.forEach {
                red += Color.red(it.value)
                green += Color.green(it.value)
                blue += Color.blue(it.value)
            }
            val count = children.size
            return Color.rgb(
                (red / count).toInt().range(0, 255),
                (green / count).toInt().range(0, 255),
                (blue / count).toInt().range(0, 255)
            )
        }

        private fun findChild(
            list: MutableList<ColorChild>,
            hueArray: List<Hue>,
            start: Int,
            end: Int
        ) {
            if (start < end) {
                val range = start until end
                hueArray.forEach {
                    // 比较中，使用半包形式，包头不包尾
                    if (it.value in range) {
                        list.add(ColorChild(it.color, it.index))
                    }
                }
            } else {
                val range1 = start..360
                val range2 = 0 until end
                hueArray.forEach {
                    // 比较中，使用半包形式，包头不包尾
                    if (it.value in range1 || it.value in range2) {
                        list.add(ColorChild(it.color, it.index))
                    }
                }
            }
        }

        private fun getHueArray(colorArray: IntArray): List<Hue> {
            val hueArray = ArrayList<Hue>(colorArray.size)
            val tempHsv = FloatArray(3)
            for (index in colorArray.indices) {
                val color = colorArray[index]
                Color.colorToHSV(color, tempHsv)
                // 丢弃明度与饱和度，只记录色相
                hueArray.add(Hue((tempHsv[0] + 0.5F).toInt(), color, index))
            }
            return hueArray
        }

        private fun getBreakthrough(list: List<Hue>): Array<Int> {
            var start = 0
            var end = list.size - 1
            var space = 360 - list[end].value + list[start].value
            for (index in 1 until list.size) {
                val s = list[index].value - list[index - 1].value
                if (s > space) {
                    space = s
                    start = index - 1
                    end = index
                }
            }
            val startHue = list[start].value
            val endHue = list[end].value
            return if (startHue > endHue) {
                arrayOf(startHue, 360 + endHue)
            } else {
                arrayOf(startHue, endHue)
            }
        }

        fun getColorName(color: Int): String {
            val colorValue = Integer.toHexString(color)
            if (colorValue.length == 6 || colorValue.length == 8) {
                return colorValue
            }
            if (colorValue.length < 6) {
                val builder = StringBuilder(colorValue)
                while (builder.length < 6) {
                    builder.insert(0, "0")
                }
                return builder.toString()
            }
            val builder = StringBuilder(colorValue)
            while (builder.length < 8) {
                builder.insert(0, "0")
            }
            return builder.toString()
        }

    }

    private var globalBitmap: Bitmap? = null

    private val globalCanvas = Canvas()

    private val srcBounds = Rect()

    private fun getBitmap(): Bitmap {
        val b = globalBitmap
        if (b != null && !b.isRecycled) {
            return b
        }
        b?.recycle()
        val newBitmap = Bitmap.createBitmap(
            SAMPLING_SIZE,
            SAMPLING_SIZE,
            Bitmap.Config.ARGB_8888
        )
        globalBitmap = newBitmap
        return newBitmap
    }

    private fun getCanvas(): Canvas {
        globalCanvas.setBitmap(getBitmap())
        return globalCanvas
    }

    fun getColor(drawable: Drawable): IntArray {
        // 保存当前Drawable的原始尺寸
        srcBounds.set(drawable.bounds)
        // 设定当前需要的尺寸
        drawable.setBounds(0, 0, SAMPLING_SIZE, SAMPLING_SIZE)
        val canvas = getCanvas()
        // 清空画布
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        // 绘制得到图案
        drawable.draw(canvas)

        // 使用调色盘计算颜色
        val palette = Palette
            .from(getBitmap())
            .maximumColorCount(5)
            .resizeBitmapArea(SAMPLING_SIZE * SAMPLING_SIZE)
            .generate()

        // 对颜色进行排序
        val swatches = ArrayList<Palette.Swatch>(palette.swatches).sortedBy { it.population }

        // 得到所有取出的颜色
        return IntArray(swatches.size) { swatches[it].rgb }
    }

    fun destroy() {
        globalBitmap?.recycle()
        globalBitmap = null
        globalCanvas.setBitmap(null)
    }

    class ColorGroupInfo(
        val groupColor: Int,
        val childrenList: Array<ColorChild>
    )

    class ColorChild(val value: Int, val index: Int)

    class Hue(val value: Int, val color: Int, val index: Int)

}