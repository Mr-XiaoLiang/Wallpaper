package com.lollipop.wallpaper.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.lollipop.wallpaper.databinding.ActivityGenerateBinding
import com.lollipop.wallpaper.entitys.AppColorInfo
import com.lollipop.wallpaper.generate.*
import com.lollipop.wallpaper.utils.*

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

    private val paletteHelper = PaletteHelper()

    private val packageUsageHelper = PackageUsageHelper(this)

    private val appColorList = ArrayList<AppColorInfo>()

    private var onAppInfoLoadedListener: ((List<AppColorInfo>) -> Unit)? = null

    private var isAppInfoLoading = false

    private val appLoadTask = task {
        isAppInfoLoading = true
        onUI {
            onAppInfoLoadStart()
        }
        packageUsageHelper.loadAppInfo()
        if (isDestroyed) {
            return@task
        }
        val appInfoList = packageUsageHelper.appInfoList
        val appColors = ArrayList<AppColorInfo>()
        appInfoList.forEach {
            if (isDestroyed) {
                return@task
            }
            val color = paletteHelper.getColor(it.icon)
            appColors.add(AppColorInfo(it, color))
        }
        if (isDestroyed) {
            return@task
        }
        appColorList.clear()
        appColorList.addAll(appColors)
        isAppInfoLoading = false
        onUI {
            onAppInfoLoadEnd()
        }
    }

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
        onAppInfoLoadedListener = callback
        if (!isAppInfoLoading) {
            appLoadTask.run()
        }
    }

    private fun onAppInfoLoadStart() {
        if (isDestroyed) {
            return
        }
        // 可以做点什么
    }

    private fun onAppInfoLoadEnd() {
        if (isDestroyed) {
            return
        }
        onAppInfoLoadedListener?.invoke(appColorList)
    }

    override fun onDestroy() {
        super.onDestroy()
        onAppInfoLoadedListener = null
        paletteHelper.destroy()
        appLoadTask.cancel()
    }

}