package com.lollipop.wallpaper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lollipop.wallpaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}