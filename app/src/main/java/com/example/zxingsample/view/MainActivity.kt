package com.example.zxingsample.view

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.zxingsample.R
import com.example.zxingsample.databinding.ActivityMainBinding
import com.example.zxingsample.databinding.LayoutInputDialogBinding
import com.example.zxingsample.room.MyRoomDatabase
import com.example.zxingsample.room.RecordEntity
import com.example.zxingsample.util.Log
import com.example.zxingsample.util.MyDateUtil
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        checkPermission()
        intent.getStringExtra("RecentRecord")?.let {
            processingData(it)
        }
        binding.btnQr.setOnClickListener {
            qrInit()
        }
    }

    // activityForResult() is deprecated, replace with registerForActivityResult()
    private val  barcodeLauncher = registerForActivityResult(ScanContract()) {
        it.contents?.let { content ->
            processingData(content)
        } ?: run {
            Log.e("QR has no data.")
            Toast.makeText(this, getString(R.string.str_unknown_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun qrInit() {
        val qrOptions = ScanOptions().apply {
            setPrompt(getString(R.string.str_qr_prompt))
            setBeepEnabled(false)
            captureActivity = EmptyActivity::class.java
        }
        barcodeLauncher.launch(qrOptions)
    }

    //permission check
    //출처 : https://github.com/ParkSangGwon/TedPermission
    private fun checkPermission() {
        val permissionListener : PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() { //권한 있음
//                Toast.makeText(this@MainActivity, "권한 허용", Toast.LENGTH_SHORT).show()
                // no-op
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { //권한 없음
                Toast.makeText(this@MainActivity, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }
        TedPermission.with(this)
                .setPermissionListener(permissionListener) //Listener set
                .setDeniedMessage(getString(R.string.str_permission_denied_msg)) //DeniedMessage (Do not granted)
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE) //Granted
                .check()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_more, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_create -> {
                val dlgBinding = LayoutInputDialogBinding.inflate(layoutInflater)
                val dlgView = dlgBinding.root
                val dlg = AlertDialog.Builder(this).create()
                dlg.setView(dlgView)

                dlgBinding.apply {
                    btnCreateData.setOnClickListener {
                        if (edtCreateData.text.toString() == "") {
                            Toast.makeText(this@MainActivity, getString(R.string.str_do_not_empty_input), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        val data = when(rgDataType.checkedRadioButtonId) {
                            R.id.rb_raw -> edtCreateData.text.toString()
                            R.id.rb_url -> "https://${edtCreateData.text.toString()}"
                            else -> ""
                        }
                        val bundle = Bundle()
                        bundle.putString("data", data)
                        startActivity(
                            Intent(
                                this@MainActivity,
                                CreateActivity::class.java
                            ).putExtra("dataBundle", bundle)
                        )
                        dlg.dismiss()
                    }
                }

                dlg.show()
            }
            R.id.menu_recent -> {
                startActivity(Intent(this, RecentActivity::class.java))
            }
        }

        return true
    }

    private var time : Long = 0
    override fun onBackPressed() {
        if(binding.webView.canGoBack()) {
            binding.webView.goBack()
        }
        else {
            if(System.currentTimeMillis() - time >= 2000) {
                time = System.currentTimeMillis()
                Toast.makeText(this@MainActivity, getString(R.string.str_one_more_press_exit), Toast.LENGTH_SHORT).show()
            }
            else if(System.currentTimeMillis() - time < 2000) {
                this.finishAffinity()
            }
        }
    }

    private fun saveRecord(content: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val entity = RecordEntity(
                0,
                MyDateUtil.getDate(MyDateUtil.HANGUEL),
                content
            )
            MyRoomDatabase.getInstance(this@MainActivity).getRoomDAO()
                .insertRecord(entity)
        }
    }

    private fun processingData(content: String) {
        saveRecord(content)
        try {
            if(content.contains("http")) {
                binding.apply {
                    webView.visibility = View.VISIBLE
                    tvQrResult.visibility = View.GONE
                }
                //webView
                binding.webView.apply {
                    webViewClient = WebViewClient()
                    settings.apply {
                        javaScriptEnabled = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        setSupportZoom(true)
                        builtInZoomControls = true
                    }
                }
                binding.webView.loadUrl(content)
            }
            else {
                binding.apply {
                    webView.visibility = View.GONE
                    tvQrResult.visibility = View.VISIBLE
                    tvQrResult.text = content
                }
            }
        } catch (e: Exception) {
            Log.e("data error!")
            Toast.makeText(this, getString(R.string.str_data_error), Toast.LENGTH_SHORT).show()
        }
    }
}