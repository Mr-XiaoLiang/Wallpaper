package com.lollipop.wallpaper.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette

/**
 * @author lollipop
 * @date 2021/7/2 22:50
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
        val swatches = ArrayList<Palette.Swatch>(palette.swatches).filter {
            it.population >= minColorArea
        }
        // 对颜色进行排序
        swatches.sortedBy { it.population }

        // 得到所有取出的颜色
        return IntArray(swatches.size) { swatches[it].rgb }
    }

    fun destroy() {
        globalBitmap?.recycle()
        globalBitmap = null
        globalCanvas.setBitmap(null)
    }

}