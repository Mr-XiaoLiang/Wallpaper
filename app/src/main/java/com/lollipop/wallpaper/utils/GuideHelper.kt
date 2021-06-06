package com.lollipop.wallpaper.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * @author lollipop
 * @date 2021/6/6 16:43
 * 引导工具的View
 */
object GuideHelper {

    fun attachTo(view: View): Builder {
        return Builder(view)
    }

    private fun showGuide(root: ViewGroup, guideLayout: Int, callback: () -> Unit) {
        val guideView = LayoutInflater.from(root.context).inflate(guideLayout, root, false)
        guideView.post { callback() }
        guideView.setOnClickListener {
            root.removeView(guideView)
        }
        guideView.applyWindowInsetsByPadding()
        root.addView(
            guideView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    class Builder(private val anchor: View) {

        private var layoutId: Int = 0

        private var shownCallback: (() -> Unit)? = null

        fun guideView(layoutId: Int): Builder {
            this.layoutId = layoutId
            return this
        }

        fun onShown(callback: () -> Unit): Builder {
            shownCallback = callback
            return this
        }

        fun show(isShow: Boolean = true) {
            if (!isShow) {
                return
            }
            if (layoutId == 0) {
                return
            }
            val viewGroup = findRoot(anchor) ?: return
            showGuide(viewGroup, layoutId, shownCallback ?: {})
        }

        private fun findRoot(view: View): ViewGroup? {
            var target: View = view
            var group: ViewGroup? = null
            do {
                if (isGroup(target)) {
                    group = target as ViewGroup
                }
                var parent = target.parent
                if (parent is View) {
                    target = parent
                } else {
                    parent = null
                }
            } while (parent != null)
            return group
        }

        private fun isGroup(target: View): Boolean {
            return (target is FrameLayout
                    || target is RelativeLayout
                    || target is ConstraintLayout
                    || target is CoordinatorLayout)
        }

    }

}