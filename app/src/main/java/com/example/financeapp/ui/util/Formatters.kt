package com.example.financeapp.ui.util

import com.example.financeapp.viewmodel.YearMonth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val spanishLocale = Locale.forLanguageTag("es-MX")

private val currencyFormat = NumberFormat.getCurrencyInstance(spanishLocale)
private val dateFormat = SimpleDateFormat("d MMM yyyy", spanishLocale)
private val monthYearFormat = SimpleDateFormat("MMMM yyyy", spanishLocale)

fun formatCurrency(amount: Double): String = currencyFormat.format(amount)

fun formatDate(timestamp: Long): String = dateFormat.format(timestamp)

fun formatMonthYear(yearMonth: YearMonth): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, yearMonth.year)
        set(Calendar.MONTH, yearMonth.month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return monthYearFormat.format(cal.time).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString()
    }
}

fun startOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun endOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis
