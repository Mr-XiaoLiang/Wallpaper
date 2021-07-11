package com.lollipop.wallpaper.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.databinding.DialogColorBinding
import com.lollipop.wallpaper.databinding.ItemPresetColorBinding
import com.lollipop.wallpaper.list.ListTouchHelper
import com.lollipop.wallpaper.list.ViewBindingHolder
import com.lollipop.wallpaper.utils.*

/**
 * @author lollipop
 * @date 2021/7/11 15:27
 */
class PaletteDialogDelegator(private val binding: DialogColorBinding) {

    val view: View
        get() {
            return binding.root
        }

    private var onColorChangedListener: ((Int) -> Unit)? = null

    private var onColorConfirmListener: ((Int) -> Unit)? = null

    private var dialogSelectedColor: Int = Color.RED

    private val colorStore = ColorStore()

    private val context: Context
        get() {
            return binding.root.context
        }

    private val settings: LSettings by lazy {
        LSettings.bind(context)
    }

    val selectedColor: Int
        get() {
            return dialogSelectedColor
        }

    var isShowPreview: Boolean
        get() {
            return binding.previewGroup.isVisible
        }
        set(value) {
            binding.previewGroup.visibleOrGone(value)
        }

    private val saveInfoTask = task {
        doAsync {
            colorStore.saveTo(settings)
        }
    }

    init {
        binding.huePaletteView.onHueChange { hue, _ ->
            binding.satValPaletteView.onHueChange(hue.toFloat())
        }
        binding.satValPaletteView.onHSVChange { _, color, _ ->
            dialogSelectedColor = color
            updatePreview(color)
            onColorChangedListener?.invoke(color)
        }
        binding.presetColorView.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.presetColorView.adapter =
            PresetColorAdapter(colorStore, ::onPresetColorClick)

        binding.colorConfirmButton.setOnClickListener {
            val color = dialogSelectedColor
            colorStore.put(color) {
                binding.presetColorView.adapter?.notifyItemInserted(it)
            }
            onColorConfirmListener?.invoke(color)
        }

        ListTouchHelper.create()
            .setMoveDirection {
                start = true
                end = true
            }.setSwipeDirection {
                down = true
            }.autoMoveWithList(colorStore)
            .onSwiped(::onPresetColorSwiped)
            .bindTo(binding.presetColorView)

    }

    fun saveInfo() {
        saveInfoTask.cancel()
        saveInfoTask.delay(100L)
    }

    fun reload() {
        colorStore.reload(settings) {
            binding.presetColorView.adapter?.notifyDataSetChanged()
        }
    }

    private fun updatePreview(color: Int) {
        if (isShowPreview) {
            val drawable = binding.palettePreviewView.drawable
            if (drawable is ColorDrawable) {
                drawable.color = color
            } else {
                binding.palettePreviewView.setImageDrawable(
                    ColorDrawable(color)
                )
            }
        }
    }

    fun onColorChanged(callback: (Int) -> Unit) {
        this.onColorChangedListener = callback
    }

    fun onColorConfirm(callback: (Int) -> Unit) {
        this.onColorConfirmListener = callback
    }

    private fun onPresetColorSwiped(
        position: Int,
        direction: ListTouchHelper.Direction
    ) {
        if (direction.down) {
            colorStore.removeAt(position)
            binding.presetColorView.adapter?.notifyItemRemoved(position)
            saveInfo()
        }
    }

    fun updatePalette(color: Int) {
        dialogSelectedColor = color
        binding.huePaletteView.parser(color)
        binding.satValPaletteView.parser(color)
        updatePreview(color)
    }

    private fun onPresetColorClick(index: Int) {
        updatePalette(colorStore[index])
    }

    private class PresetColorAdapter(
        private val data: List<Int>,
        private val onClickCallback: (Int) -> Unit
    ) : RecyclerView.Adapter<PresetColorHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetColorHolder {
            return PresetColorHolder.create(parent, onClickCallback)
        }

        override fun onBindViewHolder(holder: PresetColorHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class PresetColorHolder private constructor(
        viewBinding: ItemPresetColorBinding,
        private val onClickCallback: (Int) -> Unit
    ) : ViewBindingHolder(viewBinding) {

        companion object {
            fun create(parent: ViewGroup, callback: (Int) -> Unit): PresetColorHolder {
                return PresetColorHolder(parent.bind(true), callback)
            }
        }

        private val colorDrawable = ColorDrawable()

        init {
            viewBinding.colorPreviewView.setImageDrawable(colorDrawable)
            viewBinding.colorPreviewView.setOnClickListener {
                onClickCallback(adapterPosition)
            }
        }

        fun bind(color: Int) {
            colorDrawable.color = color
        }

    }

}