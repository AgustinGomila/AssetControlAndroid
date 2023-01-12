package com.dacosys.assetControl.dataBase.datacollection

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContract.DataCollectionRuleEntry.Companion.ACTIVE
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContract.DataCollectionRuleEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContract.DataCollectionRuleEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContract.DataCollectionRuleEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContract.getAllColumns
import com.dacosys.assetControl.model.datacollection.DataCollectionRule
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.webservice.datacollection.DataCollectionRuleObject

/**
 * Created by Agustin on 28/12/2016.
 */

class DataCollectionRuleDbHelper {
    fun sync(
        objArray: Array<DataCollectionRuleObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        currentCount: Int,
        countTotal: Int,
    ): Boolean {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> delete: id:%s", obj.dataCollectionRuleId)
            )

            val values = "($DATA_COLLECTION_RULE_ID = ${obj.dataCollectionRuleId}) OR "
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
                    DATA_COLLECTION_RULE_ID + "," +
                    DESCRIPTION + "," +
                    ACTIVE + ")" +
                    " VALUES "

            var count = 0
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.dataCollectionRuleId)
                )
                count++
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = countTotal,
                        completedTask = currentCount + count,
                        msg = getContext()
                            .getString(R.string.synchronizing_data_collection_rules),
                        registryType = SyncRegistryType.DataCollectionRule,
                        progressStatus = ProgressStatus.running
                    )
                )

                val values = "(" +
                        obj.dataCollectionRuleId + "," +
                        "'" + obj.description.replace("'", "''") + "'," +
                        obj.active + "),"

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
        dataCollectionRuleId: Long,
        description: String,
        active: Boolean,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newDataCollectionRule = DataCollectionRule(
            dataCollectionRuleId,
            description,
            active
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newDataCollectionRule.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun insert(dataCollectionRule: DataCollectionRule): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                dataCollectionRule.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun update(dataCollectionRule: DataCollectionRule): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$DATA_COLLECTION_RULE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(dataCollectionRule.dataCollectionRuleId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                dataCollectionRule.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun delete(dataCollectionRule: DataCollectionRule): Boolean {
        return deleteById(dataCollectionRule.dataCollectionRuleId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$DATA_COLLECTION_RULE_ID = ?" // WHERE code LIKE ?
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

    fun select(): ArrayList<DataCollectionRule> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = DESCRIPTION

        val sqLiteDatabase = getReadableDb()
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

    fun selectOnlyActive(): ArrayList<DataCollectionRule> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val selection = "$ACTIVE = 1" // WHERE code LIKE ?
        val order = DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns, // Lista de Columnas a consultar
                selection, // Columnas para la cláusula WHERE
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

    fun selectById(id: Long): DataCollectionRule? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$DATA_COLLECTION_RULE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = DESCRIPTION

        val sqLiteDatabase = getReadableDb()
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

    fun selectByDescription(description: String): ArrayList<DataCollectionRule> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescription ($description)")

        val columns = getAllColumns()
        val selection = "$DESCRIPTION LIKE ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf("%$description%")
        val order = DESCRIPTION

        val sqLiteDatabase = getReadableDb()
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

    fun selectByTargetAssetIdDescription(
        assetId: Long,
        description: String,
        onlyActive: Boolean,
    ): ArrayList<DataCollectionRule> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByTargetAssetIdDescription (A:$assetId/D:$description)"
        )

        /*
        SELECT
            data_collection_rule.data_collection_rule_id,
            data_collection_rule.description,
            data_collection_rule.active
        FROM
            data_collection_rule
        LEFT OUTER JOIN data_collection_rule_target ON data_collection_rule_target.data_collection_rule_id = data_collection_rule.data_collection_rule_id
        WHERE
            (data_collection_rule.description LIKE @description) AND
            (data_collection_rule_target.asset_id = @asset_id)
         */

        if (assetId <= 0) {
            return ArrayList()
        }

        val dcrTarget = DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry

        val basicSelect =
            "SELECT " +
                    TABLE_NAME + "." + DATA_COLLECTION_RULE_ID + ", " +
                    TABLE_NAME + "." + DESCRIPTION + ", " +
                    TABLE_NAME + "." + ACTIVE +
                    " FROM " + TABLE_NAME +
                    " LEFT OUTER JOIN " + dcrTarget.TABLE_NAME + " ON " +
                    dcrTarget.TABLE_NAME + "." + dcrTarget.DATA_COLLECTION_RULE_ID + " = " +
                    TABLE_NAME + "." + DATA_COLLECTION_RULE_ID +
                    " WHERE " +
                    if (description.isNotEmpty()) {
                        "($TABLE_NAME.$DESCRIPTION LIKE '%$description%') AND "
                    } else {
                        ""
                    } +
                    if (onlyActive) {
                        "($TABLE_NAME.$ACTIVE = 1) AND "
                    } else {
                        ""
                    } +
                    "(" + dcrTarget.TABLE_NAME + "." + dcrTarget.ASSET_ID + " = " + assetId + ")"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectByTargetWarehouseAreaIdDescription(
        warehouseAreaId: Long,
        description: String,
        onlyActive: Boolean,
    ): ArrayList<DataCollectionRule> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByTargetWarehouseAreaIdDescription (W:$warehouseAreaId/D:$description)"
        )

        /*
        SELECT
            data_collection_rule.data_collection_rule_id,
            data_collection_rule.description,
            data_collection_rule.active
        FROM
            data_collection_rule
        LEFT OUTER JOIN data_collection_rule_target ON data_collection_rule_target.data_collection_rule_id = data_collection_rule.data_collection_rule_id
        WHERE
            (data_collection_rule.description LIKE @description) AND
            (data_collection_rule_target.warehouse_area_id = @warehouse_area_id)
         */

        if (warehouseAreaId <= 0) {
            return ArrayList()
        }

        val dcrTarget = DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry

        val basicSelect =
            "SELECT " +
                    TABLE_NAME + "." + DATA_COLLECTION_RULE_ID + ", " +
                    TABLE_NAME + "." + DESCRIPTION + ", " +
                    TABLE_NAME + "." + ACTIVE +
                    " FROM " + TABLE_NAME +
                    " LEFT OUTER JOIN " + dcrTarget.TABLE_NAME + " ON " +
                    dcrTarget.TABLE_NAME + "." + dcrTarget.DATA_COLLECTION_RULE_ID + " = " +
                    TABLE_NAME + "." + DATA_COLLECTION_RULE_ID +
                    " WHERE " +
                    if (description.isNotEmpty()) {
                        "($TABLE_NAME.$DESCRIPTION LIKE '%$description%') AND "
                    } else {
                        ""
                    } +
                    if (onlyActive) {
                        "($TABLE_NAME.$ACTIVE = 1) AND "
                    } else {
                        ""
                    } +
                    "(" + dcrTarget.TABLE_NAME + "." + dcrTarget.WAREHOUSE_AREA_ID + " = " + warehouseAreaId + ")"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectByTargetItemCategoryIdDescription(
        itemCategoryId: Long,
        description: String,
        onlyActive: Boolean,
    ): ArrayList<DataCollectionRule> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByTargetItemCategoryIdDescription (IC:$itemCategoryId/D:$description)"
        )

        /*
        SELECT
            data_collection_rule.data_collection_rule_id,
            data_collection_rule.description,
            data_collection_rule.active
        FROM
            data_collection_rule
        LEFT OUTER JOIN data_collection_rule_target ON data_collection_rule_target.data_collection_rule_id = data_collection_rule.data_collection_rule_id
        WHERE
            (data_collection_rule.description LIKE @description) AND
            (data_collection_rule_target.warehouse_area_id = @warehouse_area_id)
         */

        if (itemCategoryId <= 0) {
            return ArrayList()
        }

        val dcrTarget = DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry

        val basicSelect =
            "SELECT " +
                    TABLE_NAME + "." + DATA_COLLECTION_RULE_ID + ", " +
                    TABLE_NAME + "." + DESCRIPTION + ", " +
                    TABLE_NAME + "." + ACTIVE +
                    " FROM " + TABLE_NAME +
                    " LEFT OUTER JOIN " + dcrTarget.TABLE_NAME + " ON " +
                    dcrTarget.TABLE_NAME + "." + dcrTarget.DATA_COLLECTION_RULE_ID + " = " +
                    TABLE_NAME + "." + DATA_COLLECTION_RULE_ID +
                    " WHERE " +
                    if (description.isNotEmpty()) {
                        "($TABLE_NAME.$DESCRIPTION LIKE '%$description%') AND "
                    } else {
                        ""
                    } +
                    if (onlyActive) {
                        "($TABLE_NAME.$ACTIVE = 1) AND "
                    } else {
                        ""
                    } +
                    "(" + dcrTarget.TABLE_NAME + "." + dcrTarget.ITEM_CATEGORY_ID + " = " + itemCategoryId + ")"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<DataCollectionRule> {
        val result = ArrayList<DataCollectionRule>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_RULE_ID))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))

                    val temp = DataCollectionRule(
                        id,
                        description,
                        active
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        /*
        CREATE TABLE "data_collection_rule" (
        `_id` bigint NOT NULL,
        `description` nvarchar ( 100 ) NOT NULL,
        `active` int NOT NULL,
        CONSTRAINT `PK_data_collection_rule` PRIMARY KEY(`_id`) )
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + DATA_COLLECTION_RULE_ID + "] BIGINT NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 100 ) NOT NULL, "
                + " [" + ACTIVE + "] INT NOT NULL, "
                + " CONSTRAINT [PK_" + DATA_COLLECTION_RULE_ID + "] PRIMARY KEY ([" + DATA_COLLECTION_RULE_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])"
        )
    }
}