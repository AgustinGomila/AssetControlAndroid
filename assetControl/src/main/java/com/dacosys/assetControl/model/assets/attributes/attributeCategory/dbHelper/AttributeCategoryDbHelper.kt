package com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.`object`.AttributeCategory
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper.AttributeCategoryContract.AttributeCategoryEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper.AttributeCategoryContract.AttributeCategoryEntry.Companion.ATTRIBUTE_CATEGORY_ID
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper.AttributeCategoryContract.AttributeCategoryEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper.AttributeCategoryContract.AttributeCategoryEntry.Companion.PARENT_ID
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper.AttributeCategoryContract.AttributeCategoryEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper.AttributeCategoryContract.getAllColumns
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.wsObject.AttributeCategoryObject
import com.dacosys.assetControl.sync.functions.ProgressStatus
import com.dacosys.assetControl.sync.functions.Sync.Companion.SyncTaskProgress
import com.dacosys.assetControl.sync.functions.SyncRegistryType

/**
 * Created by Agustin on 28/12/2016.
 */

class AttributeCategoryDbHelper {
    fun sync(
        objArray: Array<AttributeCategoryObject>,
        callback: SyncTaskProgress,
        currentCount: Int,
        countTotal: Int,
    ): Boolean {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> delete: id:%s", obj.attributeCategoryId)
            )

            val values = "($ATTRIBUTE_CATEGORY_ID = ${obj.attributeCategoryId}) OR "
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

            query = "INSERT INTO " + TABLE_NAME + " (" +
                    ATTRIBUTE_CATEGORY_ID + "," +
                    DESCRIPTION + "," +
                    ACTIVE + "," +
                    PARENT_ID + ")" +
                    " VALUES "

            var count = 0
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.attributeCategoryId)
                )
                count++
                callback.onSyncTaskProgress(
                    totalTask = countTotal,
                    completedTask = currentCount + count,
                    msg = Statics.AssetControl.getContext()
                        .getString(R.string.synchronizing_attribute_categories),
                    registryType = SyncRegistryType.AttributeCategory,
                    progressStatus = ProgressStatus.running
                )

                val values = "(" +
                        obj.attributeCategoryId + "," +
                        "'" + obj.description.replace("'", "''") + "'," +
                        obj.active + "," +
                        obj.parentId + "),"

                query = "$query$values"
            }

            if (query.endsWith(",")) {
                query = query.substring(0, query.length - 1)
            }

            Log.d(this::class.java.simpleName, query)
            sqLiteDatabase.execSQL(query)
            sqLiteDatabase.setTransactionSuccessful()
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insert(
        attributeCategoryId: Long,
        description: String,
        active: Boolean,
        parentId: Long,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newAttributeCategory = AttributeCategory(
            attributeCategoryId,
            description,
            active,
            parentId
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                newAttributeCategory.toContentValues()
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

    fun insert(attributeCategory: AttributeCategory): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                attributeCategory.toContentValues()
            )
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun update(attributeCategory: AttributeCategory): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ATTRIBUTE_CATEGORY_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(attributeCategory.attributeCategoryId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                attributeCategory.toContentValues(),
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

    fun delete(attributeCategory: AttributeCategory): Boolean {
        return deleteById(attributeCategory.attributeCategoryId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$ATTRIBUTE_CATEGORY_ID = ?" // WHERE code LIKE ?
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

    fun select(): ArrayList<AttributeCategory> {
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

    fun selectById(id: Long): AttributeCategory? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$ATTRIBUTE_CATEGORY_ID = ?" // WHERE code LIKE ?
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

    fun selectByDescription(description: String): ArrayList<AttributeCategory> {
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

    private fun fromCursor(c: Cursor?): ArrayList<AttributeCategory> {
        val result = ArrayList<AttributeCategory>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_CATEGORY_ID))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val parentId = it.getLong(it.getColumnIndexOrThrow(PARENT_ID))

                    val temp = AttributeCategory(
                        id,
                        description,
                        active,
                        parentId
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + ATTRIBUTE_CATEGORY_ID + "] BIGINT NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + ACTIVE + "] INT NOT NULL, "
                + " [" + PARENT_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + ATTRIBUTE_CATEGORY_ID + "] PRIMARY KEY ([" + ATTRIBUTE_CATEGORY_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$PARENT_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$PARENT_ID] ON [$TABLE_NAME] ([$PARENT_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])"
        )
    }
}