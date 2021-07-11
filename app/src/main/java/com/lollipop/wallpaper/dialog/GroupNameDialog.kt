package com.lollipop.wallpaper.dialog

import android.app.Activity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.DialogGroupNameBinding
import com.lollipop.wallpaper.utils.lazyBind

/**
 * @author lollipop
 * @date 2021/7/11 14:53
 */
class GroupNameDialog private constructor(activity: Activity) :
    HeaderDialog.DialogView() {

    companion object {
        fun create(activity: Activity, groupName: String, callback: (String) -> Unit) {
            HeaderDialog.with(activity).content {
                GroupNameDialog(activity).also { nameDialog ->
                    nameDialog.name = groupName
                    nameDialog.onConfirmListener = callback
                }
            }.build().apply {
                cardBackgroundColor = ContextCompat.getColor(activity, R.color.floatingBackground)
                show()
            }
        }

        fun create(fragment: Fragment, groupName: String, callback: (String) -> Unit) {
            val activity = fragment.activity ?: return
            create(activity, groupName, callback)
        }
    }

    override val view: View
        get() = binding.root

    private val binding: DialogGroupNameBinding by activity.lazyBind()

    private var name: String
        get() {
            return binding.groupNameEditView.text?.toString() ?: ""
        }
        set(value) {
            binding.groupNameEditView.setText(value)
        }

    private var onConfirmListener: (String) -> Unit = {}

    override fun onCreate() {
        super.onCreate()
        binding.confirmButton.setOnClickListener {
            onConfirmListener(name)
            dismiss()
        }
    }

}