package com.example.zxingsample.view

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebSettings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.zxingsample.R
import com.example.zxingsample.databinding.ActivityMainBinding
import com.example.zxingsample.databinding.LayoutInputDialogBinding
import com.example.zxingsample.network.MyChromeClient
import com.example.zxingsample.network.MyWebViewClient
import com.example.zxingsample.room.MyRoomDatabase
import com.example.zxingsample.room.RecordEntity
import com.example.zxingsample.util.Log
import com.example.zxingsample.util.MyDateUtil
import com.example.zxingsample.util.replaceHttp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity(), DownloadListener {
    private lateinit var binding : ActivityMainBinding

    private lateinit var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>
    private lateinit var recentLauncher: ActivityResultLauncher<Intent>
    private lateinit var safLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initWebView()

        downloadDir = File(getExternalFilesDir(null)?.path.plus("/ZxingSample"))

        checkPermission()

        initActivityLauncher()

        registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        when(intent.action) {
            Intent.ACTION_VIEW -> {
                val uri = intent.data
                uri?.let {
                    val first = it.getQueryParameter("first")
                    val second = it.getQueryParameter("second")

                    Log.e("uri is not null")
                    Log.e("$first / $second")
                    Toast.makeText(this, "$first / $second", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.apply {
            btnQr.setOnClickListener {
                qrInit()
            }
            ivRefresh.setOnClickListener {
                webView.reload()
            }
        }

        checkShare(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let { checkShare(it) }
    }

    private fun checkShare(intent: Intent) {
        if(intent.action == Intent.ACTION_SEND) {
            when (intent.type) {
                "image/*" -> {
                    val uri = intent.clipData?.getItemAt(0)?.uri
                    qrImageParse(uri)
                }
                "text/plain" -> {
                    val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                    startActivity(
                        Intent(
                            this@MainActivity,
                            CreateActivity::class.java
                        ).putExtra("dataBundle", text)
                    )
                }
            }

        }
    }

    override fun onPause() {
        Log.d("onPause()")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d("onDestroy()")
        super.onDestroy()
        unregisterReceiver(downloadCompleteReceiver)
    }

    val uriTest = { uri: Uri ->

    }

    // activityForResult() is deprecated, replace with registerForActivityResult()
    private fun initActivityLauncher() {
//        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia(), qrImageParse)
        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia(), qrImageParse)

//        safLauncher = registerForActivityResult(ActivityResultContracts.GetContent(), qrImageParse)
        safLauncher = registerForActivityResult(ActivityResultContracts.GetContent(), qrImageParse)

        barcodeLauncher = registerForActivityResult(ScanContract()) {
            it.contents?.let { content ->
                processingData(content)
            } ?: run {
                Log.e("QR has no data.")
                Toast.makeText(this, getString(R.string.str_unknown_error), Toast.LENGTH_SHORT).show()
            }
        }

        recentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.apply {
                when(resultCode) {
                    RESULT_OK -> {
                        Log.e("enter ActivityResult")
                        data?.getStringExtra("RecentRecord")?.let { processingData(it) }
                    }
                    else -> {
                        Log.e("Unexpected error!")

                    }
                }
            }
        }
    }

    private val qrImageParse = { uri: Uri? ->
        uri?.let {
            val inputSteam = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputSteam).apply {
                val pixels = IntArray(width * height)
                getPixels(pixels, 0, width, 0, 0, width, height)
                recycle()
                val source = RGBLuminanceSource(width, height, pixels)
                val bBitmap = BinaryBitmap(HybridBinarizer(source))
                val reader = MultiFormatReader()
                runCatching {
                    val result = reader.decode(bBitmap)
//                        Toast.makeText(this@MainActivity, result.text, Toast.LENGTH_SHORT).show()
                    processingData(result.text)

                }.onFailure {
                    Toast.makeText(this@MainActivity, getString(R.string.str_qr_img_parse_error), Toast.LENGTH_SHORT).show()
                }
            }


        } ?: run { Toast.makeText(this@MainActivity, getString(R.string.str_unknown_error), Toast.LENGTH_SHORT).show() }
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
        val permissionList = arrayOf(
            Manifest.permission.CAMERA
        )
        val permissionListener : PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() { //권한 있음
//                Toast.makeText(this@MainActivity, "권한 허용", Toast.LENGTH_SHORT).show()
                // no-op
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { //권한 없음
                Toast.makeText(this@MainActivity, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }
        TedPermission.Builder()
                .setPermissionListener(permissionListener) //Listener set
                .setDeniedMessage(getString(R.string.str_permission_denied_msg)) //DeniedMessage (Do not granted)
                .setPermissions(*permissionList) //Granted
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
                val dlg = MaterialAlertDialogBuilder(this).create().apply {
                    setView(dlgView)
                    setCanceledOnTouchOutside(false)
                    show()
                }

                dlgBinding.apply {
                    btnCreateData.setOnClickListener {
                        if (edtCreateData.text.isEmpty()) {
                            Toast.makeText(this@MainActivity, getString(R.string.str_do_not_empty_input), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        val data = when(rgDataType.checkedRadioButtonId) {
                            R.id.rb_raw -> edtCreateData.text.toString()
                            R.id.rb_url -> "https://${edtCreateData.text.toString()}"
                            else -> ""
                        }
                        startActivity(
                            Intent(
                                this@MainActivity,
                                CreateActivity::class.java
                            ).putExtra("dataBundle", data)
                        )
                        dlg.dismiss()
                    }
                }
            }
            R.id.menu_read_image -> {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // PhotoPicker 출력
                    photoPickerLauncher.launch(PickVisualMediaRequest())
                } else {
                    // SAF 출력
                    safLauncher.launch("image/*")
                }
            }
            R.id.menu_recent -> {
                recentLauncher.launch(Intent(this, RecentActivity::class.java))
            }
            R.id.menu_download_list -> {
                val fileList = mutableListOf<String>()
                if(!downloadDir.exists()) {
                    Log.e("$downloadDir >> Not found directory..")
                    return true
                } else {
                    Log.e("$downloadDir >> found..\n${downloadDir.listFiles() ?: "no files"}")
                }
                downloadDir.list()?.forEach {
//                    val file = File(downloadDir, it.path).also { Log.e("abstract file >> $it") }
//
//                    Log.e("file >> ${it.name}")
//                    fileList.add(it.name)
                    Log.e("file name >> $it")
                }
//                AlertDialog.Builder(this)
//                    .setItems(fileList.toTypedArray()) { _, idx ->
//                        File(getExternalFilesDir(null)?.path.plus("/ZxingSample/").plus(fileList[idx]))
//                            .run {
//                                Toast.makeText(this@MainActivity, "$name, ${length()}", Toast.LENGTH_SHORT).show()
//                            }
//                    }
//                    .setPositiveButton("닫기", null)
//                    .show()
            }
        }

        return true
    }

    private var time : Long = 0
    override fun onBackPressed() {
        Log.d("onBackPressed()")
        if(binding.webView.canGoBack()) {
            binding.webView.goBack()
        }
        else {
            if(System.currentTimeMillis() - time >= 2000) {
                time = System.currentTimeMillis()
                Toast.makeText(this@MainActivity, getString(R.string.str_one_more_press_exit), Toast.LENGTH_SHORT).show()
            }
            else if(System.currentTimeMillis() - time < 2000) {
                Log.d("Finish the app")
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
        Log.e("processingData()")
        Log.e("content >> $content")
        saveRecord(content)
        try {
            if(content.startsWith("https://")) {
                binding.apply {
                    webView.visibility = View.VISIBLE
                    llViewUrl.visibility = View.VISIBLE
                    tvQrResult.visibility = View.GONE
                }

                loadUrl(content)
            }

            else if(content.startsWith("http://")) {
                binding.apply {
                    webView.visibility = View.VISIBLE
                    llViewUrl.visibility = View.VISIBLE
                    tvQrResult.visibility = View.GONE
                }

                loadUrl(content.replaceHttp())
            }
            else {
                binding.apply {
                    webView.visibility = View.GONE
                    llViewUrl.visibility = View.GONE
                    tvQrResult.visibility = View.VISIBLE
                    tvQrResult.text = content
                }
            }
        } catch (e: Exception) {
            when (e.message) {
                "This url is not supported ssl" -> Toast.makeText(this, getString(R.string.str_not_allowed_http), Toast.LENGTH_SHORT).show()
                else -> {
                    Log.e("data error! >> $e")
                    Toast.makeText(this, getString(R.string.str_data_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initWebView() {
        Log.e("initWebView()")
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setSupportZoom(true)
                builtInZoomControls = true
                textZoom = 95
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            webViewClient = MyWebViewClient(binding.progressBar, binding.tvUrl)
            webChromeClient = MyChromeClient()
            setDownloadListener(this@MainActivity)
        }
    }

    private fun loadUrl(url: String) = binding.webView.loadUrl(url)

    private fun sslCheck(url: String) : Boolean = url.split("://")[0] == "https"

    override fun onDownloadStart(url: String?, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long) {
        Log.e("""
            
            onDownloadStart()
            - url: $url
            - UserAgent: $userAgent
            - Disposition: $contentDisposition
            - Length: $contentLength
        """.trimIndent())
        Toast.makeText(this, "다운로드를 시작합니다.", Toast.LENGTH_SHORT).show()

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        if(!downloadDir.exists()) downloadDir.mkdirs()

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("뿌슝빠슝.. 다운로드..")
            setDestinationUri(Uri.fromFile(downloadDir))
            setAllowedOverMetered(true)
            setNotificationVisibility(View.VISIBLE)
        }
        mDownloadQueueId = downloadManager.enqueue(request)
        Log.e("Download start!!! >> path: ${downloadDir.path}")
    }

    private lateinit var downloadDir: File
    private lateinit var downloadManager: DownloadManager
    private var mDownloadQueueId: Long = 0
    private val downloadCompleteReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("downloadCompleteReceiver.. ")
            val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (mDownloadQueueId == reference) {
                val query = DownloadManager.Query() // 다운로드 항목 조회에 필요한 정보 포함
                query.setFilterById(reference)
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                val columnIndex: Int = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val columnReason: Int = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val status: Int = cursor.getInt(columnIndex)
                val reason: Int = cursor.getInt(columnReason)
                cursor.close()
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> Toast.makeText(
                        this@MainActivity,
                        getString(R.string.str_download_done),
                        Toast.LENGTH_SHORT
                    ).show()
                    DownloadManager.STATUS_PAUSED -> Toast.makeText(
                        this@MainActivity,
                        getString(R.string.str_download_pause),
                        Toast.LENGTH_SHORT
                    ).show()
                    DownloadManager.STATUS_FAILED -> Toast.makeText(
                        this@MainActivity,
                        getString(R.string.str_download_cancel),
                        Toast.LENGTH_SHORT
                    ).show()
                    DownloadManager.STATUS_RUNNING -> Toast.makeText(
                        this@MainActivity,
                        "다운로드를 진행 중입니다..",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}