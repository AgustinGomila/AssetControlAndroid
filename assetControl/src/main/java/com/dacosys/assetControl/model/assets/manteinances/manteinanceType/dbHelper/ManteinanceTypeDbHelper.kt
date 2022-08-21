package com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.`object`.ManteinanceType
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.MANTEINANCE_TYPE_GROUP_ID
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.MANTEINANCE_TYPE_ID
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.ManteinanceTypeEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeContract.getAllColumns

/**
 * Created by Agustin on 28/12/2016.
 */

class ManteinanceTypeDbHelper {
    fun insert(
        manteinanceTypeId: Long,
        description: String,
        active: Boolean,
        parentId: Long,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newManteinanceType = ManteinanceType(
            manteinanceTypeId,
            description,
            active,
            parentId
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                newManteinanceType.toContentValues()
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            return r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insert(manteinanceType: ManteinanceType): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                manteinanceType.toContentValues()
            )
            sqLiteDatabase.setTransactionSuccessful()
            return r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    private fun getChangesCount(): Long {
        val db = StaticDbHelper.getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun update(manteinanceType: ManteinanceType): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$MANTEINANCE_TYPE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(manteinanceType.manteinanceTypeId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                manteinanceType.toContentValues(),
                selection,
                selectionArgs
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun delete(manteinanceType: ManteinanceType): Boolean {
        return deleteById(manteinanceType.manteinanceTypeId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$MANTEINANCE_TYPE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.delete(
                TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteAll(): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteAll")

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.delete(
                TABLE_NAME,
                null,
                null
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun select(): ArrayList<ManteinanceType> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns,// Lista de Columnas a consultar
                null,// Columnas para la cláusula WHERE
                null,// Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
            sqLiteDatabase.setTransactionSuccessful()
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectById(id: Long): ManteinanceType? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$MANTEINANCE_TYPE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns, // Lista de Columnas a consultar
                selection, // Columnas para la cláusula WHERE
                selectionArgs,// Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
            sqLiteDatabase.setTransactionSuccessful()
            val result = fromCursor(c)
            return when {
                result.size > 0 -> result[0]
                else -> null
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return null
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByManteinanceTypeGroupId(wId: Long): ArrayList<ManteinanceType> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByManteinanceTypeGroupId ($wId)")

        val columns = getAllColumns()
        val selection = "$MANTEINANCE_TYPE_GROUP_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(wId.toString())
        val order = DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns, // Lista de Columnas a consultar
                selection, // Columnas para la cláusula WHERE
                selectionArgs,// Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
            sqLiteDatabase.setTransactionSuccessful()
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByDescription(description: String): ArrayList<ManteinanceType> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescription ($description)")

        val columns = getAllColumns()
        val selection = "$DESCRIPTION LIKE ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf("%$description%")
        val order = DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns, // Lista de Columnas a consultar
                selection, // Columnas para la cláusula WHERE
                selectionArgs,// Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
            sqLiteDatabase.setTransactionSuccessful()
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByManteinanceTypeGroupIdDescription(
        manteinanceTypeGroupId: Long,
        description: String,
    ): ArrayList<ManteinanceType> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByManteinanceTypeGroupIdDescription (M:$manteinanceTypeGroupId/D:$description)"
        )

        val columns = getAllColumns()
        val selection =
            "$DESCRIPTION LIKE ? AND $MANTEINANCE_TYPE_GROUP_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf("%$description%", manteinanceTypeGroupId.toString())
        val order = DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns, // Lista de Columnas a consultar
                selection, // Columnas para la cláusula WHERE
                selectionArgs,// Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
            sqLiteDatabase.setTransactionSuccessful()
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<ManteinanceType> {
        val result = ArrayList<ManteinanceType>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(MANTEINANCE_TYPE_ID))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val manteinanceTypeGroupId =
                        it.getLong(it.getColumnIndexOrThrow(MANTEINANCE_TYPE_GROUP_ID))

                    val temp = ManteinanceType(
                        id,
                        description,
                        active,
                        manteinanceTypeGroupId
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + MANTEINANCE_TYPE_ID + "] BIGINT NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + ACTIVE + "] INT NOT NULL, "
                + " [" + MANTEINANCE_TYPE_GROUP_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + MANTEINANCE_TYPE_ID + "] PRIMARY KEY ([" + MANTEINANCE_TYPE_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$MANTEINANCE_TYPE_GROUP_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$MANTEINANCE_TYPE_GROUP_ID] ON [$TABLE_NAME] ([$MANTEINANCE_TYPE_GROUP_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])"
        )
    }
}