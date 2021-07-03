package com.lollipop.wallpaper.generate

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.lollipop.wallpaper.fragment.BaseFragment

/**
 * @author lollipop
 * @date 2021/7/3 12:55
 * 生成颜色的流程分页的基础
 */
abstract class GenerateBaseFragment: BaseFragment() {

    private var stepCallback: Callback? = null

    abstract val nextStepAction: Int

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