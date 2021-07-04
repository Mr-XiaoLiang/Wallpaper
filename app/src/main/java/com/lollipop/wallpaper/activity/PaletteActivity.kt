package com.lollipop.wallpaper.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivityPaletteBinding
import com.lollipop.wallpaper.databinding.ItemGroupInfoBinding
import com.lollipop.wallpaper.databinding.ItemPresetColorBinding
import com.lollipop.wallpaper.engine.UsageStatsGroupInfo
import com.lollipop.wallpaper.list.ListTouchHelper
import com.lollipop.wallpaper.list.ViewBindingHolder
import com.lollipop.wallpaper.service.LWallpaperService
import com.lollipop.wallpaper.utils.*

/**
 * 调色盘的Activity
 * @author Lollipop
 */
class PaletteActivity : BaseActivity() {

    private val binding: ActivityPaletteBinding by lazyBind()

    private var dialogSelectedColor = Color.GREEN

    private val colorStore = ColorStore()

    private val groupInfoList = ArrayList<UsageStatsGroupInfo>()

    private val settings = LSettings.bind(this)

    private var changedGroupInfo: UsageStatsGroupInfo? = null

    override val optionMenuId: Int
        get() = R.menu.activity_palette_menu

    override val guideLayoutId: Int
        get() = R.layout.guide_palette

    private val dialogAnimationHelper = AnimationHelper(onUpdate = ::onDialogAnimationUpdate)

    private val isDialogClosed: Boolean
        get() {
            return dialogAnimationHelper.progressIs(AnimationHelper.PROGRESS_MIN)
        }

