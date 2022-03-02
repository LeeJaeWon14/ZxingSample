package com.example.zxingsample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zxingsample.R

class RecordListAdapter(private val list: List<*>) : RecyclerView.Adapter<RecordListAdapter.RecordListViewHolder>() {
    class RecordListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime = view.findViewById<TextView>(R.id.tv_time)
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_recent_item, parent, false)
        return RecordListViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordListViewHolder, position: Int) {
        // todo: Need initialize UI
        holder.apply {
            tvTime.text = ""
            tvContent.text = ""
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}