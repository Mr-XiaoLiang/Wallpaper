package com.lollipop.wallpaper.activity

import android.os.Bundle
import android.widget.SeekBar
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivitySettingsBinding
import com.lollipop.wallpaper.service.LWallpaperService
import com.lollipop.wallpaper.utils.*
import java.lang.StringBuilder

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
        binding.settingsRoot.applyWindowInsetsByPadding(enableTop = false)
        binding.appVersionView.text = versionName()

        binding.updateDelaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.updateDelayValueView.text = progressToString(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.updateDelaySeekBar.progress = settings.updateDelay
    }

    override fun onStop() {
        val progress = binding.updateDelaySeekBar.progress
        doAsync {
            settings.updateDelay = progress
            LWallpaperService.notifyGroupInfoChanged(this)
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