package com.example.zxingsample.network

import android.net.Uri
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog

class MyChromeClient : WebChromeClient()  {
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        view?.context?.let { ctx ->
            AlertDialog.Builder(ctx)
                .setMessage(message.orEmpty())
                .setPositiveButton("확인") { _, _ ->
                    result?.confirm()
                }
                .setNegativeButton("취소") { _, _ ->
                    result?.cancel()
                }
                .show()
        }

        return false
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {

        return false
    }
}