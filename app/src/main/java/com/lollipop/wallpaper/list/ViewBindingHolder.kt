package com.lollipop.wallpaper.list

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * @author lollipop
 * @date 2021/7/4 12:26
 * 简单包装构造方法的ViewHolder
 */
open class ViewBindingHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    val context: Context
        get() {
            return itemView.context
        }

}