package com.lollipop.wallpaper.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.lollipop.wallpaper.databinding.ActivityGenerateBinding
import com.lollipop.wallpaper.entitys.AppColorInfo
import com.lollipop.wallpaper.generate.*
import com.lollipop.wallpaper.utils.lazyBind

/**
 * 生成颜色的activity
 * @author Lollipop
 * @date 2021/06/30
 */
class GenerateActivity : BaseActivity(),
    GenerateBaseFragment.Callback,
    GenerateFirstFragment.Callback,
    GenerateRetrievalFragment.Callback,
    GenerateGroupPreferenceFragment.Callback,
    GenerateGroupingFragment.Callback,
    GenerateCompleteFragment.Callback
{

    private val binding: ActivityGenerateBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
    }

    override fun nextStep(fragment: Fragment): Boolean {
        if (fragment is GenerateCompleteFragment) {
            finish()
            return true
        }
        return false
    }

    override fun getAppList(callback: (List<AppColorInfo>) -> Unit) {
        TODO("Not yet implemented")
    }

}