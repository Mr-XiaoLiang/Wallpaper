package com.lollipop.wallpaper

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

/**
 * @author lollipop
 * @date 2021/6/3 22:18
 */
class LWallpaperService : WallpaperService() {

    companion object {
        /**
         * 单次绘制时的帧数
         * 本壁纸本质上是静态的，所以绘制的次数主要是为了避免Surface多画布造成的黑屏问题
         */
        private const val DRAW_COUNT = 4
    }

    private var engine: WallpaperEngine? = null

    override fun onCreateEngine(): Engine {
        return WallpaperEngine(
            ::getWallpaperColors,
            ::getWallpaperWeight,
            ::getWallpaperBackground,
            ::getWallpaperPadding
        )
    }

    private fun getWallpaperColors(): IntArray {
        TODO()
    }

    private fun getWallpaperWeight(): IntArray {
        TODO()
    }

    private fun getWallpaperBackground(): Int {
        return if (isNightModeActive()) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }

    private fun getWallpaperPadding(): Float {
        TODO()
    }

    private fun isNightModeActive(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return resources.configuration.isNightModeActive
        }
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                true
            }
            else -> {
                false
            }
        }
    }

    private class WallpaperEngine(
        private val colorProvider: () -> IntArray,
        private val weightProvider: () -> IntArray,
        private val backgroundProvider: () -> Int,
        private val paddingProvider: () -> Float
    ) : WallpaperService.Engine() {

        private val wallpaperPainter = WallpaperPainter()

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            wallpaperPainter.changeBounds(0, 0, width, height)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                callDraw()
            }
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder?) {
            super.onSurfaceRedrawNeeded(holder)
            callDraw()
        }

        private fun callDraw() {
            wallpaperPainter.changeColors(colorProvider())
            wallpaperPainter.changeWeights(weightProvider())
            wallpaperPainter.backgroundColor = backgroundProvider()
            wallpaperPainter.paddingWeight = paddingProvider()
            doAsync {
                for (index in 0 until DRAW_COUNT) {
                    val surface = surfaceHolder ?: break
                    val canvas = surface.lockCanvas()
                    wallpaperPainter.draw(canvas)
                    surface.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

}