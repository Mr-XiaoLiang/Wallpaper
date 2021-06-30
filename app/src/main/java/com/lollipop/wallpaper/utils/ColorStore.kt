package com.lollipop.wallpaper.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import kotlin.math.min


/**
 * @author lollipop
 * @date 2021/6/23 22:30
 * 颜色的管理器仓库
 */
class ColorStore private constructor(private val colorList: ArrayList<Int>) :
    MutableList<Int> by colorList {

    constructor() : this(ArrayList())

    companion object {

        private const val SAMPLING_SIZE = 96

        private const val MIN_COLOR_OCCURRENCE = 0.05f

        private const val minColorArea = SAMPLING_SIZE * SAMPLING_SIZE * MIN_COLOR_OCCURRENCE

        /**
         * 从一组的Drawable中挨个获取颜色集合并返回
         * @param count 本次遍历集合的数量
         * @param drawableProvider 提供drawable的回调函数
         * @param colorCallback 颜色计算结果的回调函数
         */
        fun getColorListFromDrawable(
            count: Int,
            drawableProvider: (Int) -> Drawable?,
            colorCallback: (Int, IntArray) -> Unit
        ) {
            doAsync {
                val bitmap = Bitmap.createBitmap(
                    SAMPLING_SIZE,
                    SAMPLING_SIZE,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                val srcBounds = Rect()
                for (index in 0 until count) {
                    // 获取drawable
                    val drawable = drawableProvider(index) ?: continue
                    // 保存当前Drawable的原始尺寸
                    srcBounds.set(drawable.bounds)
                    // 设定当前需要的尺寸
                    drawable.setBounds(0, 0, SAMPLING_SIZE, SAMPLING_SIZE)
                    // 清空画布
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    // 绘制得到图案
                    drawable.draw(canvas)

                    // 使用调色盘计算颜色
                    val palette = Palette
                        .from(bitmap)
                        .maximumColorCount(5)
                        .resizeBitmapArea(SAMPLING_SIZE * SAMPLING_SIZE)
                        .generate()

                    // 过滤无效颜色
                    val swatches = ArrayList<Palette.Swatch>(palette.swatches).filter { it.population >= minColorArea }
                    // 对颜色进行排序
                    swatches.sortedBy { it.population }

                    // 得到所有取出的颜色
                    val colorArray = IntArray(swatches.size) { swatches[it].rgb }
                    colorCallback(index, colorArray)
                }
                bitmap.recycle()
            }
        }
    }

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