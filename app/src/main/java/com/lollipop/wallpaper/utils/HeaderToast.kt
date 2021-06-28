package com.lollipop.wallpaper.utils

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.lollipop.wallpaper.R

/**
 * @author lollipop
 * @date 2021/6/28 23:19
 * 在屏幕顶部的Toast
 */
class HeaderToast private constructor(
    private val activity: Activity,
    private val value: String
) : HeaderDialog.DialogView() {

    companion object {
        private const val TOAST_DURATION = 2600L
        private const val PADDING_VERTICAL = 20
        private const val PADDING_HORIZONTAL = 20
        private const val TEXT_SIZE = 18F

        fun show(activity: Activity, value: String) {
            HeaderDialog.with(activity).content {
                HeaderToast(activity, value)
            }.build().apply {
                dismissWhenClickBackground = false
                backgroundColor = Color.TRANSPARENT
                cardBackgroundColor = ContextCompat.getColor(activity, R.color.floatingBackground)
            }.show()
        }

    }

    private val toastView by lazy {
        TextView(activity).apply {
            text = value
            val paddingVertical = PADDING_VERTICAL.dp2px().toInt()
            val paddingHorizontal = PADDING_HORIZONTAL.dp2px().toInt()
            setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
            textSize = TEXT_SIZE
            setTextColor(getColor(R.color.textColorPrimary))
        }
    }

    override val view: View
        get() = toastView

    private val autoRemoveTask = task {
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        autoRemoveTask.delay(TOAST_DURATION)
    }

    override fun onStop() {
        super.onStop()
        autoRemoveTask.cancel()
    }

}

fun Activity.showToast(value: String) {
    HeaderToast.show(this, value)
}

fun Activity.showToast(value: Int) {
    HeaderToast.show(this, this.getString(value))
}