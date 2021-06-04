package com.lollipop.wallpaper.activity

import android.os.Bundle
import com.lollipop.wallpaper.databinding.ActivitySettingsBinding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 设置的Activity
 * @author Lollipop
 */
class SettingsActivity : BaseActivity() {

    private val binding: ActivitySettingsBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
    }

}