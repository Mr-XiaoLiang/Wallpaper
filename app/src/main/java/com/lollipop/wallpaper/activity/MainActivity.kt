package com.lollipop.wallpaper.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivityMainBinding
import com.lollipop.wallpaper.dialog.HeaderMessageDialog
import com.lollipop.wallpaper.entitys.AppInfo
import com.lollipop.wallpaper.list.AppUsageHolder
import com.lollipop.wallpaper.service.LWallpaperService
import com.lollipop.wallpaper.utils.*


class MainActivity : BaseActivity() {

    private val binding: ActivityMainBinding by lazyBind()

    override val optionMenuId: Int
        get() = R.menu.activity_main_menu

    override val guideLayoutId: Int
        get() = R.layout.guide_main

    private val packageUsageHelper = PackageUsageHelper(this)

    private val settings = LSettings.bind(this)

    private val appUsageInfoList = ArrayList<AppInfo>()

    private val backgroundHelper by lazy {
        BackgroundHelper.write(this)
    }

    private val backgroundLauncher by lazy {
        registerForActivityResult(
            BackgroundHelper.createActivityResultContract(),
            backgroundHelper.getActivityResultCallback()
        )
    }

    private val wallpaperResultLauncher by lazy {
        registerForActivityResult(WallpaperResultContract()) {
            if (it) {
                Toast.makeText(
                    this,
                    R.string.apply_wallpaper_success,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
        binding.recyclerView.fixInsetsByPadding(
            edge = WindowInsetsHelper.Edge.CONTENT
        )
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.design_default_color_primary,
            R.color.design_default_color_secondary
        )
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
        binding.permissionButton.setOnClickListener {
            PackageUsageHelper.openSettingsPage(this)
        }
        binding.recyclerView.adapter = UsageAdapter(appUsageInfoList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 初始化一次
        backgroundLauncher
        wallpaperResultLauncher
    }

    override fun canShowGuide(): Boolean {
        if (settings.isNeedShowGenerate(this)) {
            startActivity(Intent(this, GenerateActivity::class.java))
            settings.onGenerateShown(this)
            return false
        }
        return super.canShowGuide()
    }

    override fun onResume() {
        super.onResume()
        doAsync {
            LWallpaperService.notifyGroupInfoChanged(this)
        }
        packageUsageHelper.updateGroupMap(settings.getPackageInfo())
        loadData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.swipeRefreshLayout.visibleOrInvisible(true)
        binding.permissionDialog.visibleOrGone(false)
        doAsync {
            packageUsageHelper.loadUsageData()
            if (packageUsageHelper.isEmpty) {
                appUsageInfoList.clear()
                onUI {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.swipeRefreshLayout.visibleOrInvisible(false)
                    binding.permissionDialog.visibleOrInvisible(true)
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                }
            } else {
                appUsageInfoList.clear()
                onUI {
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                }
                packageUsageHelper.loadAppInfo()
                appUsageInfoList.addAll(packageUsageHelper.appInfoList)
                appUsageInfoList.sortWith { a, b -> (b.usageTime - a.usageTime).toInt() }
                onUI {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.swipeRefreshLayout.visibleOrInvisible(true)
                    binding.permissionDialog.visibleOrGone(false)
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.legend -> {
                startActivity(Intent(this, LegendActivity::class.java))
            }
            R.id.palette -> {
                startActivity(Intent(this, PaletteActivity::class.java))
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.background -> {
                HeaderMessageDialog.create(this)
                    .setMessage(getString(R.string.msg_change_background))
                    .setCancelMessage(getString(R.string.delete)) {
                        backgroundHelper.delete()
                        it.dismiss()
                    }
                    .setEnterButton(getString(R.string.change)) {
                        backgroundLauncher.launch(Unit)
                        it.dismiss()
                    }.show()
            }
            R.id.generate -> {
                startActivity(Intent(this, GenerateActivity::class.java))
            }
            R.id.apply -> {
                try {
                    wallpaperResultLauncher.launch(WallpaperRequestType.CHANGE_LIVE_WALLPAPER)
                } catch (e: Throwable) {
                    wallpaperResultLauncher.launch(WallpaperRequestType.LIVE_WALLPAPER_CHOOSER)
                }
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private class UsageAdapter(
        private val data: List<AppInfo>
    ) : RecyclerView.Adapter<AppUsageHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageHolder {
            return AppUsageHolder.create(parent)
        }

        override fun onBindViewHolder(holder: AppUsageHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class WallpaperResultContract :
        ActivityResultContract<WallpaperRequestType, Boolean>() {
        override fun createIntent(context: Context, input: WallpaperRequestType?): Intent {
            return if (input == WallpaperRequestType.CHANGE_LIVE_WALLPAPER) {
                Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(context, LWallpaperService::class.java)
                    )
                }
            } else {
                Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }

    private enum class WallpaperRequestType {
        CHANGE_LIVE_WALLPAPER,
        LIVE_WALLPAPER_CHOOSER
    }
}