package com.lollipop.wallpaper.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
    GenerateCompleteFragment.Callback {

    private val binding: ActivityGenerateBinding by lazyBind()

    private val paletteHelper = PaletteHelper()

    private val packageUsageHelper = PackageUsageHelper(this)

    private val appColorList = ArrayList<AppColorInfo>()

    private val selectedColorIndex = ArrayList<Int>()

    private var onAppInfoLoadedListener: ((List<AppColorInfo>) -> Unit)? = null

    private var isAppInfoLoading = false

    private var childOptionMenuId = 0

    private var onOptionMenuSelectedListener: ((Int) -> Unit)? = null

    private val appLoadTask = task {
        isAppInfoLoading = true
        onUI {
            onAppInfoLoadStart()
        }
        // 如果是空的，才加载
        if (appColorList.isEmpty()) {
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
            selectedColorIndex.clear()
            appColorList.clear()
            appColorList.addAll(appColors)
        }
        isAppInfoLoading = false
        onUI {
            onAppInfoLoadEnd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu ?: return super.onPrepareOptionsMenu(menu)
        menu.clear()
        if (childOptionMenuId != 0) {
            menuInflater.inflate(childOptionMenuId, menu)
        }
//        androidx.fragment.app.FragmentContainerView
//        androidx.fragment.app.FragmentContainerView
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val superResult = super.onOptionsItemSelected(item)
        if (superResult) {
            return true
        }
        val listener = onOptionMenuSelectedListener ?: return false
        listener(item.itemId)
        return true
    }

    override fun nextStep(fragment: Fragment): Boolean {
        if (fragment is GenerateCompleteFragment) {
            finish()
            return true
        }
        return false
    }

    override fun setOptionMenu(menuId: Int) {
        childOptionMenuId = menuId
        invalidateOptionsMenu()
    }

    override fun setOptionMenuListener(callback: (Int) -> Unit) {
        onOptionMenuSelectedListener = callback
    }

    override fun getAppList(callback: (List<AppColorInfo>) -> Unit) {
        onAppInfoLoadedListener = callback
        if (!isAppInfoLoading) {
            appLoadTask.run()
        }
    }

    override fun onSelectedColorChange(position: Int, colorIndex: Int) {
        while (selectedColorIndex.size <= position) {
            selectedColorIndex.add(0)
        }
        selectedColorIndex[position] = colorIndex
    }

    override fun getSelectedColorIndex(position: Int): Int {
        if (selectedColorIndex.size > position) {
            return selectedColorIndex[position]
        }
        return 0
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