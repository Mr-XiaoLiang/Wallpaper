package com.lollipop.wallpaper.service

import android.app.WallpaperColors
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.lollipop.wallpaper.engine.UsageStatsGroupInfo
import com.lollipop.wallpaper.engine.WallpaperPainter
import com.lollipop.wallpaper.utils.*
import kotlin.math.min

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

        /**
         * 分组信息变化时的广播内容
         */
        private const val ACTION_GROUP_INFO_CHANGED = "com.lollipop.ACTION_GROUP_INFO_CHANGED"

        private const val ONE_MINUTE = 60 * 1000L

        private const val ANIMATION_DURATION = 500L

        /**
         * 通知组信息被更新了
         */
        fun notifyGroupInfoChanged(context: Context) {
            context.sendBroadcast(Intent(ACTION_GROUP_INFO_CHANGED))
        }
    }

    private var engine: WallpaperEngine? = null

    private val settings = LSettings.bind(this)

    private val groupInfoList = ArrayList<UsageStatsGroupInfo>()

    private var groupColorArray: IntArray = IntArray(0)

    private val packageUsageHelper = PackageUsageHelper(this)

    private var weightArray: IntArray = IntArray(0)

    private var packageChangeReceiver: BroadcastReceiver? = null

    private var updateDelay = 10 * ONE_MINUTE

    private var animationEnable = true

    private val updateWeightTask = task {
        callUpdateWeights()
    }

    private val groupInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            callGroupInfoChange()
        }
    }

    override fun onCreate() {
        super.onCreate()
        callGroupInfoChange()
        registerReceiver(groupInfoReceiver, IntentFilter(ACTION_GROUP_INFO_CHANGED))
        packageChangeReceiver = PackageUsageHelper.registerPackageChangeReceiver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        engine = null
        unregisterReceiver(groupInfoReceiver)
        packageChangeReceiver?.let {
            unregisterReceiver(it)
        }
        packageChangeReceiver = null
    }

    override fun onCreateEngine(): Engine {
        val newEngine = WallpaperEngine(
            ::getWallpaperColors,
            ::getWallpaperWeight,
            ::getWallpaperBackground,
            ::getWallpaperPadding,
            ::animationEnable
        )
        engine = newEngine
        return newEngine
    }

    private fun getWallpaperColors(): IntArray {
        return groupColorArray
    }

    private fun getWallpaperWeight(): IntArray {
        if (weightArray.isEmpty()) {
            return IntArray(groupColorArray.size) { 1 }
        }
        return weightArray
    }

    private fun getWallpaperBackground(): Int {
        return if (isNightModeActive()) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }

    private fun getWallpaperPadding(): Float {
        return settings.padding
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

    private fun callUpdateWeights() {
        updateWeightTask.cancel()
        doAsync {
            packageUsageHelper.loadUsageData()
            if (packageUsageHelper.isEmpty) {
                weightArray = IntArray(0)
            } else {
                val list = ArrayList<Long>()
                var maxValue = 0L
                groupInfoList.forEach { group ->
                    val time = packageUsageHelper[group.key]
                    if (time > maxValue) {
                        maxValue = time
                    }
                    list.add(time)
                }
                if (maxValue > Int.MAX_VALUE) {
                    val multiple = ((maxValue * 1.0 / Int.MAX_VALUE) + 0.9).toInt()
                    val weightMax = Int.MAX_VALUE.toLong()
                    for (index in list.indices) {
                        list[index] = min(list[index] / multiple, weightMax)
                    }
                }
                weightArray = IntArray(list.size) { index ->
                    list[index].toInt()
                }
            }
            callUpdateCanvas()
            updateWeightTask.delay(updateDelay)
        }
    }

    private fun callUpdateCanvas() {
        onUI {
            engine?.callDraw()
        }
    }

    private fun callGroupInfoChange() {
        updateDelay = settings.updateDelay * ONE_MINUTE
        animationEnable = settings.animationEnable

        groupInfoList.clear()
        groupInfoList.addAll(settings.getGroupInfo())
        groupColorArray = IntArray(groupInfoList.size) { groupInfoList[it].color }

        packageUsageHelper.updateGroupMap(settings.getPackageInfo())

        callUpdateWeights()
    }

    private inner class WallpaperEngine(
        private val colorProvider: () -> IntArray,
        private val weightProvider: () -> IntArray,
        private val backgroundProvider: () -> Int,
        private val paddingProvider: () -> Float,
        private val enableAnimation: () -> Boolean
    ) : WallpaperService.Engine() {

        private val wallpaperPainter = WallpaperPainter()
        private val wallpaperColorsEngine by lazy {
            WallpaperColorsEngine(
                colorProvider,
                weightProvider,
                backgroundProvider,
                paddingProvider
            )
        }

        private var cacheBitmap: Bitmap? = null
        private val animator by lazy {
            PassiveAnimator()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            wallpaperPainter.changeBounds(0, 0, width, height)
            cacheBitmap?.let { bitmap ->
                if (bitmap.width != width || bitmap.height != height) {
                    bitmap.recycle()
                    cacheBitmap = null
                }
            }
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

        override fun onComputeColors(): WallpaperColors? {
            return wallpaperColorsEngine.getWallpaperColors()
        }

        fun callDraw() {
            if (enableAnimation()) {
                drawByAnimation()
            } else {
                drawByStatic()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                notifyColorsChanged()
            }
        }

        private fun drawByStatic() {
            updatePainter()
            doAsync {
                for (index in 0 until DRAW_COUNT) {
                    val status = doDraw {
                        wallpaperPainter.draw(it)
                    }
                    if (!status) {
                        break
                    }
                }
            }
        }

        private fun drawByAnimation() {
            val bitmap = getCacheBitmap()
            val cacheCanvas = Canvas(bitmap)
            // 缓存当前界面来做动画过渡
            wallpaperPainter.draw(cacheCanvas)

            // 缓存后再更新画笔，否则得到的不是老得图案
            updatePainter()

            animator.start(ANIMATION_DURATION)
            // 一直绘制，直到动画结束
            while (!animator.isEnd) {
                val status = doDraw {
                    // 绘制原本的图案
                    it.drawBitmap(bitmap, 0F, 0F, null)
                    // 设置透明度，用于过渡效果的渐变
                    wallpaperPainter.setAlpha((animator.progress * 255).toInt())
                    // 绘制新的图案
                    wallpaperPainter.draw(it)
                }
                if (!status) {
                    break
                }
            }
        }

        private fun doDraw(callback: (Canvas) -> Unit): Boolean {
            val surface = surfaceHolder ?: return false
            val canvas = surface.lockCanvas()
            callback(canvas)
            surface.unlockCanvasAndPost(canvas)
            return true
        }

        private fun updatePainter() {
            wallpaperPainter.changeColors(colorProvider())
            wallpaperPainter.changeWeights(weightProvider())
            wallpaperPainter.backgroundColor = backgroundProvider()
            wallpaperPainter.paddingWeight = paddingProvider()
        }

        private fun getCacheBitmap(): Bitmap {
            val bitmap = cacheBitmap
            if (bitmap != null
                && !bitmap.isRecycled
                && bitmap.width == wallpaperPainter.width
                && bitmap.height == wallpaperPainter.height) {
                return bitmap
            }
            val newBitmap = Bitmap.createBitmap(
                wallpaperPainter.width,
                wallpaperPainter.height,
                Bitmap.Config.ARGB_8888
            )
            cacheBitmap = newBitmap
            return newBitmap
        }

        override fun onDestroy() {
            super.onDestroy()
            cacheBitmap?.recycle()
            cacheBitmap = null
        }

    }

    private class WallpaperColorsEngine(
        private val colorProvider: () -> IntArray,
        private val weightProvider: () -> IntArray,
        private val backgroundProvider: () -> Int,
        private val paddingProvider: () -> Float
    ) {

        companion object {
            private const val CANVAS_SIZE = 112
        }

        private var wallpaperPainter: WallpaperPainter? = null

        private fun getPainter(): WallpaperPainter {
            val painter = wallpaperPainter
            if (painter == null) {
                val newPainter = WallpaperPainter()
                newPainter.changeBounds(0, 0, CANVAS_SIZE, CANVAS_SIZE)
                wallpaperPainter = newPainter
                return newPainter
            }
            return painter
        }

        fun getWallpaperColors(): WallpaperColors? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                return null
            }
            val painter = getPainter()
            painter.changeColors(colorProvider())
            painter.changeWeights(weightProvider())
            painter.backgroundColor = backgroundProvider()
            painter.paddingWeight = paddingProvider()

            val bitmap = Bitmap.createBitmap(
                CANVAS_SIZE, CANVAS_SIZE,
                Bitmap.Config.ARGB_8888
            )
            val bmpCanvas = Canvas(bitmap)
            painter.draw(bmpCanvas)
            val colors = WallpaperColors.fromBitmap(bitmap)
            bitmap.recycle()
            return colors
        }

    }

    private class CacheBitmapHelper {
        private var oldBitmap: Bitmap? = null
        private var newBitmap: Bitmap? = null

        fun checkBitmapSize(width: Int, height: Int) {

        }

        fun destroy() {

        }

    }

}