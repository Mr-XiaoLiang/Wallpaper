package com.lollipop.wallpaper.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.databinding.ActivityGenerateBinding
import com.lollipop.wallpaper.databinding.ItemSelectColorBinding
import com.lollipop.wallpaper.utils.bind
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 生成颜色的activity
 * @author Lollipop
 * @date 2021/06/30
 */
class GenerateActivity : BaseActivity() {

    private val binding: ActivityGenerateBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
    }

    private class ColorHolder(
        private val binding: ItemSelectColorBinding,
        private val onClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(parent: ViewGroup, onClick: (position: Int) -> Unit): ColorHolder {
                return ColorHolder(parent.bind(), onClick)
            }
        }

        private val colorDrawable = ColorDrawable()

        init {
            binding.colorPreviewView.setImageDrawable(colorDrawable)
            itemView.setOnClickListener {
                onClick(adapterPosition)
            }
        }

        fun bind(color: Int, isSelected: Boolean) {
            binding.colorPreviewView.isSelected = isSelected
            colorDrawable.color = color
        }

    }

}