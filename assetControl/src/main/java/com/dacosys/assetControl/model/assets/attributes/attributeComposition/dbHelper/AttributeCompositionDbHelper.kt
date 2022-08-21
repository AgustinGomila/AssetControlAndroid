package com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.`object`.AttributeComposition
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.ATTRIBUTE_COMPOSITION_ID
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.ATTRIBUTE_COMPOSITION_TYPE_ID
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.ATTRIBUTE_ID
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.COMPOSITION
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.DEFAULT_VALUE
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.NAME
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.READ_ONLY
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.AttributeCompositionEntry.Companion.USED
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionContract.getAllColumns

/**
 * Created by Agustin on 28/12/2016.
 */

class AttributeCompositionDbHelper {
    fun insert(
        attributeCompositionId: Long,
        attributeId: Long,
        attributeCompositionTypeId: Long,
        description: String,
        composition: String,
        used: Boolean,
        name: String,
        readOnly: Boolean,
        defaultValue: String,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newAttributeComposition = AttributeComposition(
            attributeCompositionId,
            attributeId,
            attributeCompositionTypeId,
            description,
            composition,
            used,
            name,
            readOnly,
            defaultValue
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                newAttributeComposition.toContentValues()
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insertChunked(objArray: List<AttributeComposition>): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        var query = "INSERT INTO [" + TABLE_NAME + "] (" +
                ATTRIBUTE_COMPOSITION_ID + "," +
                ATTRIBUTE_ID + "," +
                ATTRIBUTE_COMPOSITION_TYPE_ID + "," +
                DESCRIPTION + "," +
                COMPOSITION + "," +
                USED + "," +
                NAME + "," +
                READ_ONLY + "," +
                DEFAULT_VALUE + ")" +
                " VALUES "

        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> insert")
            )
            val values = "(${obj.attributeCompositionId}," +
                    "${obj.attributeId}," +
                    "${obj.attributeCompositionTypeId}," +
                    "'${obj.description.replace("'", "''")}'," +
                    "'${obj.composition?.replace("'", "''")}'," +
                    "${obj.used}," +
                    "'${obj.name.replace("'", "''")}'," +
                    "${obj.readOnly}," +
                    "'${obj.defaultValue.replace("'", "''")}'),"
            query = "$query$values"
        }

        if (query.endsWith(",")) {
            query = query.substring(0, query.length - 1)
        }

        Log.d(this::class.java.simpleName, query)

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            sqLiteDatabase.execSQL(query)
            sqLiteDatabase.setTransactionSuccessful()
            true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insert(attributeComposition: AttributeComposition): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                attributeComposition.toContentValues()
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

    fun update(attributeComposition: AttributeComposition): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ATTRIBUTE_COMPOSITION_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(attributeComposition.attributeCompositionId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                attributeComposition.toContentValues(),
                selection,
                selectionArgs
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun delete(attributeComposition: AttributeComposition): Boolean {
        return deleteById(attributeComposition.attributeCompositionId)
    }

    fun deleteByAttrIdArray(idArray: ArrayList<Long>) {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in idArray) {
            Log.i(this::class.java.simpleName, String.format(": SQLite -> delete: id:%s", obj))

            val values = "(${ATTRIBUTE_ID} = ${obj}) OR "
            query = "$query$values"
        }

        if (query.endsWith(" OR ")) {
            query = query.substring(0, query.length - 4)
        }

        Log.d(this::class.java.simpleName, query)

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(query)
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteByAttributeId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByAttributeId ($id)")

        val selection = "$ATTRIBUTE_ID = ?" // WHERE code LIKE ?
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
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$ATTRIBUTE_COMPOSITION_ID = ?" // WHERE code LIKE ?
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
        } catch (ex: Exception) {
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

    fun select(): ArrayList<AttributeComposition> {
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

    fun selectById(id: Long): AttributeComposition? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$ATTRIBUTE_COMPOSITION_ID = ?" // WHERE code LIKE ?
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

    fun selectByAttributeId(id: Long): ArrayList<AttributeComposition> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByAttributeId ($id)")

        val columns = getAllColumns()
        val selection = "$ATTRIBUTE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = ATTRIBUTE_COMPOSITION_ID

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

    fun selectByDescription(description: String): ArrayList<AttributeComposition> {
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

    private fun fromCursor(c: Cursor?): ArrayList<AttributeComposition> {
        val result = ArrayList<AttributeComposition>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_COMPOSITION_ID))
                    val attributeId = it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_ID))
                    val attributeCompositionTypeId =
                        it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_COMPOSITION_TYPE_ID))
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val composition = it.getString(it.getColumnIndexOrThrow(COMPOSITION))
                    val used = it.getInt(it.getColumnIndexOrThrow(USED)) == 1
                    val name = it.getString(it.getColumnIndexOrThrow(NAME))
                    val readOnly = it.getInt(it.getColumnIndexOrThrow(READ_ONLY)) == 1
                    val defaultValue = it.getString(it.getColumnIndexOrThrow(DEFAULT_VALUE))

                    val temp = AttributeComposition(
                        id,
                        attributeId,
                        attributeCompositionTypeId,
                        description,
                        composition,
                        used,
                        name,
                        readOnly,
                        defaultValue
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        /*
        CREATE TABLE "attribute_composition" (
        `_id` bigint NOT NULL,
        `attribute_id` bigint NOT NULL,
        `attribute_composition_type_id` bigint NOT NULL,
        `description` nvarchar ( 255 ),
        `composition` nvarchar ( 4000 ),
        `used` int NOT NULL,
        `key` nvarchar ( 100 ) NOT NULL,
        `read_only` int NOT NULL,
        `default_value` TEXT NOT NULL,
        PRIMARY KEY(`_id`) )
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + ATTRIBUTE_COMPOSITION_ID + "] BIGINT NOT NULL, "
                + " [" + ATTRIBUTE_ID + "] BIGINT NOT NULL, "
                + " [" + ATTRIBUTE_COMPOSITION_TYPE_ID + "] BIGINT NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) ,"
                + " [" + COMPOSITION + "] NVARCHAR ( 4000 ) ,"
                + " [" + USED + "] INT NOT NULL, "
                + " [" + NAME + "] NVARCHAR ( 100 ) NOT NULL, "
                + " [" + READ_ONLY + "] INT NOT NULL, "
                + " [" + DEFAULT_VALUE + "] TEXT NOT NULL, "
                + " CONSTRAINT [PK_" + ATTRIBUTE_COMPOSITION_ID + "] PRIMARY KEY ([" + ATTRIBUTE_COMPOSITION_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ATTRIBUTE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ATTRIBUTE_COMPOSITION_TYPE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ATTRIBUTE_ID] ON [$TABLE_NAME] ([$ATTRIBUTE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ATTRIBUTE_COMPOSITION_TYPE_ID] ON [$TABLE_NAME] ([$ATTRIBUTE_COMPOSITION_TYPE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])"
        )
    }
}