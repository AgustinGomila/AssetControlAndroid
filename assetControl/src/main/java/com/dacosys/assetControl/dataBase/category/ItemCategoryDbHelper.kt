package com.dacosys.assetControl.dataBase.category

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.ACTIVE
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.PARENT_ID
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.PARENT_STR
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.webservice.category.ItemCategoryObject

/**
 * Created by Agustin on 28/12/2016.
 */

class ItemCategoryDbHelper {
    fun sync(
        objArray: Array<ItemCategoryObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        currentCount: Int,
        countTotal: Int,
    ): Boolean {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> delete: id:%s", obj.item_category_id)
            )

            val values = "($ITEM_CATEGORY_ID = ${obj.item_category_id}) OR "
            query = "$query$values"
        }

        if (query.endsWith(" OR ")) {
            query = query.substring(0, query.length - 4)
        }

        Log.d(this::class.java.simpleName, query)

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(query)

            query = "INSERT INTO " + TABLE_NAME + " (" +
                    ITEM_CATEGORY_ID + "," +
                    DESCRIPTION + "," +
                    ACTIVE + "," +
                    PARENT_ID + "," +
                    TRANSFERRED + ")" +
                    " VALUES "

            var count = 0
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.item_category_id)
                )
                count++
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = countTotal,
                        completedTask = currentCount + count,
                        msg = getContext()
                            .getString(R.string.synchronizing_categories),
                        registryType = SyncRegistryType.ItemCategory,
                        progressStatus = ProgressStatus.running
                    )
                )

                val values = "(" +
                        obj.item_category_id + "," +
                        "'" + obj.description.replace("'", "''") + "'," +
                        obj.active + "," +
                        obj.parent_id + "," +
                        1 + "),"

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
        itemCategoryId: Long,
        description: String,
        active: Boolean,
        parentId: Long,
        transferred: Boolean,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newItemCategory = ItemCategory(
            itemCategoryId,
            description,
            active,
            parentId,
            transferred
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newItemCategory.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    private fun getChangesCount(): Long {
        val db = getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun updateItemCategoryId(newItemCategoryId: Long, oldItemCategoryId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateItemCategoryId")

        val selection = "$ITEM_CATEGORY_ID = ?"
        val selectionArgs = arrayOf(oldItemCategoryId.toString())
        val values = ContentValues()
        values.put(ITEM_CATEGORY_ID, newItemCategoryId)

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun updateTransferred(itemIdArray: Array<Long>): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransferred")
        if (itemIdArray.isEmpty()) return false

        val sqLiteDatabase = getWritableDb()

        var error = false
        try {
            for (id in itemIdArray) {

                val values = ContentValues()
                values.put(TRANSFERRED, 1)

                val selection = "$ITEM_CATEGORY_ID = ?"
                val args = arrayOf(id.toString())

                val updatedRows = sqLiteDatabase.update(TABLE_NAME, values, selection, args)

                if (BuildConfig.DEBUG) {
                    if (updatedRows > 0) Log.d(javaClass.simpleName, "Category ID: $id Updated")
                    else Log.e(javaClass.simpleName, "Category ID: $id NOT Updated!!!")
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            error = true
        }

        return !error
    }

    fun selectNoTransfered(): ArrayList<ItemCategory> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectNoTransfered")

        val alias = "ic"
        val parentAlias = "pc"
        val where = " WHERE (" +
                alias + "." + TRANSFERRED + " = 0)"
        val rawQuery = basicSelect(alias) + "," +
                basicStrFields(parentAlias) +
                " FROM " + TABLE_NAME + " " + alias + " " +
                basicLeftJoin(alias, parentAlias) +
                where +
                " ORDER BY " + alias + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun insert(itemCategory: ItemCategory): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                itemCategory.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun update(ic: ItemCategoryObject): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ITEM_CATEGORY_ID = ?"
        val selectionArgs = arrayOf(ic.item_category_id.toString())

        val values = ContentValues()
        values.put(ITEM_CATEGORY_ID, ic.item_category_id)
        values.put(PARENT_ID, ic.parent_id)
        values.put(ACTIVE, ic.active)
        values.put(DESCRIPTION, ic.description)
        values.put(TRANSFERRED, 0)

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun update(itemCategory: ItemCategory): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ITEM_CATEGORY_ID = ?"
        val selectionArgs = arrayOf(itemCategory.itemCategoryId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                itemCategory.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun delete(itemCategory: ItemCategory): Boolean {
        return deleteById(itemCategory.itemCategoryId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$ITEM_CATEGORY_ID = ?"
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun deleteAll(): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteAll")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                TABLE_NAME,
                null,
                null
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun select(onlyActive: Boolean): ArrayList<ItemCategory> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        /*
        SELECT ic._id,
               ic.description,
               ic.active,
               ic.parent_id,
               ic.transferred,
               pc.description AS parent_str
        FROM item_category ic LEFT JOIN item_category pc ON ic.parent_id = pc._id
        WHERE (ic.active = 1)
        ORDER BY ic.description
        */

        val alias = "ic"
        val parentAlias = "pc"
        var where = ""
        if (onlyActive) {
            where = " WHERE ($alias.$ACTIVE = 1)"
        }
        val rawQuery = basicSelect(alias) + "," +
                basicStrFields(parentAlias) +
                " FROM " + TABLE_NAME + " " + alias + " " +
                basicLeftJoin(alias, parentAlias) +
                where +
                " ORDER BY " + alias + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectById(id: Long): ItemCategory? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val alias = "ic"
        val parentAlias = "pc"
        val where = " WHERE ($alias.$ITEM_CATEGORY_ID = $id)"
        val rawQuery = basicSelect(alias) + "," +
                basicStrFields(parentAlias) +
                " FROM " + TABLE_NAME + " " + alias + " " +
                basicLeftJoin(alias, parentAlias) +
                where +
                " ORDER BY " + alias + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            val result = fromCursor(c)
            when {
                result.size > 0 -> result[0]
                else -> null
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            null
        }
    }

    fun selectByDescription(description: String, onlyActive: Boolean): ArrayList<ItemCategory> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescription ($description)")

        val alias = "ic"
        val parentAlias = "pc"
        val where = " WHERE ( " +
                if (onlyActive) {
                    "$alias.$ACTIVE = 1) AND ("
                } else {
                    ""
                } +
                alias + "." + DESCRIPTION + " LIKE '%" + description + "%')"
        val rawQuery = basicSelect(alias) + "," +
                basicStrFields(parentAlias) +
                " FROM " + TABLE_NAME + " " + alias + " " +
                basicLeftJoin(alias, parentAlias) +
                where +
                " ORDER BY " + alias + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectByParentIdDescription(
        parentId: Long,
        description: String,
        onlyActive: Boolean,
    ): ArrayList<ItemCategory> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByParentIdDescription (P:$parentId/D:$description)"
        )

        val alias = "ic"
        val parentAlias = "pc"
        val where = " WHERE ( " +
                if (onlyActive) {
                    "$alias.$ACTIVE = 1) AND ("
                } else {
                    ""
                } +
                alias + "." + DESCRIPTION + " LIKE '%" + description + "%') AND (" +
                alias + "." + PARENT_ID + " = " + parentId + ")"
        val rawQuery = basicSelect(alias) + "," +
                basicStrFields(parentAlias) +
                " FROM " + TABLE_NAME + " " + alias + " " +
                basicLeftJoin(alias, parentAlias) +
                where +
                " ORDER BY " + alias + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectByParentId(parentId: Long, onlyActive: Boolean): ArrayList<ItemCategory> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByParentId ($parentId)")

        val alias = "ic"
        val parentAlias = "pc"
        val where = " WHERE ( " +
                if (onlyActive) {
                    "$alias.$ACTIVE = 1) AND ("
                } else {
                    ""
                } +
                alias + "." + PARENT_ID + " = " + parentId + ")"
        val rawQuery = basicSelect(alias) + "," +
                basicStrFields(parentAlias) +
                " FROM " + TABLE_NAME + " " + alias + " " +
                basicLeftJoin(alias, parentAlias) +
                where +
                " ORDER BY " + alias + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<ItemCategory> {
        val result = ArrayList<ItemCategory>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(ITEM_CATEGORY_ID))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val parentId = it.getLong(it.getColumnIndexOrThrow(PARENT_ID))
                    val transferred = it.getInt(it.getColumnIndexOrThrow(TRANSFERRED)) == 1

                    val temp = ItemCategory(
                        id,
                        description,
                        active,
                        parentId,
                        transferred
                    )

                    temp.parentStr = it.getString(it.getColumnIndexOrThrow(PARENT_STR)) ?: ""
                    result.add(temp)
                }
            }
        }
        return result
    }

    val minId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> minId")

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount =
                    sqLiteDatabase.rawQuery("SELECT MIN($ITEM_CATEGORY_ID) FROM $TABLE_NAME", null)
                mCount.moveToFirst()
                val count = mCount.getLong(0)
                mCount.close()
                if (count > 0) -1 else count - 1
            } catch (ex: SQLException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                0
            }
        }

    private fun basicSelect(alias: String): String {
        return "SELECT " +
                alias + "." + ITEM_CATEGORY_ID + "," +
                alias + "." + DESCRIPTION + "," +
                alias + "." + ACTIVE + "," +
                alias + "." + PARENT_ID + "," +
                alias + "." + TRANSFERRED
    }

    private fun basicStrFields(parentAlias: String): String {
        return "$parentAlias.$DESCRIPTION AS $PARENT_STR"
    }

    private fun basicLeftJoin(alias: String, parentAlias: String): String {
        return " LEFT OUTER JOIN $TABLE_NAME $parentAlias ON $alias.$PARENT_ID = $parentAlias.$ITEM_CATEGORY_ID"
    }

    companion object {
        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + ITEM_CATEGORY_ID + "] BIGINT NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + ACTIVE + "] INT NOT NULL, "
                + " [" + PARENT_ID + "] BIGINT NOT NULL, "
                + " [" + TRANSFERRED + "] INT ,"
                + " CONSTRAINT [PK_" + ITEM_CATEGORY_ID + "] PRIMARY KEY ([" + ITEM_CATEGORY_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$PARENT_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$PARENT_ID] ON [$TABLE_NAME] ([$PARENT_ID])"
        )
    }
}