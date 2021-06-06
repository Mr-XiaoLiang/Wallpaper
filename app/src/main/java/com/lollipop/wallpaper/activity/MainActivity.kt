package com.lollipop.wallpaper.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivityMainBinding
import com.lollipop.wallpaper.list.AppUsageHolder
import com.lollipop.wallpaper.utils.*

class MainActivity : BaseActivity() {

    private val binding: ActivityMainBinding by lazyBind()

    override val optionMenuId: Int
        get() = R.menu.activity_main_ment

    override val guideLayoutId: Int
        get() = R.layout.guide_main

    private val packageUsageHelper = PackageUsageHelper(this)

    private val settings = LSettings.bind(this)

    private val appUsageInfoList = ArrayList<PackageUsageHelper.AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
        binding.recyclerView.applyWindowInsetsByPadding(
            enableTop = false
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
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private class UsageAdapter(
        private val data: List<PackageUsageHelper.AppInfo>
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