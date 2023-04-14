package com.example.zxingsample.adapter

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zxingsample.databinding.LayoutRecentItemBinding
import com.example.zxingsample.room.RecordEntity
import com.example.zxingsample.util.Log
import com.example.zxingsample.view.MainActivity
import com.example.zxingsample.view.RecentActivity

class RecordListAdapter(private val list: List<RecordEntity>) : RecyclerView.Adapter<RecordListAdapter.RecordListViewHolder>() {
    private val recordList get() = list.toMutableList().sortedByDescending { it.time }

    class RecordListViewHolder(private val binding: LayoutRecentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: RecordEntity) {
            binding.apply {
                tvTime.text = record.time
                tvContent.text = record.data
                llItemLayout.apply {
                    setOnClickListener {
                        if(context is RecentActivity) Log.e("this activity is RecentActivity")
                        (context as Activity).run {
                            setResult(
                                RESULT_OK,
                                Intent(context, MainActivity::class.java).apply {
                                    putExtra("RecentRecord", record.data)
                                }
                            )
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordListViewHolder {
        val binding = LayoutRecentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordListViewHolder, position: Int) {
        holder.apply {
            bind(list[position])
        }
    }

    override fun getItemCount(): Int {
        return recordList.size
    }
}