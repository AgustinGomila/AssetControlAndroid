package com.dacosys.assetControl.dataBase.user

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.user.UserPermissionContract.UserPermissionEntry.Companion.PERMISSION_ID
import com.dacosys.assetControl.dataBase.user.UserPermissionContract.UserPermissionEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.user.UserPermissionContract.UserPermissionEntry.Companion.USER_ID
import com.dacosys.assetControl.dataBase.user.UserPermissionContract.getAllColumns
import com.dacosys.assetControl.model.user.UserPermission
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.splitList
import com.dacosys.assetControl.webservice.user.UserPermissionObject

/**
 * Created by Agustin on 28/12/2016.
 */

class UserPermissionDbHelper {
    fun insert(
        userId: Long,
        permissionId: Long,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newUserPermission = UserPermission(
            userId,
            permissionId
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                newUserPermission.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun insert(
        upArray: Array<UserPermissionObject>?,
        onSyncTaskProgress: (SyncProgress) -> Unit = {},
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        if (upArray.isNullOrEmpty()) {
            return false
        }

        val countTotal = upArray.size
        val splitList = splitList(upArray, 100)

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            for ((index, part) in splitList.withIndex()) {
                var insertQ = ("INSERT INTO $TABLE_NAME" +
                        "($USER_ID," +
                        "$PERMISSION_ID) VALUES ")

                for ((index2, up) in part.withIndex()) {
                    Log.d(
                        this::class.java.simpleName,
                        "SQLITE-QUERY-INSERT-->" + up.user_id + "," + up.permission_id
                    )
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            totalTask = countTotal,
                            completedTask = (index * 100) + index2 + 1,
                            msg = AssetControlApp.getContext()
                                .getString(R.string.synchronizing_user_permissions),
                            registryType = SyncRegistryType.UserPermission,
                            progressStatus = ProgressStatus.running
                        )
                    )

                    val values = "(${up.user_id},${up.permission_id}),"
                    insertQ = "$insertQ$values"
                }

                if (insertQ.endsWith(",")) {
                    insertQ = insertQ.substring(0, insertQ.length - 1)
                }

                sqLiteDatabase.execSQL(insertQ)
            }
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

    fun insert(userPermission: UserPermission): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                userPermission.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun deleteByUserId(userId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByUserId ($userId)")

        val selection = "$USER_ID = ?"
        val selectionArgs = arrayOf(userId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: Exception) {
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

    fun select(): ArrayList<UserPermission> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = "$USER_ID, $PERMISSION_ID"

        val sqLiteDatabase = getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns,// Lista de Columnas a consultar
                null,// Columnas para la cláusula WHERE
                null, // Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
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

    fun selectByUserIdUserPermissionId(userId: Long, userPermissionId: Long): UserPermission? {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByUserIdUserPermissionId (U:$userId/P:$userPermissionId)"
        )

        val columns = getAllColumns()
        val selection = "$USER_ID = ? AND $PERMISSION_ID = ?"
        val selectionArgs = arrayOf(userId.toString(), userPermissionId.toString())
        val order = "$USER_ID, $PERMISSION_ID"

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

    fun selectByUserId(userId: Long): UserPermission? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByUserId ($userId)")

        val columns = getAllColumns()
        val selection = "$USER_ID = ?"
        val selectionArgs = arrayOf(userId.toString())
        val order = "$USER_ID, $PERMISSION_ID"

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

    private fun fromCursor(c: Cursor?): ArrayList<UserPermission> {
        val result = ArrayList<UserPermission>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val userId = it.getLong(it.getColumnIndexOrThrow(USER_ID))
                    val permissionId = it.getLong(it.getColumnIndexOrThrow(PERMISSION_ID))

                    val temp = UserPermission(
                        userId = userId,
                        permissionId = permissionId
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "] " +
                "( [" + USER_ID + "] BIGINT NOT NULL, " +
                "[" + PERMISSION_ID + "] BIGINT NOT NULL )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$USER_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$PERMISSION_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$USER_ID] ON [$TABLE_NAME] ([$USER_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$PERMISSION_ID] ON [$TABLE_NAME] ([$PERMISSION_ID])"
        )
    }
}
