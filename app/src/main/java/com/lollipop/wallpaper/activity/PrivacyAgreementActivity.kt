package com.lollipop.wallpaper.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.wallpaper.databinding.ActivityPrivacyAgreementBinding
import com.lollipop.wallpaper.utils.*

class PrivacyAgreementActivity : AppCompatActivity() {

    companion object {
        fun start(context: Activity) {
            context.startActivity(Intent(context, PrivacyAgreementActivity::class.java))
        }
    }

    private val binding: ActivityPrivacyAgreementBinding by lazyBind()

    private val settings by lazy {
        LSettings.bind(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        binding.pageRoot.fixInsetsByPadding(WindowInsetsHelper.Edge.ALL)
        val allowPrivacyAgreement = settings.allowPrivacyAgreement
        binding.privacyAgreementAllowButton.visibleOrGone(!allowPrivacyAgreement) {
            setOnClickListener {
                settings.allowPrivacyAgreement = true
                startActivity(Intent(this@PrivacyAgreementActivity, MainActivity::class.java))
                finish()
            }
        }
        binding.privacyAgreementRefuseButton.visibleOrGone(!allowPrivacyAgreement) {
            setOnClickListener {
                finish()
            }
        }
    }

}