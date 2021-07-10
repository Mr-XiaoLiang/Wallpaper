package com.lollipop.wallpaper.dialog

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.utils.dp2px

/**
 * @author lollipop
 * @date 2021/7/10 14:06
 */
class GridSelectedDialog private constructor(private val option: Option) :
    HeaderDialog.DialogView() {

    companion object {
        fun create(fragment: Fragment): Builder {
            return Builder(fragment.activity)
        }

        fun create(activity: Activity): Builder {
            return Builder(activity)
        }
    }

    override val view: View
        get() = recyclerView

    private val recyclerView by lazy {
        RecyclerView(option.activity).apply {
            val padding = 12F.dp2px().toInt()
            setPaddingRelative(0, padding, 0, padding)
            clipToPadding = false
            adapter = SimpleTextAdapter(option.dataArray, ::onItemClick)
            layoutManager =
                GridLayoutManager(context, option.gridCount, RecyclerView.VERTICAL, false)
            adapter?.notifyDataSetChanged()
        }
    }

    private fun onItemClick(position: Int, value: String) {
        option.onSelectedListener(this, position, value)
    }

    private class SimpleTextAdapter(
        private val data: Array<String>,
        private val onClickCallback: (Int, String) -> Unit
    ) : RecyclerView.Adapter<SimpleTextHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleTextHolder {
            return SimpleTextHolder.create(parent, ::onItemClick)
        }

        override fun onBindViewHolder(holder: SimpleTextHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        private fun onItemClick(position: Int) {
            onClickCallback(position, data[position])
        }

    }

    private class SimpleTextHolder private constructor(
        private val textView: TextView,
        private val onClickCallback: (Int) -> Unit
    ) : RecyclerView.ViewHolder(textView) {

        companion object {
            fun create(parent: ViewGroup, onClick: (Int) -> Unit): SimpleTextHolder {
                return SimpleTextHolder(
                    createView(parent),
                    onClick
                )
            }

            private fun createView(parent: ViewGroup): TextView {
                return TextView(parent.context).apply {
                    textSize = 16F
                    gravity = Gravity.CENTER
                    val padding = 12F.dp2px().toInt()
                    setPaddingRelative(0, padding, 0, padding)
                }
            }

        }

        init {
            itemView.setOnClickListener {
                onClickCallback(adapterPosition)
            }
        }

        fun bind(value: String) {
            textView.text = value
        }

    }

    private class Option(
        val activity: Activity,
        val gridCount: Int,
        val dataArray: Array<String>,
        val onSelectedListener: ((GridSelectedDialog, Int, String) -> Unit)
    )

    class Builder(val activity: Activity?) {
        private var gridCount = 3
        private val dataArray = ArrayList<String>()
        private var onSelectedListener: (GridSelectedDialog, Int, String) -> Unit =
            { dialog, _, _ -> dialog.dismiss() }

        fun setGridCount(count: Int): Builder {
            gridCount = count
            return this
        }

        fun setData(data: Array<String>): Builder {
            this.dataArray.clear()
            this.dataArray.addAll(data)
            return this
        }

        fun onSelected(listener: (GridSelectedDialog, Int, String) -> Unit): Builder {
            this.onSelectedListener = listener
            return this
        }

        fun show() {
            activity ?: return
            HeaderDialog.with(activity).content {
                GridSelectedDialog(
                    Option(
                        activity,
                        gridCount,
                        dataArray.toTypedArray(),
                        onSelectedListener
                    )
                )
            }.build().apply {
                cardBackgroundColor = ContextCompat.getColor(activity, R.color.floatingBackground)
                show()
            }
        }

    }

}