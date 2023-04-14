package com.example.zxingsample.network

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.zxingsample.R
import com.example.zxingsample.util.Log

class MyWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        super.shouldOverrideUrlLoading(view, request)
        Log.e("onShouldOverrideUrlLoading()")
        return if (view?.url?.startsWith("https") == true) false
        else {
            Toast.makeText(view?.context, view?.context?.getString(R.string.str_not_allowed_http), Toast.LENGTH_SHORT).show()
            true
        }
    }
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Log.e("onPageStarted()")
        Log.e("page start >> $url")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.e("onPageFinished()")
        Log.e("page finish >> $url")
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

//        view?.url?.let { url ->
//            // todo: It will separate to static method.
//            if(url.startsWith("http")) {
//                val domain = url.split("://")[1]
//                view.loadUrl("https://".plus(domain))
//                return
//            }
//        }


    }
}