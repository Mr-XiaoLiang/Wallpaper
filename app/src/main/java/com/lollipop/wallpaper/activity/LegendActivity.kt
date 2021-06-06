package com.lollipop.wallpaper.activity

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivityLegendBinding
import com.lollipop.wallpaper.databinding.ItemUsageLegendBinding
import com.lollipop.wallpaper.databinding.ItemUsageLegendFloatingBinding
import com.lollipop.wallpaper.engine.UsageStatsGroupInfo
import com.lollipop.wallpaper.engine.UsageStatsItemInfo
import com.lollipop.wallpaper.service.LWallpaperService
import com.lollipop.wallpaper.utils.*

/**
 * 图例的activity
 * @author Lollipop
 */
class LegendActivity : BaseActivity() {

    override val optionMenuId: Int
        get() = R.menu.activity_legent_menu

    override val guideLayoutId: Int
        get() = R.layout.guide_legend

    private val binding: ActivityLegendBinding by lazyBind()
    private val settings = LSettings.bind(this)
    private val packageUsageHelper = PackageUsageHelper(this)

    private val groupInfoList = ArrayList<UsageStatsGroupInfo>()
    private val pkgGroupMap = HashMap<String, String>()
    private val appInfoList = ArrayList<PackageUsageHelper.AppInfo>()

    private var swapMode = false
    private var buildMode = false

    private var selectedGroupKey = UsageStatsGroupInfo.DEFAULT_GROUP_KEY

    private val floatingPanelAnimationHelper =
        AnimationHelper(onUpdate = ::onFloatingPanelAnimationUpdate)

