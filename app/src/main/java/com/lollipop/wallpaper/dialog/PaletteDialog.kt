package com.lollipop.wallpaper.dialog

import android.app.Activity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.utils.bind

/**
 * @author lollipop
 * @date 2021/7/11 16:05
 */
class PaletteDialog(activity: Activity) : HeaderDialog.DialogView() {

    companion object {
        fun create(activity: Activity, color: Int, callback: (Int) -> Unit) {
            HeaderDialog.with(activity).content {
                PaletteDialog(activity).also { nameDialog ->
                    nameDialog.color = color
                    nameDialog.onConfirmListener = callback
                }
            }.build().apply {
                cardBackgroundColor = ContextCompat.getColor(activity, R.color.floatingBackground)
                show()
            }
        }

        fun create(fragment: Fragment, color: Int, callback: (Int) -> Unit) {
            val activity = fragment.activity ?: return
            create(activity, color, callback)
        }
    }

    override val view: View
        get() = delegator.view

    private val delegator by lazy {
        PaletteDialogDelegator(activity.bind())
    }

    private var color: Int
        get() {
            return delegator.selectedColor
        }
        set(value) {
            delegator.updatePalette(value)
        }

    private var onConfirmListener: ((Int) -> Unit)? = null

    init {
        delegator.isShowPreview = true
        delegator.onColorConfirm {
            onConfirmListener?.invoke(it)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        delegator.reload()
    }

    override fun onStop() {
        super.onStop()
        delegator.saveInfo()
    }

}