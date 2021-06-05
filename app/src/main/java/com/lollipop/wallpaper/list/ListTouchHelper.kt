package com.lollipop.wallpaper.list

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * @author lollipop
 * @date 2021/6/5 21:29
 */
class ListTouchHelper private constructor() :
    ItemTouchHelper.Callback() {

    companion object {
        fun create(): ListTouchHelper {
            return ListTouchHelper()
        }
    }

    private val moveDirection = Direction()

    private val swipeDirection = Direction()

    private var onItemMoveCallback: OnItemMoveCallback? = null

    private var onItemSwipedCallback: OnItemSwipedCallback? = null

    fun setMoveDirection(directionCallback: Direction.() -> Unit): ListTouchHelper {
        directionCallback(moveDirection)
        return this
    }

    fun setSwipeDirection(directionCallback: Direction.() -> Unit): ListTouchHelper {
        directionCallback(swipeDirection)
        return this
    }

    fun <T> autoMoveWithList(data: MutableList<T>): ListTouchHelper {
        return onMove(SimpleMoveImpl(data))
    }

    fun onMove(callback: OnItemMoveCallback?): ListTouchHelper {
        this.onItemMoveCallback = callback
        return this
    }

    fun onSwiped(callback: OnItemSwipedCallback?): ListTouchHelper {
        this.onItemSwipedCallback = callback
        return this
    }

    fun bindTo(recyclerView: RecyclerView) {
        ItemTouchHelper(this).attachToRecyclerView(recyclerView)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        var dragFlag = 0
        if (moveDirection.start) {
            dragFlag = dragFlag or ItemTouchHelper.START
        }
        if (moveDirection.up) {
            dragFlag = dragFlag or ItemTouchHelper.UP
        }
        if (moveDirection.end) {
            dragFlag = dragFlag or ItemTouchHelper.END
        }
        if (moveDirection.down) {
            dragFlag = dragFlag or ItemTouchHelper.DOWN
        }

        var swipeFlag = 0
        if (swipeDirection.start) {
            swipeFlag = swipeFlag or ItemTouchHelper.START
        }
        if (swipeDirection.up) {
            swipeFlag = swipeFlag or ItemTouchHelper.UP
        }
        if (swipeDirection.end) {
            swipeFlag = swipeFlag or ItemTouchHelper.END
        }
        if (swipeDirection.down) {
            swipeFlag = swipeFlag or ItemTouchHelper.DOWN
        }
        return makeMovementFlags(dragFlag, swipeFlag)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return onItemMoveCallback?.onMove(
            recyclerView,
            viewHolder.adapterPosition,
            target.adapterPosition
        ) ?: false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onItemSwipedCallback?.onSwiped(
            viewHolder.adapterPosition,
            Direction(
                start = direction == ItemTouchHelper.START,
                up = direction == ItemTouchHelper.UP,
                end = direction == ItemTouchHelper.END,
                down = direction == ItemTouchHelper.DOWN
            )
        )
    }

    class Direction(
        var start: Boolean = false,
        var up: Boolean = false,
        var end: Boolean = false,
        var down: Boolean = false,
    )

    fun interface OnItemMoveCallback {
        fun onMove(
            recyclerView: RecyclerView,
            fromPosition: Int,
            toPosition: Int
        ): Boolean
    }

    fun interface OnItemSwipedCallback {
        fun onSwiped(position: Int, direction: Direction)
    }

    class SimpleMoveImpl<T>(
        private val data: MutableList<T>
    ): OnItemMoveCallback {
        override fun onMove(
            recyclerView: RecyclerView,
            fromPosition: Int,
            toPosition: Int
        ): Boolean {
            val temp = data[fromPosition]
            data[fromPosition] = data[toPosition]
            data[toPosition] = temp
            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
            return true
        }
    }

}