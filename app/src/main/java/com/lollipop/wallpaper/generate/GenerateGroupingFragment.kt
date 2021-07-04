package com.lollipop.wallpaper.generate

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.FragmentGenerateGroupingBinding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 色彩生成的分组页面
 * 将会把生成的颜色进行合并及分组
 * @author Lollipop
 * @date 2021/07/03
 */
class GenerateGroupingFragment : GenerateBaseFragment() {

    private val binding: FragmentGenerateGroupingBinding by lazyBind()

    private var callback: Callback? = null

    override val nextStepAction: Int
        get() = R.id.actionGroupingToComplete

    override val contentViewBinding: ViewBinding
        get() = binding

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

    }

}