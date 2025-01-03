package com.example.zxingsample.network

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.zxingsample.util.Log
import com.example.zxingsample.util.replaceHttp

class MyWebViewClient(val progressBar: ProgressBar, val tvUrl: TextView) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        Log.e("onShouldOverrideUrlLoading()")
//        return super.shouldOverrideUrlLoading(view, request)

        return if (view?.url?.startsWith("http://") == true) {
            view.url?.replaceHttp()?.let { view.loadUrl(it) }
            true
        }
        else false
    }
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Log.e("onPageStarted()")
        Log.e("page start >> $url")

        progressBar.isVisible = true
        tvUrl.text = url
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.e("onPageFinished()")
        Log.e("page finish >> $url")

        progressBar.isVisible = false
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        Log.e("onReceivedError()")
        error?.let { err ->
            Log.e("""
                
                Received error from web! >>
                code: ${err.errorCode}
                desc: ${err.description}
            """.trimIndent())
        }
    }
}