package com.example.zxingsample.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.zxingsample.R
import com.journeyapps.barcodescanner.CaptureActivity

/**
 * This activity is an empty activity for screen portrait
 */
class EmptyActivity : CaptureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        setContentView()


        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_capture, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> { onBackPressed() }
            R.menu.menu_capture -> {  }
        }
        return true
    }
}