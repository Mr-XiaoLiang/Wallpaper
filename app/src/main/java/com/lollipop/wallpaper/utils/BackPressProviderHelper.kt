package com.lollipop.wallpaper.utils

import com.lollipop.wallpaper.listener.BackPressListener
import com.lollipop.wallpaper.provider.BackPressProvider

/**
 * @author lollipop
 * @date 4/16/21 22:07
 * 返回事件提供者的辅助工具
 * 它主要负责通用的事件分发逻辑
 */
class BackPressProviderHelper(
        firstListener: BackPressListener? = null
) : BackPressProvider, BackPressListener {

    private val listenerList = ArrayList<BackPressListener>().apply {
        if (firstListener != null) {
            add(firstListener)
        }
    }

    override fun addBackPressListener(listener: BackPressListener) {
        listenerList.add(listener)
    }

    override fun removeBackPressListener(listener: BackPressListener) {
        listenerList.remove(listener)
    }

    override fun onBackPressed(): Boolean {
        for (listener in listenerList) {
            if (listener.onBackPressed()) {
                return true
            }
        }
        return false
    }

    fun destroy() {
        listenerList.clear()
    }

}