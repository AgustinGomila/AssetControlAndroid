package com.dacosys.assetControl.data.room.database

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

// Interfaz que define las operaciones comunes de una base de datos
interface DatabaseInterface {
    fun execSQL(sql: String)
}

// Clase que implementa la interfaz para SupportSQLiteDatabase
class SupportSQLiteDB(private val database: SupportSQLiteDatabase) : DatabaseInterface {
    override fun execSQL(sql: String) {
        database.execSQL(sql)
    }
}

// Clase que implementa la interfaz para SQLiteDatabase
class SQLiteDB(private val database: SQLiteDatabase) : DatabaseInterface {
    override fun execSQL(sql: String) {
        database.execSQL(sql)
    }
}

// Función que acepta una base de datos genérica y ejecuta el comando SQL
fun runMigration(databaseInterface: DatabaseInterface, sqlCommands: List<String>) {
    for (sql in sqlCommands) {
        databaseInterface.execSQL(sql)
    }
}