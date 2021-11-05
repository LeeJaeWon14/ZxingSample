package com.example.zxingsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import com.example.zxingsample.databinding.ActivityCreateBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.lang.Exception

class CreateActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.getBundleExtra("dataBundle")
        bundle?.let {
            createQR(binding.ivQr, it.getString("data")!!)
        } ?: run {
            Toast.makeText(this@CreateActivity, "Data를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                onBackPressed()
            }, 2000)
        }
    }

    private fun createQR(imv: ImageView, data: String) {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200)
            val bitmap = BarcodeEncoder().createBitmap(bitMatrix)
            imv.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // no-op
        }
    }
}