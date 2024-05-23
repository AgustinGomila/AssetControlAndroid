package com.dacosys.assetControl.utils.misc

import android.util.Log
import com.honeywell.aidc.BuildConfig
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
class UTCDataTime {
    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

        fun getUTCDateTimeAsNotNullDate(): Date {
            return getUTCDateTimeAsDate() ?: Date()
        }

        fun getUTCDateTimeAsDate(): Date? {
            //note: doesn't check for null
            return stringDateToDate(getUTCDateTimeAsString())
        }

        fun getUTCDateTimeAsString(): String {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val date = sdf.parse(sdf.format(Date())) ?: Calendar.getInstance()

            sdf.timeZone = TimeZone.getDefault()
            val r = sdf.format(date)
            if (BuildConfig.DEBUG)
                Log.d(this::class.java.simpleName, "Current date: $r")
            return r
        }

        fun dateToNotNullStringDate(dateTime: Date?): String {
            return dateToStringDate(dateTime).orEmpty()
        }

        fun dateToStringDate(dateTime: Date?): String? {
            if (dateTime == null) return null

            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
            return try {
                val date = sdf.parse(sdf.format(dateTime)) ?: Calendar.getInstance()
                val result = sdf.format(date)
                if (BuildConfig.DEBUG)
                    Log.d(this::class.java.simpleName, "Original date: $dateTime ->> Result date: $result")
                return result
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun stringDateToNotNullDate(strDate: String): Date {
            return if (strDate.isEmpty()) Date()
            else stringDateToDate(strDate) ?: Date()
        }

        fun stringDateToDate(strDate: String): Date? {
            if (strDate.isEmpty()) return null

            var dateToReturn: Date? = null
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)

            try {
                dateToReturn = dateFormat.parse(strDate) as Date
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (BuildConfig.DEBUG)
                Log.d(this::class.java.simpleName, "Original date: $strDate ->> Result date: $dateToReturn")

            return dateToReturn
        }
    }
}