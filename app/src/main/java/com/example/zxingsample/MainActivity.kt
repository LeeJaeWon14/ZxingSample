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
            it.contents?.let {
                try {
                    if(it.contains("http")) {
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
                        binding.webView.loadUrl(it)
                    }
                    else {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    MyLogger.e("data error!")
                }
            } ?: {
                MyLogger.e("QR has no data.")
            }
        }
    }

    private fun qrInit() {
        qrIntent = IntentIntegrator(this)
        qrIntent.setPrompt(getString(R.string.str_qr_prompt))
//        qrIntent.setCaptureActivity()
        qrIntent.setOrientationLocked(true)
        qrIntent.initiateScan()
    }

    //permission check
    //출처 : https://github.com/ParkSangGwon/TedPermission
    private fun checkPermission() {
        val permissionListener : PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() { //권한 있음
                Toast.makeText(this@MainActivity, "권한 허용", Toast.LENGTH_SHORT).show()
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { //권한 없음
                Toast.makeText(this@MainActivity, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }
        TedPermission.with(this)
                .setPermissionListener(permissionListener) //Listener set
                .setDeniedMessage(getString(R.string.str_permission_denied_msg)) //DeniedMessage (Do not granted)
                .setPermissions(Manifest.permission.CAMERA) //Granted
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
                        val bundle = Bundle()
                        bundle.putString("data", edtCreateData.text.toString())
                        startActivity(Intent(this@MainActivity, CreateActivity::class.java).putExtra("dataBundle", bundle))
                    }
                }

                dlg.show()
            }
        }

        return true
    }
//
//    override fun onBackPressed() {
//        if(binding.webView.canGoBack()) {
//            binding.webView.goBack()
//        }
//        else {
//            finish()
//        }
//    }
}