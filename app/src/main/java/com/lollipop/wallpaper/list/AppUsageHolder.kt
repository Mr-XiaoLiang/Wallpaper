package com.lollipop.wallpaper.list

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ItemAppUsageBinding
import com.lollipop.wallpaper.entitys.AppInfo
import com.lollipop.wallpaper.utils.bind

/**
 * @author lollipop
 * @date 2021/6/5 15:17
 * APP使用时间的Bean
 */
class AppUsageHolder(private val viewBinding: ItemAppUsageBinding) :
    ViewBindingHolder(viewBinding) {

    companion object {
        fun create(parent: ViewGroup): AppUsageHolder {
            return AppUsageHolder(parent.bind(true))
        }

        private const val ONE_SECOND = 1000L
        private const val ONE_MINUTE = ONE_SECOND * 60
        private const val ONE_HOUR = ONE_MINUTE * 60
        private const val ONE_DAY = ONE_HOUR * 24

    }

    private val usageProgressDrawable = UsageProgressDrawable()

    init {
        itemView.background = usageProgressDrawable
        usageProgressDrawable.progressColor =
            ContextCompat.getColor(itemView.context, R.color.colorSecondaryVariant)
        usageProgressDrawable.alpha = 32
    }

    fun bind(info: AppInfo) {
        viewBinding.appIconView.setImageDrawable(info.icon)
        viewBinding.appLabelView.text = info.label
        viewBinding.appPkgView.text = info.packageName
        viewBinding.appUsageView.text = getUsageTime(info.usageTime)
        usageProgressDrawable.progress = info.usageTime * 1F / ONE_DAY
    }

    private fun getUsageTime(timeLength: Long): String {
        val builder = StringBuilder()
        val context = itemView.context
        (timeLength / ONE_HOUR).let { hour ->
            if (hour > 0) {
                builder.append(
                    context.getString(
                        R.string.unit_hour,
                        hour
                    )
                )
            }
        }
        (timeLength % ONE_HOUR / ONE_MINUTE).let { minute ->
            if (minute > 0 || builder.isNotEmpty()) {
                builder.append(
                    context.getString(
                        R.string.unit_minute,
                        minute
                    )
                )
            }
        }
        (timeLength % ONE_MINUTE / ONE_SECOND).let { second ->
            if (second > 0 || builder.isNotEmpty()) {
                builder.append(
                    context.getString(
                        R.string.unit_second,
                        second
                    )
                )
            }
        }
        builder.append(
            context.getString(
                R.string.unit_millisecond,
                timeLength % ONE_SECOND
            )
        )
        return builder.toString()
    }

    private class UsageProgressDrawable() : Drawable() {

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL_AND_STROKE
        }

        var progressColor: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        var progress = 0F
            set(value) {
                field = value
                updateBounds()
            }

        private val drawBounds = Rect()

        override fun draw(canvas: Canvas) {
            canvas.drawRect(drawBounds, paint)
        }

        override fun onBoundsChange(bounds: Rect?) {
            super.onBoundsChange(bounds)
            updateBounds()
        }

        private fun updateBounds() {
            val width = (bounds.width() * progress).toInt()
            drawBounds.set(0, 0, width, bounds.height())
            drawBounds.offset(bounds.left, bounds.top)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

}