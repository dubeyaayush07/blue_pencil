package com.example.bluepencil

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun getCurrencyString(num: Int): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.setMaximumFractionDigits(0)
    format.setCurrency(Currency.getInstance("INR"))

    return format.format(num)
}

fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy")
    return formatter.format(date)
}