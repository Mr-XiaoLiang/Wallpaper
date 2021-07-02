package com.lollipop.wallpaper.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import kotlin.math.abs

/**
 * @author lollipop
 * @date 2021/7/2 22:50
 * 调色板辅助工具
 */
class PaletteHelper {

    companion object {

        private const val SAMPLING_SIZE = 96

        private const val MIN_COLOR_OCCURRENCE = 0.05f

        private const val minColorArea = SAMPLING_SIZE * SAMPLING_SIZE * MIN_COLOR_OCCURRENCE

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

        // 过滤无效颜色
        var swatches = ArrayList<Palette.Swatch>(palette.swatches).filter {
            it.population >= minColorArea
        }
        // 对颜色进行排序
        swatches = swatches.sortedBy { it.population }

        // 得到所有取出的颜色
        return IntArray(swatches.size) { swatches[it].rgb }
    }

    /**
     * 为颜色分组
     * @param colorArray 颜色的原始数组，它是有序的，返回结果中将会包含原始颜色的序号信息
     * @param groupCount 分组的数量，它也将决定每一组的颜色数量
     * 它的算法在于，计算每个颜色的HSV值中的色相，然后通过色相值进行排序（不考虑环处理
     * 排序后的色相覆盖范围进行分组，统一色相区域的颜色分为一组。
     * 分组颜色取决于色相区域的中间颜色，饱和度与明度保持为最高
     */
    fun colorGrouping(colorArray: IntArray, groupCount: Int): Array<ColorGroupInfo> {
        // 获取色相并且排序
        val hueArray = getHueArray(colorArray).sortedBy { it.value }

        // 色相分布范围
        val minHue = hueArray[0].value
        // 最大值+1，增加取值范围
        val maxHue = hueArray[hueArray.size - 1].value + 1

        // 范围内计算色相范围步长
        val hueStep = abs(maxHue - minHue) * 1F / groupCount

        val groupList = ArrayList<ColorGroupInfo>()
        val tempHsv = FloatArray(3) { 1F }
        val tempChildrenList = ArrayList<ColorChild>()

        var startHue = minHue
        for (index in 0 until groupCount) {
            // 清理缓存
            tempChildrenList.clear()

            // 计算当前片段中的结束位置
            val endHue = if (index == groupCount - 1) {
                // 如果是最后一个，那么直接食用最大值，避免遗漏
                maxHue
            } else {
                // 采用四舍五入的形式
                (startHue + hueStep + 0.5F).toInt()
            }

            // 计算当前色相颜色
            tempHsv[0] = (endHue + startHue) * 0.5F
            val groupColor = Color.HSVToColor(tempHsv)

            hueArray.forEach {
                // 比较中，使用半包形式，包头不包尾
                if (it.value in startHue until endHue) {
                    tempChildrenList.add(ColorChild(it.color, it.index))
                }
            }

            groupList.add(ColorGroupInfo(groupColor, tempChildrenList.toTypedArray()))

            // 接着上一个的结尾，避免漏掉
            startHue = endHue
        }
        return groupList.toTypedArray()
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