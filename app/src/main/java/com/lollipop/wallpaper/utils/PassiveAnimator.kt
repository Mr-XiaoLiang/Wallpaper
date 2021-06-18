package com.lollipop.wallpaper.utils

/**
 * @author lollipop
 * @date 2021/6/18 22:18
 * 被动式的动画辅助器
 * 根本原理为设定开始及结束时间，在询问时，返回当前的进度值
 * 主要用于动态壁纸等主动刷新的场景
 */
class PassiveAnimator {

    private var startTime = 0L
    private var endTime = 0L

    val isEnd: Boolean
        get() {
            if (!isEffective) {
                return true
            }
            return now >= endTime
        }

    private val now: Long
        get() {
            return System.currentTimeMillis()
        }

    val isEffective: Boolean
        get() {
            return startTime > 0 && endTime > 0
        }

    val progress: Float
        get() {
            if (isEnd) {
                return 1F
            }
            return (now - startTime) * 1F / (endTime - startTime)
        }

    fun start(duration: Long) {
        this.startTime = now
        this.endTime = startTime + duration
    }

    fun cancel() {
        this.startTime = -1L
        this.endTime = -1L
    }

    fun end() {
        endTime = now
    }

}