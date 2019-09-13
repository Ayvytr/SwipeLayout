package com.ayvytr.swipelayoutapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ayvytr.adapter.SmartAdapter
import kotlinx.android.synthetic.main.activity_swipe_recycler.*

class SwipeRecyclerActivity : AppCompatActivity() {
    private lateinit var smartAdapter: SmartAdapter<SwipeBean>
    private var list = mutableListOf<SwipeBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_recycler)
        initView()
    }

    private fun initView() {
        for (i in 0 until 30) {
            when (i % 3) {
                0 -> list.add(SwipeBean("左右侧滑"))
                1 -> list.add(SwipeBean("左边侧滑", 1))
                2 -> list.add(SwipeBean("右边侧滑", 2))
            }
        }

        rv.adapter = SwipeAdapter(list, rv)
    }
}
