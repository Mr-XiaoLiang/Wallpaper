package com.lollipop.wallpaper.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.lollipop.wallpaper.listener.BackPressListener
import com.lollipop.wallpaper.provider.BackPressProvider
import com.lollipop.wallpaper.utils.*
import java.util.*

/**
 * @author lollipop
 * @date 2021/6/24 21:17
 * 一个在顶部显示的dialog
 */
class HeaderDialog private constructor(
    private val rootGroup: ViewGroup,
    private val backPressProvider: BackPressProvider?,
    private val viewProvider: ViewProvider
) : BackPressListener {

    companion object {
        private const val ANIMATION_DURATION = 300L

        private const val DIALOG_TOP_SPACE = 24

        private val BACKGROUND_COLOR = Color.BLACK.changeAlpha(0.5F)

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
            return with(findRootGroup(fragment.requireView()), true, backPressProvider)
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

        fun findDialog(rootGroup: View, onFound: (DialogRootGroup) -> Boolean) {
            val viewList = LinkedList<View>()
            viewList.add(rootGroup)
            while (viewList.isNotEmpty()) {
                val view = viewList.removeFirst()
                if (view is DialogRootGroup && onFound(view)) {
                    break
                }
                if (view is ViewGroup) {
                    for (i in 0 until view.childCount) {
                        viewList.addLast(view.getChildAt(i))
                    }
                }
            }
        }
    }

    private var dialogImpl: DialogView? = null

    var dismissWhenClickBackground = true

    var dismissWhenBackPress = true

    var backgroundColor = BACKGROUND_COLOR

    var topSpace = DIALOG_TOP_SPACE

    var cardBackgroundColor = Color.WHITE

    private val dialogContentGroup by lazy {
        MaterialCardView(rootGroup.context).apply {
            cardElevation = 7.dp2px()
            radius = 10.dp2px()
            setOnClickListener {
                // do nothing
            }
        }
    }

    private val dialogRootView by lazy {
        DialogRootGroup(this, rootGroup.context).apply {
            clipToPadding = false
            if (dismissWhenClickBackground) {
                setOnClickListener {
                    dismiss()
                }
            }
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {}

                override fun onViewDetachedFromWindow(v: View?) {
                    animatorHelper.destroy()
                }
            })
            val rootId = View.generateViewId()
            id = rootId

            val layoutParams = ConstraintLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.leftToLeft = rootId
            layoutParams.topToTop = rootId
            layoutParams.rightToRight = rootId
            layoutParams.matchConstraintPercentWidth = 0.9F
            layoutParams.topMargin = topSpace.dp2px().toInt()
            addView(dialogContentGroup, layoutParams)
        }
    }

    private val animatorHelper by lazy {
        AnimationHelper(ANIMATION_DURATION, ::onAnimationUpdate).apply {
            onStart(::onAnimationStart)
            onEnd(::onAnimationEnd)
        }
    }

    private val viewCallback = {
        dismiss()
    }

    private fun checkView() {
        var content = dialogImpl
        if (content == null) {
            content = viewProvider.createDialogView(dialogRootView)
            dialogImpl = content
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
            content.onCreate()
            rootGroup.addView(
                dialogRootView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialogRootView.fixInsetsByPadding(
                edge = WindowInsetsHelper.Edge.ALL
            )
            dialogRootView.tryInvisible()
        }
    }

    private fun removeDialogView() {
        dialogImpl?.onDestroy()
        val parent = dialogRootView.parent
        if (parent is ViewManager) {
            parent.removeView(dialogRootView)
        }
    }

    fun optDialogView(): DialogView? {
        return dialogImpl
    }

    fun show() {
        backPressProvider?.addBackPressListener(this)
        checkView()
        dialogRootView.post {
            dialogImpl?.setHost(this)
            dialogImpl?.setDismissCallback(viewCallback)
            dialogImpl?.onStart()
            doAnimation(true)
        }
    }

    fun dismiss() {
        backPressProvider?.removeBackPressListener(this)
        dialogImpl?.onStop()
        dialogImpl?.setDismissCallback(null)
        dialogImpl?.setHost(null)
        doAnimation(false)
    }

    private fun doAnimation(isOpen: Boolean) {
        if (isOpen) {
            animatorHelper.open()
        } else {
            animatorHelper.close()
        }
    }

    private fun onAnimationUpdate(progress: Float) {
        dialogRootView.setBackgroundColor(backgroundColor.changeAlpha(progress))
        dialogContentGroup.translationY = dialogContentGroup.bottom * (progress - 1)
    }

    private fun onAnimationStart(progress: Float) {
        onAnimationUpdate(progress)
        dialogRootView.tryVisible()
    }

    private fun onAnimationEnd(progress: Float) {
        if (animatorHelper.progressIs(AnimationHelper.PROGRESS_MIN)) {
            removeDialogView()
        }
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

        val dialog: HeaderDialog?
            get() {
                return host
            }

        private var dismissCallback: (() -> Unit)? = null

        private var host: HeaderDialog? = null

        fun setHost(dialog: HeaderDialog?) {
            host = dialog
        }

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

        fun setDismissCallback(callback: (() -> Unit)?) {
            dismissCallback = callback
        }

        fun dismiss() {
            dismissCallback?.invoke()
        }

    }

    override fun onBackPressed(): Boolean {
        if (dismissWhenBackPress && dialogRootView.isShown) {
            dismiss()
            return true
        }
        return false
    }

    class DialogRootGroup(val dialog: HeaderDialog, context: Context) : ConstraintLayout(context)

}