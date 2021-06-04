package com.lollipop.wallpaper.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.lollipop.wallpaper.databinding.ActivityBaseBinding
import com.lollipop.wallpaper.utils.applyWindowInsetsByPadding
import com.lollipop.wallpaper.utils.initWindowFlag
import com.lollipop.wallpaper.utils.lazyBind

/**
 * @author lollipop
 * @date 2021/6/5 00:49
 */
open class BaseActivity : AppCompatActivity() {

    protected val baseBinding: ActivityBaseBinding by lazyBind()

    protected open val optionMenuId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindowFlag()
        super.setContentView(baseBinding.root)
        setSupportActionBar(baseBinding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        baseBinding.appBarLayout.applyWindowInsetsByPadding()
    }

    fun setContentView(binding: ViewBinding) {
        setContentView(binding.root)
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(layoutInflater.inflate(layoutResID, baseBinding.contentRoot, false))
    }

    override fun setContentView(view: View) {
        setContentView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        baseBinding.contentRoot.addView(view, params)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (optionMenuId != 0) {
            menuInflater.inflate(optionMenuId, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }


}