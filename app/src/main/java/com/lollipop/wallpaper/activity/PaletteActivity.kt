package com.lollipop.wallpaper.activity

import android.os.Bundle
import com.lollipop.wallpaper.databinding.ActivityPaletteBinding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 调色盘的Activity
 * @author Lollipop
 */
class PaletteActivity : BaseActivity() {

    private val binding: ActivityPaletteBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
    }

}