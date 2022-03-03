package com.example.zxingsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zxingsample.databinding.ActivityRecentBinding

class RecentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            rvRecentList.apply {
                layoutManager = LinearLayoutManager(this@RecentActivity)
//                adapter = RecordListAdapter()
            }
        }
    }
}