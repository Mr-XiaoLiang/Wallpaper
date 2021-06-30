package com.lollipop.wallpaper.activity

import android.os.Bundle
import com.lollipop.wallpaper.databinding.ActivityGenerateBinding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 生成颜色的activity
 * @author Lollipop
 * @date 2021/06/30
 */
class GenerateActivity : BaseActivity() {

    private val binding: ActivityGenerateBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
    }
}