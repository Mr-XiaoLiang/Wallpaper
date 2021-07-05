package com.lollipop.wallpaper.dialog

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.utils.dp2px
import com.lollipop.wallpaper.utils.getColor
import com.lollipop.wallpaper.utils.visibleOrGone

/**
 * @author lollipop
 * @date 2021/7/5 22:28
 */
class HeaderMessageDialog private constructor(
    private val activity: Activity
) : HeaderDialog.DialogView() {

    companion object {
        private const val PADDING_VERTICAL = 20
        private const val PADDING_HORIZONTAL = 20
        private const val TEXT_SIZE = 18F

        fun create(fragment: Fragment): Builder {
            return Builder(fragment.activity)
        }

        fun create(activity: Activity): Builder {
            return Builder(activity)
        }
    }

    private var onEnterClick: ((HeaderDialog) -> Unit)? = null
    private var onCancelClick: ((HeaderDialog) -> Unit)? = null

    override val view: View
        get() = contentView

    private var message: CharSequence
        get() {
            return messageView.text
        }
        set(value) {
            messageView.text = value
        }

    private var enterName: CharSequence
        get() {
            return enterButton.text
        }
        set(value) {
            enterButton.text = value
            enterButton.visibleOrGone(value.trim().isNotEmpty())
        }

    private var cancelName: CharSequence
        get() {
            return cancelButton.text
        }
        set(value) {
            cancelButton.text = value
            cancelButton.visibleOrGone(value.trim().isNotEmpty())
        }

    private val messageView by lazy {
        TextView(activity).apply {
            val paddingVertical = PADDING_VERTICAL.dp2px().toInt()
            val paddingHorizontal = PADDING_HORIZONTAL.dp2px().toInt()
            setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
            textSize = TEXT_SIZE
            setTextColor(getColor(R.color.textColorPrimary))
        }
    }

    private val enterButton by lazy {
        TextView(activity).apply {
            val paddingVertical = PADDING_VERTICAL.dp2px().toInt()
            val paddingHorizontal = PADDING_HORIZONTAL.dp2px().toInt()
            setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
            textSize = TEXT_SIZE
            setTextColor(getColor(R.color.colorPrimaryVariant))
            visibility = View.GONE
            gravity = Gravity.CENTER
            setOnClickListener {
                dialog?.let {
                    onEnterClick?.invoke(it)
                }
            }
        }
    }

    private val cancelButton by lazy {
        TextView(activity).apply {
            val paddingVertical = PADDING_VERTICAL.dp2px().toInt()
            val paddingHorizontal = PADDING_HORIZONTAL.dp2px().toInt()
            setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
            textSize = TEXT_SIZE
            setTextColor(getColor(R.color.textColorSecondary))
            visibility = View.GONE
            gravity = Gravity.CENTER
            setOnClickListener {
                dialog?.let {
                    onCancelClick?.invoke(it)
                }
            }
        }
    }

    private val contentView by lazy {
        LinearLayout(activity).also { content ->
            content.orientation = LinearLayout.VERTICAL
            content.addView(
                messageView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            content.addView(
                LinearLayout(activity).also { buttonLayout ->
                    buttonLayout.orientation = LinearLayout.HORIZONTAL
                    buttonLayout.addView(
                        enterButton,
                        LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).also {
                            it.weight = 1F
                        }
                    )
                    buttonLayout.addView(
                        cancelButton,
                        LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).also {
                            it.weight = 1F
                        }
                    )
                },
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    class Builder(private val activity: Activity?) {
        private var message: CharSequence = ""
        private var enter: CharSequence = ""
        private var cancel: CharSequence = ""
        private var onEnterClick: ((HeaderDialog) -> Unit)? = null
        private var onCancelClick: ((HeaderDialog) -> Unit)? = null

        fun setMessage(msg: CharSequence): Builder {
            this.message = msg
            return this
        }

        fun setEnterButton(name: CharSequence, listener: (HeaderDialog) -> Unit): Builder {
            this.enter = name
            this.onEnterClick = listener
            return this
        }

        fun setCancelMessage(name: CharSequence, listener: (HeaderDialog) -> Unit): Builder {
            this.cancel = name
            this.onCancelClick = listener
            return this
        }

        fun show() {
            activity ?: return
            val builder = this
            HeaderDialog.with(activity).content {
                HeaderMessageDialog(activity).also { messageDialog ->
                    messageDialog.message = builder.message
                    messageDialog.enterName = builder.enter
                    messageDialog.onEnterClick = builder.onEnterClick
                    messageDialog.cancelName = builder.cancel
                    messageDialog.onCancelClick = builder.onCancelClick
                }
            }.build().apply {
                cardBackgroundColor = ContextCompat.getColor(activity, R.color.floatingBackground)
                show()
            }
        }
    }

}