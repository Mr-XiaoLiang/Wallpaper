package com.lollipop.wallpaper.generate

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.lollipop.wallpaper.R
import com.lollipop.wallpaper.databinding.FragmentGenerateGroupingBinding
import com.lollipop.wallpaper.databinding.ItemAppInGroupBinding
import com.lollipop.wallpaper.databinding.ItemColorGroupBinding
import com.lollipop.wallpaper.entitys.AppInfo
import com.lollipop.wallpaper.entitys.GroupInfo
import com.lollipop.wallpaper.list.ViewBindingHolder
import com.lollipop.wallpaper.utils.*

/**
 * 色彩生成的分组页面
 * 将会把生成的颜色进行合并及分组
 * @author Lollipop
 * @date 2021/07/03
 */
class GenerateGroupingFragment : GenerateBaseFragment() {

    private val binding: FragmentGenerateGroupingBinding by lazyBind()

    private var callback: Callback? = null

    override val nextStepAction: Int
        get() = R.id.actionGroupingToComplete

    override val contentViewBinding: ViewBinding
        get() = binding

    private val groupInfoList = ArrayList<GroupInfo>()

    private val adapter = GroupAdapter(groupInfoList, ::changeColor, ::changeLabel)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
        binding.groupListView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.groupListView.adapter = adapter
        PagerSnapHelper().attachToRecyclerView(binding.groupListView)
        binding.nextBtn.setOnClickListener {
            nextStep()
        }
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun loadData() {
        startLoading()
        binding.nextBtn.hide()
        groupInfoList.clear()
        adapter.notifyDataSetChanged()
        callback?.getGroupingInfo { groupList ->
            groupInfoList.clear()
            groupInfoList.addAll(groupList)
            adapter.notifyDataSetChanged()
            endLoading()
            binding.nextBtn.show()
        }
    }

    private fun changeColor(position: Int) {}

    private fun changeLabel(position: Int) {}

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
        fun getGroupingInfo(callback: (List<GroupInfo>) -> Unit)
    }

    private class GroupAdapter(
        private val data: List<GroupInfo>,
        private val callChangeColor: (Int) -> Unit,
        private val callChangeLabel: (Int) -> Unit
    ) : RecyclerView.Adapter<GroupHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
            return GroupHolder.create(parent, callChangeColor, callChangeLabel)
        }

        override fun onBindViewHolder(holder: GroupHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class GroupHolder(
        private val binding: ItemColorGroupBinding,
        private val callChangeColor: (Int) -> Unit,
        private val callChangeLabel: (Int) -> Unit
    ) : ViewBindingHolder(binding) {

        companion object {
            fun create(
                parent: ViewGroup,
                callChangeColor: (Int) -> Unit,
                callChangeLabel: (Int) -> Unit
            ): GroupHolder {
                return GroupHolder(parent.bind(true), callChangeColor, callChangeLabel)
            }
        }

        private val appList = ArrayList<AppInfo>()
        private val adapter = AppAdapter(appList)

        init {
            binding.appListView.adapter = adapter
            binding.appListView.layoutManager = GridLayoutManager(
                context, 3, RecyclerView.VERTICAL, false
            )
            binding.colorPreviewView.setOnClickListener {
                callChangeColor(adapterPosition)
            }
            binding.groupLabelView.setOnClickListener {
                callChangeLabel(adapterPosition)
            }
        }

        fun bind(info: GroupInfo) {
            binding.groupLabelView.text = info.label
            binding.colorPreviewView.setBackgroundColor(info.color)
            appList.clear()
            appList.addAll(info.appList)
            adapter.notifyDataSetChanged()
        }

    }

    private class AppAdapter(
        private val data: List<AppInfo>
    ) : RecyclerView.Adapter<AppHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            return AppHolder.create(parent)
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            holder.bind(data[position].icon)
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class AppHolder(
        private val binding: ItemAppInGroupBinding
    ) : ViewBindingHolder(binding) {

        companion object {
            fun create(parent: ViewGroup): AppHolder {
                return AppHolder(parent.bind(true))
            }
        }

        fun bind(icon: Drawable) {
            binding.appIconView.setImageDrawable(icon)
        }

    }

}