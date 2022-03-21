package com.lollipop.wallpaper.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.utils.LSettings
import com.lollipop.wallpaper.utils.WindowInsetsHelper
import com.lollipop.wallpaper.utils.delay

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(R.layout.activity_launcher)
        delay(1000) {
            checkPrivacyAgreement()
        }
    }

    private fun checkPrivacyAgreement() {
        val settings = LSettings.bind(this)
        if (!settings.allowPrivacyAgreement) {
            PrivacyAgreementActivity.start(this)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}