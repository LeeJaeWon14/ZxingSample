package com.example.zxingsample.view

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.zxingsample.R
import com.example.zxingsample.databinding.ActivityCreateBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.str_create_qr_label)
        }

        val data = intent.getStringExtra("dataBundle")
        data?.let {
            createQR(binding.ivQr, it)
        } ?: run {
            Toast.makeText(this@CreateActivity, getString(R.string.str_not_found_data), Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                onBackPressed()
            }, 500)
        }
    }

    private fun createQR(imv: ImageView, data: String?) {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200)
            val bitmap = BarcodeEncoder().createBitmap(bitMatrix)
            imv.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // no-op
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                val bitmap = binding.ivQr.drawable.toBitmap()
                val uri = getImageUri(this, bitmap)
                shareIntent.setType("image/*")
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(shareIntent, "Shared QR code"))
            }
            R.id.menu_save -> {
                val bitmap = binding.ivQr.drawable.toBitmap()
                // insertImage() was deprecated. Find the other way.
                MediaStore.Images.Media.insertImage(
                    this.contentResolver,
                    bitmap,
                    intent.getBundleExtra("dataBundle")?.get("data").toString(),
                    ""
                )
                Toast.makeText(this, getString(R.string.str_save_success), Toast.LENGTH_SHORT).show()
            }
            android.R.id.home -> {
                onBackPressed()
            }
        }

        return true
    }

    private fun getImageUri(context: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            context.getContentResolver(),
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }
}