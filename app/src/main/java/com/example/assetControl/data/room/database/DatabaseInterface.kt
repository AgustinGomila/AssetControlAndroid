package com.example.assetControl.data.room.database

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.assetControl.BuildConfig

interface DatabaseInterface {
    fun execSQL(sql: String)
}

class SupportSQLiteDB(private val database: SupportSQLiteDatabase) : DatabaseInterface {
    override fun execSQL(sql: String) {
        database.execSQL(sql)
    }
}

class SQLiteDB(private val database: SQLiteDatabase) : DatabaseInterface {
    override fun execSQL(sql: String) {
        database.execSQL(sql)
    }
}

fun runMigration(databaseInterface: DatabaseInterface, sqlCommands: List<String>) {
    for (sql in sqlCommands) {
        if (BuildConfig.DEBUG) println(sql)
        databaseInterface.execSQL(sql)
    }
}