package com.lollipop.wallpaper.activity

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.ActivityLegendBinding
import com.lollipop.wallpaper.databinding.ItemUsageLegendBinding
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
        get() = R.menu.activity_legent_ment

    override val guideLayoutId: Int
        get() = R.layout.guide_legend

    private val binding: ActivityLegendBinding by lazyBind()
    private val settings = LSettings.bind(this)
    private val packageUsageHelper = PackageUsageHelper(this)

    private val groupInfoList = ArrayList<UsageStatsGroupInfo>()
    private val pkgGroupMap = HashMap<String, String>()
    private val appInfoList = ArrayList<PackageUsageHelper.AppInfo>()

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
            ::getGroupKeyByPackage,
            ::onPackageGroupChanged
        )
        binding.recyclerView.applyWindowInsetsByPadding(
            enableTop = false
        )
    }

    private fun refresh() {
        binding.swipeRefreshLayout.isRefreshing = true
        appInfoList.clear()
        binding.recyclerView.adapter?.notifyDataSetChanged()
        doAsync {
            packageUsageHelper.loadAppInfo()
            appInfoList.addAll(packageUsageHelper.appInfoList)

            groupInfoList.clear()
            groupInfoList.addAll(settings.getGroupInfo())

            val packageInfo = settings.getPackageInfo()
            pkgGroupMap.clear()
            packageInfo.forEach {
                pkgGroupMap[it.packageName] = it.groupKey
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

    private fun getGroupKeyByPackage(pkgName: String): String {
        return pkgGroupMap[pkgName] ?: UsageStatsGroupInfo.DEFAULT_GROUP_KEY
    }

    private fun onPackageGroupChanged(pkgName: String, groupKey: String) {
        pkgGroupMap[pkgName] = groupKey
    }

    private class AppInfoAdapter(
        private val data: List<PackageUsageHelper.AppInfo>,
        private val groupInfo: List<UsageStatsGroupInfo>,
        private val groupKeyProvider: (pkgName: String) -> String,
        private val onGroupChangedCallback: (pkgName: String, groupKey: String) -> Unit
    ) : RecyclerView.Adapter<AppInfoHolder>() {

        private val colorList = ArrayList<Int>()
        private val keyList = ArrayList<String>()

        init {
            updateGroupInfo()
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    updateGroupInfo()
                }
            })
        }

        fun updateGroupInfo() {
            colorList.clear()
            keyList.clear()
            groupInfo.forEach {
                colorList.add(it.color)
                keyList.add(it.key)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoHolder {
            return AppInfoHolder.create(parent, ::onLegendChanged)
        }

        override fun onBindViewHolder(holder: AppInfoHolder, position: Int) {
            val appInfo = data[position]
            var legendPosition = keyList.indexOf(groupKeyProvider(appInfo.packageName))
            if (legendPosition < 0) {
                legendPosition = 0
            }
            holder.bind(appInfo, colorList, legendPosition)
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
        private val onLegendChanged: (holderPosition: Int, legendPosition: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(parent: ViewGroup, callback: (Int, Int) -> Unit): AppInfoHolder {
                return AppInfoHolder(parent.bind(), callback).apply {
                    itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        }

        private val colorArray = ArrayList<Int>()

        private var selectedLegendPosition = -1

        init {
            binding.legendViewPager.adapter = LegendPagerAdapter(colorArray)
            binding.legendViewPager.orientation = RecyclerView.HORIZONTAL
            binding.legendViewPager.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        onLegendPageChanged(position)
                    }
                })
        }

        private fun onLegendPageChanged(position: Int) {
            if (selectedLegendPosition != position) {
                selectedLegendPosition = position
                onLegendChanged(adapterPosition, position)
            }
        }

        fun bind(
            info: PackageUsageHelper.AppInfo,
            colorList: List<Int>,
            legendPosition: Int
        ) {
            selectedLegendPosition = legendPosition

            colorArray.clear()
            colorArray.addAll(colorList)
            binding.legendViewPager.adapter?.notifyDataSetChanged()
            binding.legendViewPager.setCurrentItem(legendPosition, false)
            binding.appIconView.setImageDrawable(info.icon)
            binding.appLabelView.text = info.label
        }

    }

    private class LegendPagerAdapter(
        private val colorArray: List<Int>
    ) : RecyclerView.Adapter<LegendPagerHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendPagerHolder {
            return LegendPagerHolder.create(parent)
        }

        override fun onBindViewHolder(holder: LegendPagerHolder, position: Int) {
            holder.bind(colorArray[position])
        }

        override fun getItemCount(): Int {
            return colorArray.size
        }

    }

    private class LegendPagerHolder(view: ImageView) : RecyclerView.ViewHolder(view) {

        companion object {
            fun create(parent: ViewGroup): LegendPagerHolder {
                return LegendPagerHolder(ImageView(parent.context)).apply {
                    itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        }

        private val legendDrawable: LegendDrawable = LegendDrawable()

        init {
            view.setImageDrawable(legendDrawable)
        }

        fun bind(color: Int) {
            legendDrawable.color = color
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