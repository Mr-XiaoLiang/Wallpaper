package com.lollipop.wallpaper.generate

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.viewbinding.ViewBinding
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.FragmentGenerateGroupPreferenceBinding
import com.lollipop.wallpaper.dialog.GridSelectedDialog
import com.lollipop.wallpaper.utils.*

/**
 * 色彩生成的分组偏好设置页面
 * 将会必要的说明以及分组数量的偏好设置
 * @author Lollipop
 * @date 2021/07/03
 */
class GenerateGroupPreferenceFragment : GenerateBaseFragment(), AdapterView.OnItemSelectedListener {

    companion object {
        private const val MIN_GROUPING_COUNT = PaletteHelper.MIN_GROUPING_COUNT
        private const val DEFAULT_GROUPING_COUNT = PaletteHelper.DEFAULT_GROUPING_COUNT
        private const val MAX_GROUPING_COUNT = PaletteHelper.MAX_GROUPING_COUNT
    }

    private val binding: FragmentGenerateGroupPreferenceBinding by lazyBind()

    private var callback: Callback? = null

    override val nextStepAction: Int
        get() = R.id.actionGroupPreferenceToGrouping

    override val contentViewBinding: ViewBinding
        get() = binding

    private val countArray: Array<String> by lazy {
        val itemList = ArrayList<String>()
        for (count in MIN_GROUPING_COUNT..MAX_GROUPING_COUNT) {
            itemList.add("$count")
        }
        itemList.toTypedArray()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
        initView()
    }

    private fun initView() {
        binding.nextBtn.setOnClickListener {
            nextStep()
        }
        binding.groupCountSpinner.setOnClickListener {
            showDialog()
        }
        val defaultCount = (callback?.getGroupingCount() ?: DEFAULT_GROUPING_COUNT).range(
            MIN_GROUPING_COUNT, MAX_GROUPING_COUNT
        )
        setCount(defaultCount)
    }

    private fun showDialog() {
        GridSelectedDialog.create(this)
            .setGridCount(3)
            .setData(countArray)
            .onSelected { dialog, position, _ ->
                val count = position + MIN_GROUPING_COUNT
                setCount(count)
                dialog.dismiss()
            }
            .show()
    }

    private fun setCount(count: Int) {
        binding.groupCountSpinner.text = "$count"
        callback?.setGroupingCount(count)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        checkIdentity<Callback>(context) {
            callback = it
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    interface Callback {
        fun getGroupingCount(): Int
        fun setGroupingCount(count: Int)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        callback?.setGroupingCount(position + MIN_GROUPING_COUNT)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        callback?.setGroupingCount(DEFAULT_GROUPING_COUNT)
    }
}