package com.lollipop.wallpaper

import android.graphics.*
import kotlin.math.max
import kotlin.math.min

/**
 * 壁纸的绘制者
 * 它抽象于View与Drawable之上，只需要提供绘制基本的绘制条件，
 * 就可以进行绘制，目的在于抛开绘制场景的局限
 * @author Lollipop
 * @date 2021/06/02
 */
class WallpaperPainter {

    private val colorArray = ArrayList<Int>()

    private val weightArray = ArrayList<Int>()

    private val bounds = Rect()

    private val shaderArray = ArrayList<Shader>()

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    private var totalWeight = 0

    var paddingWeight = 0.1F
        set(value) {
            if ((field * 1000).toInt() != (value * 1000).toInt()) {
                field = value
                notifyDataSetChange()
            }
        }

    var backgroundColor: Int
        get() {
            return paint.color
        }
        set(value) {
            paint.color = value
        }

    fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    fun changeBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (bounds.left != left
            || bounds.top != top
            || bounds.right != right
            || bounds.bottom != bottom
        ) {
            bounds.set(left, top, right, bottom)
            notifyDataSetChange()
        }
    }

    fun changeColors(colors: IntArray) {
        if (!intArrayEquals(colors, colorArray)) {
            colorArray.clear()
            colors.forEach {
                colorArray.add(it)
            }
            notifyDataSetChange()
        }
    }

    fun changeWeights(weights: IntArray) {
        if (!intArrayEquals(weights, weightArray)) {
            totalWeight = 0
            weightArray.clear()
            weights.forEach {
                weightArray.add(it)
                totalWeight += it
            }
            notifyDataSetChange()
        }
    }

    private fun intArrayEquals(array1: IntArray, array2: List<Int>): Boolean {
        if (array1.size != array2.size) {
            return false
        }
        for (index in array1.indices) {
            if (array1[index] != array2[index]) {
                return false
            }
        }
        return true
    }

    private fun notifyDataSetChange() {
        shaderArray.clear()
    }

    private fun checkWallpaper() {
        if (shaderArray.isNotEmpty()) {
            return
        }
        val maxCount = max(colorArray.size, weightArray.size)
        if (maxCount == 0) {
            return
        }
        val startX = bounds.width() * paddingWeight + bounds.left
        val endX = bounds.right - bounds.width() * paddingWeight
        val minRadius = min(bounds.width(), bounds.height()) * 0.6F
        val realHeight = bounds.height() / 2 * 3
        val colorEnd = backgroundColor and 0xFFFFFF

        var y = 0F
        var leftRadius = 0F
        var rightRadius = 0F

        for (index in 0 until maxCount) {
            val weight = weightArray[index]
            if (weight == 0) {
                continue
            }
            val radius = weight * 1F / totalWeight * realHeight

            val x: Float
            when (shaderArray.size % 3) {
                0 -> {
                    y += radius
                    leftRadius = radius
                    x = startX
                }
                1 -> {
                    rightRadius = radius
                    x = endX
                }
                else -> {
                    y += radius
                    x = leftRadius / (leftRadius + rightRadius)
                }
            }
            val radialRadius = max(radius * 2.5F, minRadius)
            shaderArray.add(
                RadialGradient(
                    x,
                    y,
                    radialRadius,
                    intArrayOf(
                        colorArray[index],
                        colorEnd,
                    ),
                    floatArrayOf(
                        0.2F,
                        0.8F
                    ),
                    Shader.TileMode.CLAMP
                )
            )
        }

    }

    fun draw(canvas: Canvas) {
        // 检查数据，必要时更新内容
        checkWallpaper()
        // 置空画布，绘制底色
        paint.shader = null
        // 绘制底色
        canvas.drawRect(bounds, paint)
        // 绘制每一个元素
        shaderArray.forEach {
            paint.shader = it
            canvas.drawRect(bounds, paint)
        }
    }

}