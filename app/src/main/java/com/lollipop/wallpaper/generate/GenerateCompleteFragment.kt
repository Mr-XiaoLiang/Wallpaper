package com.lollipop.wallpaper.generate

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.lollipop.wallpaper.databinding.FragmentGenerateCompleteBinding
import com.lollipop.wallpaper.utils.WindowInsetsHelper
import com.lollipop.wallpaper.utils.fixInsetsByPadding
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

    override val contentViewBinding: ViewBinding
        get() = binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
        binding.nextBtn.setOnClickListener {
            callComplete()
        }
    }

    private fun callComplete() {
        startLoading()
        callback?.callComplete {
            endLoading()
            nextStep()
        }
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

        fun callComplete(callback: () -> Unit)

    }

}