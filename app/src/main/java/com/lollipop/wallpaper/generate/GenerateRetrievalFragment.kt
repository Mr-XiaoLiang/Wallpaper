package com.lollipop.wallpaper.generate

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.FragmentGenerateRetrievalBinding
import com.lollipop.wallpaper.databinding.ItemAppColorBinding
import com.lollipop.wallpaper.databinding.ItemSelectColorBinding
import com.lollipop.wallpaper.dialog.HeaderMessageDialog
import com.lollipop.wallpaper.entitys.AppColorInfo
import com.lollipop.wallpaper.list.ViewBindingHolder
import com.lollipop.wallpaper.utils.*

/**
 * 色彩生成的APP信息检索页面
 * 它将会负责检索并且显示APP生成的颜色信息
 * 并且选择APP对应的代表色
 * @author Lollipop
 * @date 2021/07/03
 */
class GenerateRetrievalFragment : GenerateBaseFragment() {

    private val binding: FragmentGenerateRetrievalBinding by lazyBind()

    private var callback: Callback? = null

    override val nextStepAction: Int
        get() = R.id.actionRetrievalToGroupPreference

    override val contentViewBinding: ViewBinding
        get() = binding

    private val appListData = ArrayList<AppColorInfo>()

    private val adapter =
        AppColorListAdapter(appListData, ::onColorSelected, ::getSelectedColorIndex)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appList.layoutManager = LinearLayoutManager(context)
        binding.appList.adapter = adapter
        binding.appList.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
    }

    private fun onColorSelected(position: Int, colorIndex: Int) {
        callback?.onSelectedColorChange(position, colorIndex)
    }

    private fun getSelectedColorIndex(position: Int): Int {
        return callback?.getSelectedColorIndex(position) ?: 0
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun loadData() {
        startLoading()
        appListData.clear()
        adapter.notifyDataSetChanged()
        callback?.getAppList { appList ->
            appListData.clear()
            appListData.addAll(appList)
            adapter.notifyDataSetChanged()
            endLoading()
            onLoadedData()
        }
    }

    private fun onLoadedData() {
        HeaderMessageDialog.create(this)
            .setMessage(getString(R.string.msg_retrieval_guide))
            .setCancelMessage(getString(R.string.confirm)) {
                it.dismiss()
            }.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        checkIdentity<Callback>(context) {
            callback = it
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    interface Callback {

        fun getAppList(callback: (List<AppColorInfo>) -> Unit)

        fun onSelectedColorChange(position: Int, colorIndex: Int)

        fun getSelectedColorIndex(position: Int): Int

    }

    private class AppColorListAdapter(
        private val data: List<AppColorInfo>,
        private val onColorSelected: (position: Int, colorIndex: Int) -> Unit,
        private val getSelectedColorIndex: (position: Int) -> Int
    ) : RecyclerView.Adapter<AppColorHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppColorHolder {
            return AppColorHolder.create(parent, onColorSelected)
        }

        override fun onBindViewHolder(holder: AppColorHolder, position: Int) {
            holder.bind(data[position], getSelectedColorIndex(position))
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class AppColorHolder(
        private val binding: ItemAppColorBinding,
        private val onColorSelected: (position: Int, colorIndex: Int) -> Unit
    ) : ViewBindingHolder(binding) {

        companion object {
            fun create(
                parent: ViewGroup,
                onColorSelected: (position: Int, colorIndex: Int) -> Unit
            ): AppColorHolder {
                return AppColorHolder(parent.bind(), onColorSelected).apply {
                    itemView.layoutParams {
                        it.width = ViewGroup.LayoutParams.MATCH_PARENT
                        it.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
            }
        }

        private var selectedColorIndex = 0

        private val colorList = ArrayList<Int>()

        private val adapter = ColorListAdapter(colorList, ::getSelectedColorIndex, ::onColorClick)

        init {
            binding.appColorGroup.layoutManager = LinearLayoutManager(
                context, RecyclerView.HORIZONTAL, false
            )
            binding.appColorGroup.adapter = adapter
        }

        fun bind(info: AppColorInfo, selectedIndex: Int) {
            binding.appIconView.setImageDrawable(info.appInfo.icon)
            binding.appLabelView.text = info.appInfo.label
            selectedColorIndex = selectedIndex
            colorList.clear()
            info.colorArray.forEach {
                colorList.add(it)
            }
            adapter.notifyDataSetChanged()
        }

        private fun onColorClick(colorIndex: Int) {
            val lastPosition = selectedColorIndex
            selectedColorIndex = colorIndex
            onColorSelected(adapterPosition, colorIndex)
            adapter.notifyItemChanged(lastPosition)
            adapter.notifyItemChanged(colorIndex)
        }

        private fun getSelectedColorIndex(): Int {
            return selectedColorIndex
        }

    }

    private class ColorListAdapter(
        private val colorList: List<Int>,
        private val colorSelected: () -> Int,
        private val onClick: (position: Int) -> Unit
    ) : RecyclerView.Adapter<ColorHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorHolder {
            return ColorHolder.create(parent, onClick)
        }

        override fun onBindViewHolder(holder: ColorHolder, position: Int) {
            holder.bind(colorList[position], colorSelected() == position)
        }

        override fun getItemCount(): Int {
            return colorList.size
        }

    }

    private class ColorHolder(
        private val binding: ItemSelectColorBinding,
        private val onClick: (position: Int) -> Unit
    ) : ViewBindingHolder(binding) {

        companion object {
            fun create(parent: ViewGroup, onClick: (position: Int) -> Unit): ColorHolder {
                return ColorHolder(parent.bind(), onClick).apply {
                    itemView.layoutParams {
                        it.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        it.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
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
            binding.colorSelectView.isSelected = isSelected
            colorDrawable.color = color
        }

    }

}