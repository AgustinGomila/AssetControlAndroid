package com.dacosys.assetControl.model.assets.itemCategory.dbHelper

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.itemCategory.`object`.ItemCategory
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryContract.ItemCategoryEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryContract.ItemCategoryEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryContract.ItemCategoryEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryContract.ItemCategoryEntry.Companion.PARENT_ID
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryContract.ItemCategoryEntry.Companion.PARENT_STR
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryContract.ItemCategoryEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryContract.ItemCategoryEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.model.assets.itemCategory.wsObject.ItemCategoryObject
import com.dacosys.assetControl.sync.functions.ProgressStatus
import com.dacosys.assetControl.sync.functions.Sync.Companion.SyncTaskProgress
import com.dacosys.assetControl.sync.functions.SyncRegistryType

/**
 * Created by Agustin on 28/12/2016.
 */

class ItemCategoryDbHelper {
    fun sync(
        objArray: Array<ItemCategoryObject>,
        callback: SyncTaskProgress,
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

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
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
                callback.onSyncTaskProgress(
                    totalTask = countTotal,
                    completedTask = currentCount + count,
                    msg = Statics.AssetControl.getContext()
                        .getString(R.string.synchronizing_categories),
                    registryType = SyncRegistryType.ItemCategory,
                    progressStatus = ProgressStatus.running
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

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                newItemCategory.toContentValues()
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

    private fun getChangesCount(): Long {
        val db = StaticDbHelper.getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun updateItemCategoryId(newItemCategoryId: Long, oldItemCategoryId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateItemCategoryId")

        val selection = "$ITEM_CATEGORY_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldItemCategoryId.toString())
        val values = ContentValues()
        values.put(ITEM_CATEGORY_ID, newItemCategoryId)

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                values,
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

    fun updateTransferred(itemCategoryId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransferred")

        /*
        UPDATE asset
        SET transfered = 1
        WHERE (asset_id = @asset_id)
         */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    TRANSFERRED + " = 1 " +
                    " WHERE (" + ITEM_CATEGORY_ID + " = " + itemCategoryId + ")"

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(updateQ, null)
            c.moveToFirst()
            c.close()
            sqLiteDatabase.setTransactionSuccessful()
            getChangesCount() > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
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

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insert(itemCategory: ItemCategory): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                itemCategory.toContentValues()
            )
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun update(ic: ItemCategoryObject): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ITEM_CATEGORY_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(ic.item_category_id.toString())

        val values = ContentValues()
        values.put(ITEM_CATEGORY_ID, ic.item_category_id)
        values.put(PARENT_ID, ic.parent_id)
        values.put(ACTIVE, ic.active)
        values.put(DESCRIPTION, ic.description)
        values.put(TRANSFERRED, 0)

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                values,
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

    fun update(itemCategory: ItemCategory): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ITEM_CATEGORY_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(itemCategory.itemCategoryId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                itemCategory.toContentValues(),
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

    fun delete(itemCategory: ItemCategory): Boolean {
        return deleteById(itemCategory.itemCategoryId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$ITEM_CATEGORY_ID = ?" // WHERE code LIKE ?
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

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
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

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
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

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
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

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
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

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
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

            val sqLiteDatabase = StaticDbHelper.getReadableDb()
            sqLiteDatabase.beginTransaction()
            return try {
                val mCount =
                    sqLiteDatabase.rawQuery("SELECT MIN($ITEM_CATEGORY_ID) FROM $TABLE_NAME", null)
                sqLiteDatabase.setTransactionSuccessful()
                mCount.moveToFirst()
                val count = mCount.getLong(0)
                mCount.close()
                if (count > 0) {
                    -1
                } else {
                    count - 1
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                0
            } finally {
                sqLiteDatabase.endTransaction()
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