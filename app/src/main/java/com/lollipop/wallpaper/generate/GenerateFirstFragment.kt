package com.lollipop.wallpaper.generate

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.FragmentGenerateFirstBinding
import com.lollipop.wallpaper.utils.WindowInsetsHelper
import com.lollipop.wallpaper.utils.fixInsetsByPadding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 色彩生成的第一引导页面
 * @author Lollipop
 * @date 2021/07/03
 */
class GenerateFirstFragment : GenerateBaseFragment() {

    private val binding: FragmentGenerateFirstBinding by lazyBind()

    private var callback: Callback? = null

    override val nextStepAction: Int
        get() = R.id.actionFirstToRetrieval

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nextBtn.setOnClickListener {
            nextStep()
        }
        binding.root.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
        startLoading()
    }

    interface Callback {

    }

}