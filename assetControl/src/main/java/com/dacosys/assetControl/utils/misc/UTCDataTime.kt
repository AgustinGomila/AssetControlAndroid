package com.dacosys.assetControl.utils.misc

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
class UTCDataTime {
    companion object {
        private const val dateFormat = "yyyy-MM-dd HH:mm:ss"

        fun getUTCDateTimeAsDate(): Date? {
            //note: doesn't check for null
            return stringDateToDate(getUTCDateTimeAsString())
        }

        fun getUTCDateTimeAsString(): String {
            val sdf = SimpleDateFormat(dateFormat, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val date = sdf.parse(sdf.format(Date())) ?: Calendar.getInstance()

            sdf.timeZone = TimeZone.getDefault()
            val r = sdf.format(date)
            Log.d(this::class.java.simpleName, "Current date: $r")
            return r
        }

        fun dateToStringDate(dateTime: Date): String? {
            val sdf = SimpleDateFormat(dateFormat, Locale.US)
            return try {
                val date = sdf.parse(sdf.format(dateTime)) ?: Calendar.getInstance()
                sdf.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun stringDateToDate(strDate: String): Date? {
            var dateToReturn: Date? = null
            val dateFormat = SimpleDateFormat(dateFormat, Locale.US)

            try {
                dateToReturn = dateFormat.parse(strDate) as Date
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return dateToReturn
        }
    }
}