package com.todohub.app.util

import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {
    private val dateFmt = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun todayStart(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    fun todayEnd(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return cal.time
    }

    fun weekStart(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    fun weekEnd(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek + 6)
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return cal.time
    }

    fun formatDate(millis: Long): String = dateFmt.format(Date(millis))

    fun formatTime(millis: Long): String = timeFmt.format(Date(millis))

    fun isToday(millis: Long): Boolean {
        val today = todayStart()
        val tomorrow = Date(today.time + 86400000)
        return millis >= today.time && millis < tomorrow.time
    }

    fun isThisWeek(millis: Long): Boolean {
        return millis >= weekStart().time && millis <= weekEnd().time
    }

    fun dueText(dueMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = dueMillis - now
        if (diff < 0) return "已逾期"
        val mins = diff / 60000
        if (mins < 60) return "${mins}分钟后"
        val hours = mins / 60
        if (hours < 24) return "${hours}小时后"
        val days = hours / 24
        return "${days}天后"
    }
}
