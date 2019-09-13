package com.ayvytr.swipelayoutapp

import androidx.ayvytr.adapter.SmartAdapter
import androidx.ayvytr.adapter.SmartViewHolder
import androidx.recyclerview.widget.RecyclerView
import com.ayvytr.swipelayout.SwipeLayout
import kotlinx.android.synthetic.main.item_type_0.view.*
import org.jetbrains.anko.toast

/**
 * @author admin
 */
class SwipeAdapter(list: MutableList<SwipeBean>, rv: RecyclerView) :
    SmartAdapter<SwipeBean>(list, rv) {
    init {
        map(R.layout.item_type_0, 0) {
            swipe_layout.offset = it.offset
            swipe_layout.isSwipeEnabled = true
            tv.text = it.text

            tv_left.setOnClickListener {
                context.toast(tv_left.text)
            }

            tv_right.setOnClickListener {
                context.toast(tv_right.text)
            }
        }
        map(R.layout.item_type_0, 1) {
            swipe_layout.offset = it.offset
            swipe_layout.isRightSwipeEnabled = false
            tv.text = it.text

            tv_left.setOnClickListener { context.toast(tv_left.text) }
        }
        map(R.layout.item_type_0, 2) {
            swipe_layout.offset = it.offset
            swipe_layout.isLeftSwipeEnabled = false
            tv.text = it.text

            tv_right.setOnClickListener {
                context.toast(tv_right.text)
            }
        }
        type {
            it.type
        }
    }

    override fun onBindViewHolder(holder: SmartViewHolder<SwipeBean>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.findViewById<SwipeLayout>(R.id.swipe_layout).offset = list[position].offset
        holder.itemView.setOnClickListener {
            rv.context.toast("点击$position")
        }
    }

    override fun onViewDetachedFromWindow(holder: SmartViewHolder<SwipeBean>) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            list[position].offset = holder.itemView.findViewById<SwipeLayout>(R.id.swipe_layout).offset
        }
    }
}
