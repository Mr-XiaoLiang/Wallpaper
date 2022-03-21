package com.lollipop.wallpaper.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivitySettingsBinding
import com.lollipop.wallpaper.service.LWallpaperService
import com.lollipop.wallpaper.utils.*


/**
 * 设置的Activity
 * @author Lollipop
 */
class SettingsActivity : BaseActivity() {

    companion object {
        private const val ONE_HOUR = 60
    }

    private val settings = LSettings.bind(this)

    private val binding: ActivitySettingsBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
        binding.settingsRoot.fixInsetsByPadding(
            edge = WindowInsetsHelper.Edge.CONTENT
        )
        binding.appVersionView.text = versionName()

        binding.updateDelaySeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.updateDelayValueView.text = progressToString(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.updateDelaySeekBar.progress = settings.updateDelay
        binding.animationSwitchView.isChecked = settings.animationEnable
        binding.animationSwitchView.setOnCheckedChangeListener { _, isChecked ->
            settings.animationEnable = isChecked
        }
        binding.copyright.setOnClickListener {
            openGitHub()
        }
        binding.appVersionView.setOnClickListener {
            openGitHub()
        }
        binding.privacyAgreement.setOnClickListener {
            PrivacyAgreementActivity.start(this)
        }
    }

    private fun openGitHub() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Mr-XiaoLiang/Wallpaper/releases")
                )
            )
        } catch (e: Throwable) {
        }
    }

    override fun onStop() {
        val progress = binding.updateDelaySeekBar.progress
        val context = applicationContext
        doAsync {
            settings.updateDelay = progress
            LWallpaperService.notifyGroupInfoChanged(context)
        }
        super.onStop()
    }

    private fun progressToString(progress: Int): String {
        val builder = StringBuilder()
        (progress / ONE_HOUR).let { hour ->
            if (hour > 0) {
                builder.append(
                    getString(
                        R.string.unit_hour,
                        hour
                    )
                )
            }
        }
        (progress % ONE_HOUR).let { minute ->
            builder.append(
                getString(
                    R.string.unit_minute,
                    minute
                )
            )
        }
        return builder.toString()
    }

}