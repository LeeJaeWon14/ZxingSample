package com.example.zxingsample

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.zxingsample.databinding.ActivityMainBinding
import com.example.zxingsample.databinding.LayoutInputDialogBinding
import com.example.zxingsample.util.MyLogger
import com.google.zxing.integration.android.IntentIntegrator
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var qrIntent : IntentIntegrator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        checkPermission()
        binding.btnQr.setOnClickListener {
            qrInit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.let {
            it.contents?.let { content ->
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
                                builtInZoomControls = true
                                setSupportZoom(true)
                            }
                        }
                        binding.webView.loadUrl(content)
                    }
                    else {
//                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        binding.apply {
                            webView.visibility = View.GONE
                            tvQrResult.visibility = View.VISIBLE
                            tvQrResult.text = content
                        }
                    }
                } catch (e: Exception) {
                    MyLogger.e("data error!")
                    Toast.makeText(this, getString(R.string.str_data_error), Toast.LENGTH_SHORT).show()
                }
            } ?: {
                MyLogger.e("QR has no data.")
                Toast.makeText(this, getString(R.string.str_unknown_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun qrInit() {
        qrIntent = IntentIntegrator(this)
        qrIntent.setPrompt(getString(R.string.str_qr_prompt))
        qrIntent.captureActivity = EmptyActivity::class.java
//        qrIntent.setOrientationLocked(true)
        qrIntent.initiateScan()
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
                        val data = when(dlgBinding.rgDataType.checkedRadioButtonId) {
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
}