package com.lollipop.wallpaper.activity

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivityMainBinding
import com.lollipop.wallpaper.entitys.AppInfo
import com.lollipop.wallpaper.list.AppUsageHolder
import com.lollipop.wallpaper.service.LWallpaperService
import com.lollipop.wallpaper.utils.*


class MainActivity : BaseActivity() {

    companion object {
        private const val REQUEST_CODE_SET_WALLPAPER = 120
    }

    private val binding: ActivityMainBinding by lazyBind()

    override val optionMenuId: Int
        get() = R.menu.activity_main_menu

    override val guideLayoutId: Int
        get() = R.layout.guide_main

    private val packageUsageHelper = PackageUsageHelper(this)

    private val settings = LSettings.bind(this)

    private val appUsageInfoList = ArrayList<AppInfo>()

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
        packageUsageHelper.updateGroupMap(settings.getPackageInfo())
        loadData()
    }

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
            R.id.generate -> {
                startActivity(Intent(this, GenerateActivity::class.java))
            }
            R.id.apply -> {
                try {
                    startActivityForResult(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(applicationContext, LWallpaperService::class.java)
                        )
                    }, REQUEST_CODE_SET_WALLPAPER)
                } catch (e: Throwable) {
                    try {
                        startActivityForResult(
                            Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER),
                            REQUEST_CODE_SET_WALLPAPER
                        )
                    } catch (ee: Throwable) {
                        Toast.makeText(this, R.string.apply_wallpaper_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SET_WALLPAPER && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, R.string.apply_wallpaper_success, Toast.LENGTH_SHORT).show()
        }
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

}