    private val autoSaveTask = task {
        val tempMap = HashMap<String, String>()
        tempMap.putAll(pkgGroupMap)
        doAsync {
            val list = ArrayList<UsageStatsItemInfo>()
            tempMap.entries.forEach { entry ->
                list.add(UsageStatsItemInfo(entry.value, entry.key))
            }
            settings.setPackageInfo(list)
            LWallpaperService.notifyGroupInfoChanged(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)
        initView()
    }

    private fun initView() {
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorSecondary
        )
        binding.swipeRefreshLayout.setOnRefreshListener {
            refresh()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = AppInfoAdapter(
            appInfoList,
            groupInfoList,
            ::swapMode,
            ::onAppItemClick,
            ::getGroupKeyByPackage,
            ::onPackageGroupChanged
        )
        binding.recyclerView.applyWindowInsetsByPadding(
            enableTop = false
        )
        binding.legendFloatingCardView.post {
            val offsetX = (binding.legendRoot.width - binding.legendFloatingCardView.left).toFloat()
            binding.legendFloatingCardView.translationX = offsetX
            binding.legendFloatingHandleButton.translationX = offsetX
        }
        binding.legendFloatingHandleButton.setOnClickListener {
            if (floatingPanelAnimationHelper.progressIs(AnimationHelper.PROGRESS_MIN)) {
                floatingPanelAnimationHelper.open()
            } else {
                floatingPanelAnimationHelper.close()
            }
        }
    }

    private fun refresh() {
        binding.swipeRefreshLayout.isRefreshing = true
        appInfoList.clear()
        binding.recyclerView.adapter?.notifyDataSetChanged()
        doAsync {
            groupInfoList.clear()
            groupInfoList.addAll(settings.getGroupInfo())

            val packageInfo = settings.getPackageInfo()
            pkgGroupMap.clear()
            packageInfo.forEach {
                pkgGroupMap[it.packageName] = it.groupKey
            }

            packageUsageHelper.loadAppInfo()
            val tempAppList = packageUsageHelper.appInfoList
            val appFlagArray = IntArray(tempAppList.size) { 0 }
            groupInfoList.forEach { group ->
                val key = group.key
                for (index in tempAppList.indices) {
                    if (appFlagArray[index] == 0) {
                        val app = tempAppList[index]
                        if (getGroupKeyByPackage(app.packageName) == key) {
                            appInfoList.add(app)
                            appFlagArray[index] = 1
                        }
                    }
                }
            }
            for (index in appFlagArray.indices) {
                if (appFlagArray[index] == 0) {
                    appInfoList.add(tempAppList[index])
                }
            }

            onUI {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        refresh()
    }

    override fun onStop() {
        super.onStop()
        autoSaveTask.cancel()
        autoSaveTask.run()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.swap -> {
                swapMode = !swapMode
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
            R.id.build -> {
                buildMode = !buildMode
                checkBuildMode()
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private fun checkBuildMode() {
        if (buildMode) {
            var color = Color.TRANSPARENT
            groupInfoList.forEach { group ->
                if (group.key == selectedGroupKey) {
                    color = group.color
                }
            }
            binding.legendFloatingHandleColorView.setBackgroundColor(color)
        } else {
            floatingPanelAnimationHelper.close(false)
        }
        binding.legendFloatingCardView.visibleOrInvisible(buildMode)
        binding.legendFloatingHandleButton.visibleOrInvisible(buildMode)
    }

    private fun onAppItemClick(position: Int) {
        if (!buildMode) {
            return
        }
        pkgGroupMap[appInfoList[position].packageName] = selectedGroupKey
        binding.recyclerView.adapter?.notifyItemChanged(position)
    }

    private fun onFloatingPanelAnimationUpdate(progress: Float) {
        val offsetX =
            (binding.legendRoot.width - binding.legendFloatingCardView.left) * (1 - progress)
        binding.legendFloatingCardView.translationX = offsetX
        binding.legendFloatingHandleButton.translationX = offsetX
        binding.legendFloatingHandleArrow.rotation = 180 * progress
    }

    private fun getGroupKeyByPackage(pkgName: String): String {
        return pkgGroupMap[pkgName] ?: UsageStatsGroupInfo.DEFAULT_GROUP_KEY
    }

    private fun onPackageGroupChanged(pkgName: String, groupKey: String) {
        pkgGroupMap[pkgName] = groupKey
        autoSaveTask.cancel()
        autoSaveTask.delay(100L)
    }

    private class AppInfoAdapter(
        private val data: List<PackageUsageHelper.AppInfo>,
        private val groupInfo: List<UsageStatsGroupInfo>,
        private val swapModeProvider: () -> Boolean,
        private val itemClickCallback: (position: Int) -> Unit,
        private val groupKeyProvider: (pkgName: String) -> String,
        private val onGroupChangedCallback: (pkgName: String, groupKey: String) -> Unit
    ) : RecyclerView.Adapter<AppInfoHolder>() {

        private val colorList = ArrayList<Int>()
        private val keyList = ArrayList<String>()

        private var dataVersion = 0

        init {
            updateGroupInfo()
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    updateGroupInfo()
                }
            })
        }

        private fun updateGroupInfo() {
            dataVersion++
            colorList.clear()
            keyList.clear()
            groupInfo.forEach {
                colorList.add(it.color)
                keyList.add(it.key)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoHolder {
            return AppInfoHolder.create(parent, ::onLegendChanged, itemClickCallback)
        }

        override fun onBindViewHolder(holder: AppInfoHolder, position: Int) {
            val appInfo = data[position]
            var legendPosition = keyList.indexOf(groupKeyProvider(appInfo.packageName))
            if (legendPosition < 0) {
                legendPosition = 0
            }
            holder.updateColorInfo(colorList, dataVersion)
            holder.bind(appInfo, legendPosition, swapModeProvider())
        }

        private fun onLegendChanged(holderPosition: Int, legendPosition: Int) {
            onGroupChangedCallback(data[holderPosition].packageName, keyList[legendPosition])
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class AppInfoHolder(
        private val binding: ItemUsageLegendBinding,
        private val onLegendChanged: (holderPosition: Int, legendPosition: Int) -> Unit,
        private val onItemClick: (holderPosition: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(
                parent: ViewGroup,
                legendCallback: (Int, Int) -> Unit,
                clickClick: (holderPosition: Int) -> Unit
            ): AppInfoHolder {
                return AppInfoHolder(parent.bind(), legendCallback, clickClick).apply {
                    itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        }

        private var selectedLegendPosition = -1
        private var colorInfoVersion = -1
        private val colorArray = ArrayList<Int>()
        private val legendDrawable: LegendDrawable = LegendDrawable()
        private var isSwapMode: Boolean = false

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
            binding.legendColorView.setImageDrawable(legendDrawable)
            binding.legendLeftArray.setOnClickListener {
                if (selectedLegendPosition > 0) {
                    callLegendPageChanged(selectedLegendPosition - 1)
                }
            }
            binding.legendRightArray.setOnClickListener {
                if (selectedLegendPosition < colorArray.size - 1) {
                    callLegendPageChanged(selectedLegendPosition + 1)
                }
            }
        }

        private fun onLegendPageChanged(position: Int) {
            if (selectedLegendPosition != position) {
                selectedLegendPosition = position
                onLegendChanged(adapterPosition, position)
            }
        }

        fun updateColorInfo(colorList: List<Int>, version: Int) {
            if (version != colorInfoVersion) {
                colorArray.clear()
                colorArray.addAll(colorList)
                colorInfoVersion = version
            }
        }

        fun bind(
            info: PackageUsageHelper.AppInfo,
            legendPosition: Int,
            isSwapMode: Boolean
        ) {
            selectedLegendPosition = legendPosition
            this.isSwapMode = isSwapMode
            checkArrowButton(isSwapMode)
            legendDrawable.color = getColor(legendPosition)
            binding.appIconView.setImageDrawable(info.icon)
            binding.appLabelView.text = info.label
        }

        private fun getColor(position: Int): Int {
            if (colorArray.isEmpty()) {
                return Color.TRANSPARENT
            }
            if (position < 0 || position >= colorArray.size) {
                return Color.TRANSPARENT
            }
            return colorArray[position]
        }

        private fun checkArrowButton(swapMode: Boolean = isSwapMode) {
            binding.legendLeftArray.visibleOrInvisible(
                swapMode && selectedLegendPosition > 0
            )
            binding.legendRightArray.visibleOrInvisible(
                swapMode && selectedLegendPosition < colorArray.size - 1
            )
        }

        private fun callLegendPageChanged(position: Int) {
            selectedLegendPosition = position
            legendDrawable.color = getColor(position)
            checkArrowButton()
            onLegendPageChanged(position)
        }

    }

    private class FloatingGroupInfoAdapter(
        private val data: List<UsageStatsGroupInfo>,
        private val onItemClick: (position: Int) -> Unit
    ) : RecyclerView.Adapter<FloatingGroupInfoHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FloatingGroupInfoHolder {
            return FloatingGroupInfoHolder.create(parent, onItemClick)
        }

        override fun onBindViewHolder(holder: FloatingGroupInfoHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }


    }

    private class FloatingGroupInfoHolder(
        private val binding: ItemUsageLegendFloatingBinding,
        private val onItemClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(
                parent: ViewGroup,
                clickClick: (holderPosition: Int) -> Unit
            ): FloatingGroupInfoHolder {
                return FloatingGroupInfoHolder(parent.bind(), clickClick).apply {
                    itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        }

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }

        fun bind(info: UsageStatsGroupInfo) {
            binding.groupColorView.setBackgroundColor(info.color)
            binding.groupLabelView.text = info.name
        }

    }

    private class LegendDrawable : Drawable() {

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL_AND_STROKE
        }

        var color: Int = Color.RED
            set(value) {
                if (value != field) {
                    field = value
                    checkShader()
                } else {
                    invalidateSelf()
                }
            }

        private fun checkShader() {
            if (bounds.isEmpty || color == PixelFormat.TRANSPARENT) {
                paint.shader = null
                return
            }
            paint.shader = LinearGradient(
                bounds.left.toFloat(),
                bounds.exactCenterY(),
                bounds.right.toFloat(),
                bounds.exactCenterY(),
                0xFFFFFF,
                color,
                Shader.TileMode.CLAMP
            )
            invalidateSelf()
        }

        override fun onBoundsChange(bounds: Rect?) {
            super.onBoundsChange(bounds)
            checkShader()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRect(bounds, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

}