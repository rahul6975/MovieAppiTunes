package com.rahul.movieappitunes.utils

import java.text.SimpleDateFormat
import java.util.*

private val DATE_HOUR_FORM by lazy {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
}

fun Date.toStringDate() = this.let(DATE_HOUR_FORM::format)

fun String.toDate() = this.let(DATE_HOUR_FORM::parse)!!

fun Double.toCurrency(code: String): String {
    return "$this $code"
}