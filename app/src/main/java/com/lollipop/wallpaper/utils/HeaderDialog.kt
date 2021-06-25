package com.lollipop.wallpaper.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.lollipop.wallpaper.provider.BackPressProvider

/**
 * @author lollipop
 * @date 2021/6/24 21:17
 * 一个在顶部显示的dialog
 */
class HeaderDialog private constructor(
    private val rootGroup: ViewGroup,
    private val backPressProvider: BackPressProvider?,
    private val viewProvider: ViewProvider
) {

    companion object {
        private const val ANIMATION_MAX = 1F
        private const val ANIMATION_MIN = 0F
        private const val ANIMATION_DURATION = 300L

        fun with(activity: Activity): Builder {
            val backPressProvider = if (activity is BackPressProvider) {
                activity
            } else {
                null
            }
            return with(activity.window.decorView as ViewGroup, false, backPressProvider)
        }

        fun with(fragment: Fragment): Builder {
            val backPressProvider = if (fragment is BackPressProvider) {
                fragment
            } else {
                null
            }
            return with(findRootGroup(fragment.view!!), true, backPressProvider)
        }

        fun with(
            viewGroup: ViewGroup,
            isFindRoot: Boolean,
            backPressProvider: BackPressProvider?
        ): Builder {
            return if (isFindRoot) {
                Builder(findRootGroup(viewGroup), backPressProvider)
            } else {
                Builder(viewGroup, backPressProvider)
            }
        }

        fun with(view: View, backPressProvider: BackPressProvider?): Builder {
            return with(findRootGroup(view), false, backPressProvider)
        }

        private fun findRootGroup(view: View): ViewGroup {
            var target: View = view
            var viewGroup: ViewGroup? = null
            do {
                if (isGuideParent(target)) {
                    viewGroup = target as ViewGroup
                }
                val parent = target.parent
                if (parent is View) {
                    target = parent
                }
            } while (parent != null)
            if (viewGroup == null) {
                throw RuntimeException("Root view not found")
            }
            return viewGroup
        }

        private fun isGuideParent(view: View): Boolean {
            return (view is FrameLayout
                    || view is ConstraintLayout
                    || view is RelativeLayout
                    || view is CoordinatorLayout)
        }
    }

    private var dialogImpl: DialogView? = null

    private val dialogContentGroup by lazy {
        MaterialCardView(rootGroup.context).apply {
            cardElevation = 7.dp2px()
            radius = 10.dp2px()
        }
    }

    private val dialogRootView by lazy {
        ConstraintLayout(rootGroup.context).apply {
            val rootId = View.generateViewId()
            id = rootId
            val layoutParams = ConstraintLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.leftToLeft = rootId
            layoutParams.topToTop = rootId
            layoutParams.rightToRight = rootId
            layoutParams.horizontalWeight = 0.9F
            addView(dialogContentGroup, layoutParams)
        }
    }

    private var dialogView: DialogView? = null

    private fun checkView() {
        var content = dialogImpl
        if (content == null) {
            content = viewProvider.createDialogView(dialogRootView)
            dialogImpl = content
            content.onCreate()
        }
        val contentView = content.view
        contentView.parent.let {
            if (it == null || it != dialogContentGroup) {
                if (it is ViewManager) {
                    it.removeView(contentView)
                }
                dialogContentGroup.addView(
                    contentView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        val parent = dialogRootView.parent
        if (parent == null || parent != rootGroup) {
            if (parent is ViewManager) {
                parent.removeView(dialogRootView)
            }
            rootGroup.addView(
                dialogRootView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    fun show() {
        checkView()
        dialogRootView.visibleOrInvisible(false)
        dialogRootView.post {
            doAnimation(true)
        }
    }

    fun dismiss() {

    }

    private fun doAnimation(isOpen: Boolean) {
        // TODO
    }

    class Builder(
        private val rootGroup: ViewGroup,
        private val backPressProvider: BackPressProvider?
    ) {
        private var viewProvider: ViewProvider? = null

        fun content(provider: ViewProvider): Builder {
            viewProvider = provider
            return this
        }

        fun build(): HeaderDialog {
            val provider = viewProvider ?: throw RuntimeException("content not found")
            return HeaderDialog(rootGroup, backPressProvider, provider)
        }

    }

    fun interface ViewProvider {
        fun createDialogView(parent: ViewGroup): DialogView
    }

    abstract class DialogView {

        abstract val view: View

        private var dismissCallback: (() -> Unit)? = null

        open fun onCreate() {}

        open fun onStart() {}

        open fun onStop() {}

        open fun onDestroy() {
            view.parent?.let {
                if (it is ViewManager) {
                    it.removeView(view)
                }
            }
        }

        protected fun dismiss() {
            dismissCallback?.invoke()
        }

    }

}