package com.dacosys.assetControl.data.dataBase.datacollection

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.data.dataBase.datacollection.FragmentDataContract.FragmentDataEntry.Companion.ATTRIBUTE_COMPOSITION_TYPE_ID
import com.dacosys.assetControl.data.dataBase.datacollection.FragmentDataContract.FragmentDataEntry.Companion.DATA_COLLECTION_RULE_CONTENT_ID
import com.dacosys.assetControl.data.dataBase.datacollection.FragmentDataContract.FragmentDataEntry.Companion.IS_ENABLED
import com.dacosys.assetControl.data.dataBase.datacollection.FragmentDataContract.FragmentDataEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.data.dataBase.datacollection.FragmentDataContract.FragmentDataEntry.Companion.VALUE_STR
import com.dacosys.assetControl.ui.fragments.route.FragmentData
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.splitList

class FragmentDataDbHelper {
    fun tempTableInsert(itemArray: Array<FragmentData>): Boolean {
        var result = false

        // Las tablas temporales y los índices se crearán sólo si no existen
        createTempTable()

        val sqLiteDatabase = getWritableDb()
        try {
            sqLiteDatabase.delete(TABLE_NAME, null, null)
            result = insertTemp(itemArray)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        }
        return result
    }

    // Funciones que guardan y recuperan Id entre actividades
    // y evitar el error: !!! FAILED BINDER TRANSACTION !!!
    // cuando se pasa un objeto demasiado grande

    private fun tempTableExists(): Boolean {
        val sqLiteDatabase = getReadableDb()
        val query =
            "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '${TABLE_NAME}'"
        sqLiteDatabase.rawQuery(query, null).use { cursor ->
            return (cursor != null && cursor.count > 0)
        }
    }

    private fun createTempTable() {
        if (!tempTableExists()) {
            val allCommands: ArrayList<String> = ArrayList()
            allCommands.add(CREATE_TEMP_TABLE)
            for (sql in getTempIndex()) {
                allCommands.add(sql)
            }

            val sqLiteDatabase = getWritableDb()
            sqLiteDatabase.beginTransaction()
            try {
                for (sql in allCommands) {
                    println("$sql;")
                    sqLiteDatabase.execSQL(sql)
                }
                sqLiteDatabase.setTransactionSuccessful()
            } finally {
                sqLiteDatabase.endTransaction()
            }
        }
    }

    private fun insertTemp(itemArray: Array<FragmentData>): Boolean {
        if (itemArray.isEmpty()) {
            return false
        }

        val sqLiteDatabase = getWritableDb()

        val splitList = splitList(itemArray, 20)
        var error = false
        try {
            sqLiteDatabase.beginTransaction()
            for (part in splitList) {
                var insertQ =
                    ("INSERT INTO $TABLE_NAME (" +
                            "$DATA_COLLECTION_RULE_CONTENT_ID," +
                            "$ATTRIBUTE_COMPOSITION_TYPE_ID," +
                            "$VALUE_STR," +
                            "$IS_ENABLED) VALUES ")

                for (i in part) {
                    if (i.dcrContId == null) continue

                    val values =
                        "(${i.dcrContId}," +
                                "${(i.attrCompTypeId ?: 0)}," +
                                "'${i.valueStr}'," +
                                "${(if (i.isEnabled) 1 else 0)}),"

                    insertQ = "$insertQ$values"
                }

                if (insertQ.endsWith(",")) {
                    insertQ = insertQ.substring(0, insertQ.length - 1)
                }

                Log.i(this.javaClass.simpleName, insertQ)

                sqLiteDatabase.execSQL(insertQ)
            }
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            error = true
        } finally {
            sqLiteDatabase.endTransaction()
        }

        return !error
    }

    fun tempTableSelect(): ArrayList<FragmentData> {
        Log.d(
            this::class.java.simpleName,
            "SQLITE-SELECT"
        )

        val rawQuery = getTempBasicSelect()

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<FragmentData> {
        val res = ArrayList<FragmentData>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val dcrContId =
                        it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_RULE_CONTENT_ID))
                    val isEnabled =
                        it.getInt(it.getColumnIndexOrThrow(IS_ENABLED)) == 1
                    val valueStr = it.getString(it.getColumnIndexOrThrow(VALUE_STR))
                    val attrCompTypeId =
                        it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_COMPOSITION_TYPE_ID))

                    val temp = FragmentData(
                        dcrContId = dcrContId,
                        attrCompTypeId = attrCompTypeId,
                        valueStr = valueStr,
                        isEnabled = isEnabled
                    )
                    res.add(temp)
                }
            }
        }
        return res
    }

    companion object {
        const val CREATE_TEMP_TABLE =
            """CREATE TABLE IF NOT EXISTS [$TABLE_NAME] ( 
            [$DATA_COLLECTION_RULE_CONTENT_ID] BIGINT NOT NULL, 
            [$ATTRIBUTE_COMPOSITION_TYPE_ID] BIGINT NOT NULL, 
            [$VALUE_STR] TEXT NOT NULL, 
            [$IS_ENABLED] INT NOT NULL,                        
            PRIMARY KEY( [$DATA_COLLECTION_RULE_CONTENT_ID]) )"""

        private fun getTempIndex(): ArrayList<String> {
            return arrayListOf(
                "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_CONTENT_ID]",
                "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ATTRIBUTE_COMPOSITION_TYPE_ID]",
                "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_CONTENT_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_RULE_CONTENT_ID])",
                "CREATE INDEX [IDX_${TABLE_NAME}_$ATTRIBUTE_COMPOSITION_TYPE_ID] ON [$TABLE_NAME] ([$ATTRIBUTE_COMPOSITION_TYPE_ID])"
            )
        }

        private fun getTempBasicSelect(): String {
            return """SELECT 
                [$DATA_COLLECTION_RULE_CONTENT_ID], 
                [$ATTRIBUTE_COMPOSITION_TYPE_ID],
                [$VALUE_STR],                 
                [$IS_ENABLED]
                FROM [${TABLE_NAME}]"""
        }
    }
}