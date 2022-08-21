package com.dacosys.assetControl.model.users.user.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.permissions.PermissionEntry
import com.dacosys.assetControl.model.users.user.`object`.User
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.EMAIL
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.EXTERNAL_ID
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.NAME
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.PASSWORD
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.USER_ID
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.getAllColumns
import com.dacosys.assetControl.model.users.user.wsObject.UserObject
import com.dacosys.assetControl.model.users.userPermission.dbHelper.UserPermissionContract.UserPermissionEntry.Companion.PERMISSION_ID
import com.dacosys.assetControl.sync.functions.ProgressStatus
import com.dacosys.assetControl.sync.functions.Sync.Companion.SyncTaskProgress
import com.dacosys.assetControl.sync.functions.SyncRegistryType
import com.dacosys.assetControl.model.users.userPermission.dbHelper.UserPermissionContract.UserPermissionEntry.Companion.TABLE_NAME as UP_TABLE_NAME
import com.dacosys.assetControl.model.users.userPermission.dbHelper.UserPermissionContract.UserPermissionEntry.Companion.USER_ID as UP_USER_ID

/**
 * Created by Agustin on 28/12/2016.
 */

class UserDbHelper {
    fun insert(
        userId: Long,
        name: String,
        externalId: String?,
        email: String,
        active: Boolean,
        password: String,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newUser = User(
            userId,
            name,
            externalId,
            email,
            active,
            password
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                newUser.toContentValues()
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

    fun sync(
        objArray: Array<UserObject>,
        callback: SyncTaskProgress?,
        currentCount: Int,
        countTotal: Int,
    ): Boolean {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> delete: id:%s", obj.user_id)
            )

            val values = "($USER_ID = ${obj.user_id}) OR "
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

            query = "INSERT INTO [" + TABLE_NAME + "] (" +
                    USER_ID + "," +
                    NAME + "," +
                    ACTIVE + "," +
                    PASSWORD + "," +
                    EXTERNAL_ID + "," +
                    EMAIL + ")" +
                    " VALUES "

            var count = 0
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.user_id)
                )
                count++
                callback?.onSyncTaskProgress(
                    totalTask = countTotal,
                    completedTask = currentCount + count,
                    msg = Statics.AssetControl.getContext().getString(R.string.synchronizing_users),
                    registryType = SyncRegistryType.User,
                    progressStatus = ProgressStatus.running
                )

                val values = "(${obj.user_id}," +
                        "'${obj.name.replace("'", "''")}'," +
                        "${obj.active}," +
                        "'${obj.password.replace("'", "''")}'," +
                        "'${obj.external_id.replace("'", "''")}'," +
                        "'${obj.email.replace("'", "''")}'),"
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

    fun insert(user: User): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = if (sqLiteDatabase.insert(
                    TABLE_NAME,
                    null,
                    user.toContentValues()
                ) > 0
            ) {
                user.userId
            } else {
                0
            }
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

    fun update(user: User): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$USER_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(user.userId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                user.toContentValues(),
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

    fun delete(user: User): Boolean {
        return deleteById(user.userId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$USER_ID = ?" // WHERE code LIKE ?
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

    fun select(): ArrayList<User> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = NAME

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
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
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByActiveAndPermission(): ArrayList<User> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByActiveAndPermission")

        val basicSelect = "SELECT " +
                UP_TABLE_NAME + "." + UP_USER_ID + " AS " + USER_ID + ", " +
                "[" + TABLE_NAME + "]." + NAME + ", " +
                "[" + TABLE_NAME + "]." + EXTERNAL_ID + ", " +
                "[" + TABLE_NAME + "]." + EMAIL + ", " +
                "[" + TABLE_NAME + "]." + ACTIVE + ", " +
                "[" + TABLE_NAME + "]." + PASSWORD +
                " FROM " + UP_TABLE_NAME +
                " LEFT OUTER JOIN [" + TABLE_NAME + "] ON [" + TABLE_NAME + "]." + USER_ID + " = " +
                UP_TABLE_NAME + "." + UP_USER_ID +
                " WHERE ([" + TABLE_NAME + "]." + ACTIVE + " = 1 AND " +
                UP_TABLE_NAME + "." + PERMISSION_ID + " = " + PermissionEntry.UseCollectorProgram.id + ")"

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
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

    fun selectById(userId: Long): User? {
        val columns = getAllColumns()
        val selection = "$USER_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(userId.toString())
        val order = NAME

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

    fun selectUserByNameOrEmail(nameOrEmail: String): User? {
        val columns = getAllColumns()
        val selection = "$NAME = ? OR $EMAIL = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(nameOrEmail)
        val order = NAME

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

    private fun fromCursor(c: Cursor?): ArrayList<User> {
        val result = ArrayList<User>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(USER_ID))
                    val name = it.getString(it.getColumnIndexOrThrow(NAME))
                    val externalId = it.getString(it.getColumnIndexOrThrow(EXTERNAL_ID))
                    val email = it.getString(it.getColumnIndexOrThrow(EMAIL))
                    val password = it.getString(it.getColumnIndexOrThrow(PASSWORD))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1

                    val temp = User(
                        id,
                        name,
                        externalId,
                        email,
                        active,
                        password
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
                "[" + NAME + "] nvarchar(255) NOT NULL, " +
                "[" + EXTERNAL_ID + "] nvarchar(45) , " +
                "[" + EMAIL + "] nvarchar(255) NOT NULL UNIQUE , " +
                "[" + ACTIVE + "] int NOT NULL, " +
                "[" + PASSWORD + "] nvarchar(100) NULL, " +
                "CONSTRAINT [PK_user] PRIMARY KEY ([" + USER_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$NAME]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$EXTERNAL_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$NAME] ON [$TABLE_NAME] ([$NAME])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$EXTERNAL_ID] ON [$TABLE_NAME] ([$EXTERNAL_ID])"
        )
    }
}