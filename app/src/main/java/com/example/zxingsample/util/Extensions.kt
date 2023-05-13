package com.example.zxingsample.util

fun String.replaceHttp() : String {
    val tempUrl = this.substring(4, length)
    return "https".plus(tempUrl)
}