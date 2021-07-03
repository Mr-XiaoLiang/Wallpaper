package com.lollipop.wallpaper.generate

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.wallpaper.databinding.FragmentGenerateCompleteBinding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 色彩生成的完成页面
 * @author Lollipop
 * @date 2021/07/03
 */
class GenerateCompleteFragment : GenerateBaseFragment() {

    private val binding: FragmentGenerateCompleteBinding by lazyBind()

    private var callback: Callback? = null

    override val nextStepAction: Int
        get() = 0

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