package com.lollipop.wallpaper.generate

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.lollipop.wallpaper.databinding.FragmentGenerateBaseBinding
import com.lollipop.wallpaper.fragment.BaseFragment
import com.lollipop.wallpaper.utils.lazyBind

/**
 * @author lollipop
 * @date 2021/7/3 12:55
 * 生成颜色的流程分页的基础
 */
abstract class GenerateBaseFragment : BaseFragment() {

    private var stepCallback: Callback? = null

    abstract val nextStepAction: Int

    abstract val contentViewBinding: ViewBinding

    private val baseBinding: FragmentGenerateBaseBinding by lazyBind()

    protected var isLoading = false
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (contentViewBinding.root.parent != baseBinding.fragmentContent) {
            baseBinding.fragmentContent.addView(
                contentViewBinding.root,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return baseBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        endLoading()
    }

    protected fun startLoading() {
        isLoading = true
        baseBinding.contentLoadingView.show()
    }

    protected fun endLoading() {
        isLoading = false
        baseBinding.contentLoadingView.hide()
    }

    override fun onStart() {
        super.onStart()
        // 重新启动的时候，如果加载状态不变，那么将会让它继续显示
        if (isLoading) {
            baseBinding.contentLoadingView.show()
        }
    }

    override fun onStop() {
        super.onStop()
        baseBinding.contentLoadingView.hide()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        checkIdentity<Callback>(context) {
            stepCallback = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stepCallback = null
    }

    protected fun nextStep() {
        if (stepCallback?.nextStep(this) == true) {
            return
        }
        if (nextStepAction == 0) {
            return
        }
        NavHostFragment.findNavController(this).navigate(nextStepAction)
    }

    interface Callback {
        fun nextStep(fragment: Fragment): Boolean
    }

}