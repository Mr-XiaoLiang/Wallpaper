package com.lollipop.wallpaper.generate

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.FragmentGenerateGroupPreferenceBinding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 色彩生成的分组偏好设置页面
 * 将会必要的说明以及分组数量的偏好设置
 * @author Lollipop
 * @date 2021/07/03
 */
class GenerateGroupPreferenceFragment : GenerateBaseFragment() {

    private val binding: FragmentGenerateGroupPreferenceBinding by lazyBind()

    private var callback: Callback? = null

    override val nextStepAction: Int
        get() = R.id.actionGroupPreferenceToGrouping

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

    }
}