package com.lollipop.wallpaper.generate

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.FragmentGenerateRetrievalBinding
import com.lollipop.wallpaper.databinding.ItemSelectColorBinding
import com.lollipop.wallpaper.entitys.AppColorInfo
import com.lollipop.wallpaper.utils.bind
import com.lollipop.wallpaper.utils.lazyBind

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
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