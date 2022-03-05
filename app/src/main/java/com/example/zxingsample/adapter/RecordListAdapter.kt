package com.example.zxingsample.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zxingsample.R
import com.example.zxingsample.room.RecordEntity
import com.example.zxingsample.view.MainActivity

class RecordListAdapter(private val list: List<RecordEntity>) : RecyclerView.Adapter<RecordListAdapter.RecordListViewHolder>() {
    private val recordList = list.toMutableList()
    class RecordListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvContent: TextView = view.findViewById(R.id.tv_content)
        val llItemLayout: LinearLayout = view.findViewById(R.id.ll_item_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_recent_item, parent, false)
        return RecordListViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordListViewHolder, position: Int) {
        // todo: Need initialize UI
        holder.apply {
            tvTime.text = recordList[position].time
            tvContent.text = recordList[position].data
            llItemLayout.apply {
                setOnClickListener {
                    context.startActivity(Intent(context, MainActivity::class.java).apply {
                        putExtra("RecentRecord", recordList[position].data)
                    })
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return recordList.size
    }
}