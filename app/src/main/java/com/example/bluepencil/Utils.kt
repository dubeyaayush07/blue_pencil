package com.example.bluepencil

import java.text.NumberFormat
import java.util.Currency

fun getCurrencyString(num: Int): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.setMaximumFractionDigits(0)
    format.setCurrency(Currency.getInstance("INR"))

    return format.format(num)
}