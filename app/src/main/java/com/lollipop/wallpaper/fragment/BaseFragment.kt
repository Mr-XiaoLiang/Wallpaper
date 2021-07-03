package com.lollipop.wallpaper.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import com.lollipop.wallpaper.listener.BackPressListener
import com.lollipop.wallpaper.provider.BackPressProvider
import com.lollipop.wallpaper.utils.BackPressProviderHelper

/**
 * @author lollipop
 * @date 2021/7/3 12:37
 * 基础的Fragment
 */
open class BaseFragment : Fragment(), BackPressProvider, BackPressListener {

    private val backPressProviderHelper = BackPressProviderHelper()

    private var parentBackPressProvider: BackPressProvider? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        checkIdentity<BackPressProvider>(context) {
            it.addBackPressListener(this)
            parentBackPressProvider = it
        }
    }

    override fun onDetach() {
        super.onDetach()
        parentBackPressProvider?.removeBackPressListener(this)
        parentBackPressProvider = null
    }

    protected inline fun <reified T> checkIdentity(context: Context, callback: (T) -> Unit) {
        parentFragment?.let {
            if (it is T) {
                callback(it)
                return
            }
        }
        context.let {
            if (it is T) {
                callback(it)
                return
            }
        }
        getContext()?.let {
            if (it is T) {
                callback(it)
                return
            }
        }
        activity?.let {
            if (it is T) {
                callback(it)
                return
            }
        }
    }

    override fun addBackPressListener(listener: BackPressListener) {
        backPressProviderHelper.addBackPressListener(listener)
    }

    override fun removeBackPressListener(listener: BackPressListener) {
        backPressProviderHelper.removeBackPressListener(listener)
    }

    override fun onBackPressed(): Boolean {
        return backPressProviderHelper.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressProviderHelper.destroy()
    }

}