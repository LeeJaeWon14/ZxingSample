package com.example.zxingsample.network

import android.net.Uri
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import com.example.zxingsample.util.DialogHelper
import com.example.zxingsample.util.Log

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

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        view?.context?.let { ctx ->
            AlertDialog.Builder(ctx)
                .setMessage(message)
                .setPositiveButton("확인") { _, _ ->
                    result?.confirm()
                }
                .setNegativeButton("취소") { _, _ ->
                    result?.confirm()
                }
        }

        return super.onJsConfirm(view, url, message, result)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        Log.e(String.format(
            "%s (Line: %s) : %s",
            consoleMessage?.sourceId(),
            consoleMessage?.lineNumber(),
            consoleMessage?.message()
        ))
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {

        return false
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        view?.context?.let {
            if(!DialogHelper.progressDialog(it).isShowing)
                DialogHelper.progressDialog(it)

            if(newProgress == 100)
                DialogHelper.progressDialog(it).dismiss()
        }
    }
}