package com.example.zxingsample.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zxingsample.R
import com.example.zxingsample.adapter.RecordListAdapter
import com.example.zxingsample.databinding.ActivityRecentBinding
import com.example.zxingsample.room.MyRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RecentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            getString(R.string.recent_title)
        }

        val selectRoutine = CoroutineScope(Dispatchers.IO).async {
            MyRoomDatabase.getInstance(this@RecentActivity).getRoomDAO()
                .selectRecord()
        }

        binding.apply {
            rvRecentList.apply {
                CoroutineScope(Dispatchers.Main).launch {
                    selectRoutine.await().also {
                        if(it.isEmpty()) {
                            hideList()
                        }
                        else {
                            layoutManager = LinearLayoutManager(this@RecentActivity)
                            adapter = RecordListAdapter(it)
                        }
                    }
                }
            }
        }
    }

    private fun hideList() {
        binding.apply {
            rlRecordList.isVisible = false
            tvNoData.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> { onBackPressed() }
        }
        return true
    }
}