    private val saveInfoTask = task {
        val context = applicationContext
        doAsync {
            colorStore.saveTo(settings)
            settings.setGroupInfo(groupInfoList)
            LWallpaperService.notifyGroupInfoChanged(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
        initView()
    }

    private fun initView() {
        initPalette()
        binding.groupInfoView.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        binding.groupInfoView.adapter = GroupAdapter(groupInfoList, ::onGroupInfoClick)
        binding.groupInfoView.fixInsetsByPadding(
            edge = WindowInsetsHelper.Edge.CONTENT
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPalette() {
        binding.huePaletteView.onHueChange { hue, _ ->
            binding.satValPaletteView.onHueChange(hue.toFloat())
        }
        binding.satValPaletteView.onHSVChange { _, color, _ ->
            dialogSelectedColor = color
            val drawable = binding.colorPreviewView.drawable
            if (drawable is ColorDrawable) {
                drawable.color = color
            } else {
                binding.colorPreviewView.setImageDrawable(
                    ColorDrawable(color)
                )
            }
        }
        binding.presetColorView.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.presetColorView.adapter =
            PresetColorAdapter(colorStore, ::onPresetColorClick)

        binding.confirmButton.setOnClickListener {
            onColorConfirm()
        }

        ListTouchHelper.create()
            .setMoveDirection {
                start = true
                end = true
            }.setSwipeDirection {
                down = true
            }.autoMoveWithList(colorStore)
            .onSwiped(::onPresetColorSwiped)
            .bindTo(binding.presetColorView)

        ListTouchHelper.create()
            .setMoveDirection {
                start = true
                end = true
                up = true
                down = true
            }.setSwipeDirection {
                start = true
                end = true
            }.autoMoveWithList(groupInfoList)
            .onSwiped(::onGroupInfoSwiped)
            .bindTo(binding.groupInfoView)

        dialogAnimationHelper.onStart {
            binding.groupInfoCardView.visibleOrInvisible(true)
            binding.paletteCardView.visibleOrInvisible(true)
        }

        dialogAnimationHelper.onEnd {
            if (dialogAnimationHelper.progressIs(AnimationHelper.PROGRESS_MIN)) {
                binding.groupInfoCardView.visibleOrInvisible(false)
                binding.paletteCardView.visibleOrInvisible(false)
            }
        }

        binding.groupInfoCardView.setOnClickListener {
            closeBoard()
        }

        binding.dialogTouchInterceptView.setOnTouchListener { _, _ -> true }

        binding.groupInfoCardView.post {
            onDialogAnimationUpdate(0F)
            binding.groupInfoCardView.visibleOrInvisible(false)
            binding.paletteCardView.visibleOrInvisible(false)
        }
    }

    override fun onStart() {
        super.onStart()
        colorStore.reload(settings) {
            binding.presetColorView.adapter?.notifyDataSetChanged()
        }

        groupInfoList.clear()
        binding.groupInfoView.adapter?.notifyDataSetChanged()
        doAsync {
            groupInfoList.addAll(settings.getGroupInfo())
            onUI {
                binding.groupInfoView.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        saveInfo()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                openDialog(null)
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (!isDialogClosed) {
            closeDialog()
            return
        }
        super.onBackPressed()
    }

    private fun onDialogAnimationUpdate(progress: Float) {
        val viewProgress = (progress - 1F)
        binding.groupInfoCardView.apply {
            translationY = (top + height) * viewProgress
        }
        binding.paletteCardView.apply {
            translationY = (top + height) * viewProgress
        }
    }

    private fun onColorConfirm() {
        val name = binding.groupNameEditView.text?.toString() ?: ""
        if (name.isBlank()) {
            binding.groupNameEditView.error = getString(R.string.blank_content)
            return
        }
        var key = ""

        val editInfo = changedGroupInfo
        var index = 0
        if (editInfo != null) {
            key = editInfo.key
            index = groupInfoList.indexOf(editInfo)
            if (index >= 0) {
                groupInfoList.removeAt(index)
            } else {
                index = 0
            }
        }
        if (key.isEmpty()) {
            key = UsageStatsGroupInfo.generateKey(name)
        }

        groupInfoList.forEach { info ->
            if (info.key == key) {
                binding.groupNameEditView.error = getString(R.string.content_consistent)
                return
            }
        }

        val color = dialogSelectedColor
        colorStore.put(color) {
            binding.presetColorView.adapter?.notifyItemInserted(it)
        }

        groupInfoList.add(
            index,
            UsageStatsGroupInfo(
                key = key,
                name = name,
                color = color
            )
        )

        binding.groupInfoView.adapter?.apply {
            if (editInfo == null) {
                notifyItemInserted(0)
            } else {
                notifyItemChanged(index)
            }
        }
        closeDialog()
        saveInfo()
    }

    private fun openDialog(info: UsageStatsGroupInfo?) {
        this.changedGroupInfo = info
        binding.groupNameEditView.setText(info?.name ?: "")
        updatePalette(info?.color ?: Color.RED)
        binding.dialogTouchInterceptView.visibleOrGone(true)
        dialogAnimationHelper.open()
    }

    private fun closeDialog() {
        changedGroupInfo = null
        closeBoard()
        binding.dialogTouchInterceptView.visibleOrGone(false)
        dialogAnimationHelper.close()
    }

    private fun onPresetColorSwiped(
        position: Int,
        direction: ListTouchHelper.Direction
    ) {
        if (direction.down) {
            colorStore.removeAt(position)
            binding.presetColorView.adapter?.notifyItemRemoved(position)
            saveInfo()
        }
    }

    private fun onGroupInfoSwiped(
        position: Int,
        direction: ListTouchHelper.Direction
    ) {
        if (direction.start || direction.end) {
            groupInfoList.removeAt(position)
            binding.groupInfoView.adapter?.notifyItemRemoved(position)
            saveInfo()
        }
    }

    private fun onPresetColorClick(index: Int) {
        updatePalette(colorStore[index])
    }

    private fun onGroupInfoClick(index: Int) {
        openDialog(groupInfoList[index])
    }

    private fun updatePalette(color: Int) {
        dialogSelectedColor = color
        binding.huePaletteView.parser(color)
        binding.satValPaletteView.parser(color)
    }

    private fun saveInfo() {
        saveInfoTask.cancel()
        saveInfoTask.delay(100L)
    }

    private class PresetColorAdapter(
        private val data: List<Int>,
        private val onClickCallback: (Int) -> Unit
    ) : RecyclerView.Adapter<PresetColorHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetColorHolder {
            return PresetColorHolder.create(parent, onClickCallback)
        }

        override fun onBindViewHolder(holder: PresetColorHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class PresetColorHolder private constructor(
        viewBinding: ItemPresetColorBinding,
        private val onClickCallback: (Int) -> Unit
    ) : ViewBindingHolder(viewBinding) {

        companion object {
            fun create(parent: ViewGroup, callback: (Int) -> Unit): PresetColorHolder {
                return PresetColorHolder(parent.bind(), callback).apply {
                    itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        }

        private val colorDrawable = ColorDrawable()

        init {
            viewBinding.colorPreviewView.setImageDrawable(colorDrawable)
            viewBinding.colorPreviewView.setOnClickListener {
                onClickCallback(adapterPosition)
            }
        }

        fun bind(color: Int) {
            colorDrawable.color = color
        }

    }

    private class GroupAdapter(
        private val data: List<UsageStatsGroupInfo>,
        private val onClickCallback: (Int) -> Unit
    ) : RecyclerView.Adapter<GroupHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
            return GroupHolder.create(parent, onClickCallback)
        }

        override fun onBindViewHolder(holder: GroupHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class GroupHolder private constructor(
        private val viewBinding: ItemGroupInfoBinding,
        private val onClickCallback: (Int) -> Unit
    ) : ViewBindingHolder(viewBinding) {

        companion object {
            fun create(parent: ViewGroup, callback: (Int) -> Unit): GroupHolder {
                return GroupHolder(parent.bind(), callback).apply {
                    itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        }

        private val colorDrawable = ColorDrawable()

        init {
            viewBinding.colorPreviewView.setImageDrawable(colorDrawable)
            viewBinding.cardView.setOnClickListener {
                onClickCallback(adapterPosition)
            }
        }

        fun bind(info: UsageStatsGroupInfo) {
            viewBinding.groupLabelView.text = info.name
            colorDrawable.color = info.color
        }

    }

}