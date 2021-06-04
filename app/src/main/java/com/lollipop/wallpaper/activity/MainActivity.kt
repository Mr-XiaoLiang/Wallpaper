package com.lollipop.wallpaper.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.utils.applyWindowInsetsByPadding
import com.lollipop.wallpaper.databinding.ActivityMainBinding
import com.lollipop.wallpaper.utils.initWindowFlag
import com.lollipop.wallpaper.utils.lazyBind

class MainActivity : BaseActivity() {

    private val binding: ActivityMainBinding by lazyBind()

    override val optionMenuId: Int
        get() = R.menu.activity_main_ment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
        binding.recyclerView.applyWindowInsetsByPadding()
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