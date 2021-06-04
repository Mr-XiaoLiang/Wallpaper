package com.lollipop.wallpaper.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivityMainBinding
import com.lollipop.wallpaper.utils.*

class MainActivity : BaseActivity() {

    private val binding: ActivityMainBinding by lazyBind()

    override val optionMenuId: Int
        get() = R.menu.activity_main_ment

    private val packageUsageHelper = PackageUsageHelper(this)

    private val settings = LSettings.bind(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
        binding.recyclerView.applyWindowInsetsByPadding()
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
            packageUsageHelper.loadData()
            if (packageUsageHelper.isEmpty) {
                onUI {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.swipeRefreshLayout.visibleOrInvisible(false)
                    binding.permissionDialog.visibleOrInvisible(true)
                }
            } else {
                // TODO
                onUI {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.swipeRefreshLayout.visibleOrInvisible(true)
                    binding.permissionDialog.visibleOrGone(false)
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
            R.id.helper -> {

            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

}