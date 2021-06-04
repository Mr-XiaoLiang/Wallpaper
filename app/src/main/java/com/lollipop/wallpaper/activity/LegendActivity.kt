package com.lollipop.wallpaper.activity

import android.os.Bundle
import com.lollipop.wallpaper.databinding.ActivityLegendBinding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 图例的activity
 * @author Lollipop
 */
class LegendActivity : BaseActivity() {

    private val binding: ActivityLegendBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
    }

}