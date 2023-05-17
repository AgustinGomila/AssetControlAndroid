package com.dacosys.assetControl.dataBase

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.asset.AssetContract
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.attribute.*
import com.dacosys.assetControl.dataBase.barcode.BarcodeLabelCustomContract
import com.dacosys.assetControl.dataBase.barcode.BarcodeLabelCustomDbHelper
import com.dacosys.assetControl.dataBase.barcode.BarcodeLabelTargetContract
import com.dacosys.assetControl.dataBase.barcode.BarcodeLabelTargetDbHelper
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract
import com.dacosys.assetControl.dataBase.category.ItemCategoryDbHelper
import com.dacosys.assetControl.dataBase.datacollection.*
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract
import com.dacosys.assetControl.dataBase.location.WarehouseAreaDbHelper
import com.dacosys.assetControl.dataBase.location.WarehouseContract
import com.dacosys.assetControl.dataBase.location.WarehouseDbHelper
import com.dacosys.assetControl.dataBase.manteinance.*
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContract
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementDbHelper
import com.dacosys.assetControl.dataBase.review.*
import com.dacosys.assetControl.dataBase.route.*
import com.dacosys.assetControl.dataBase.user.*
import com.dacosys.assetControl.network.sync.SyncDownload
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.room.database.IcDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionDbHelper.Companion as DataCollectionDbHelper

class DataBaseHelper : SQLiteOpenHelper(
    getContext(),
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    @Synchronized
    override fun close() {
        myDataBase?.close()
        myDataBase = null
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Limpiamos la instancia estática de la base de datos para
        // forzar que una nueva se cree la próxima vez.
        cleanInstance()

        createTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // on upgrade drop older tables
        val allTables: ArrayList<String> = ArrayList()
        Collections.addAll(
            allTables,
            AssetContract.AssetEntry.TABLE_NAME,
            AssetManteinanceContract.AssetManteinanceEntry.TABLE_NAME,
            AssetReviewContract.AssetReviewEntry.TABLE_NAME,
            AssetReviewContentContract.AssetReviewContentEntry.TABLE_NAME,
            AssetReviewStatusContract.AssetReviewStatusEntry.TABLE_NAME,
            AttributeCategoryContract.AttributeCategoryEntry.TABLE_NAME,
            AttributeCompositionContract.AttributeCompositionEntry.TABLE_NAME,
            AttributeContract.AttributeEntry.TABLE_NAME,
            BarcodeLabelCustomContract.BarcodeLabelCustomEntry.TABLE_NAME,
            BarcodeLabelTargetContract.BarcodeLabelTargetEntry.TABLE_NAME,
            DataCollectionContentContract.DataCollectionContentEntry.TABLE_NAME,
            DataCollectionContract.DataCollectionEntry.TABLE_NAME,
            DataCollectionRuleContentContract.DataCollectionRuleContentEntry.TABLE_NAME,
            DataCollectionRuleContract.DataCollectionRuleEntry.TABLE_NAME,
            DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.TABLE_NAME,
            ItemCategoryContract.ItemCategoryEntry.TABLE_NAME,
            ManteinanceStatusContract.ManteinanceStatusEntry.TABLE_NAME,
            ManteinanceTypeContract.ManteinanceTypeEntry.TABLE_NAME,
            ManteinanceTypeGroupContract.ManteinanceTypeGroupEntry.TABLE_NAME,
            RouteCompositionContract.RouteCompositionEntry.TABLE_NAME,
            RouteContract.RouteEntry.TABLE_NAME,
            RouteProcessContentContract.RouteProcessContentEntry.TABLE_NAME,
            RouteProcessContract.RouteProcessEntry.TABLE_NAME,
            RouteProcessStatusContract.RouteProcessStatusEntry.TABLE_NAME,
            RouteProcessStepsContract.RouteProcessStepsEntry.TABLE_NAME,
            WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME,
            WarehouseContract.WarehouseEntry.TABLE_NAME,
            WarehouseMovementContentContract.WarehouseMovementContentEntry.TABLE_NAME,
            WarehouseMovementContract.WarehouseMovementEntry.TABLE_NAME,
            UserContract.UserEntry.TABLE_NAME,
            UserWarehouseAreaContract.UserWarehouseAreaEntry.TABLE_NAME,
            UserPermissionContract.UserPermissionEntry.TABLE_NAME,
        )

        for (tableName in allTables) {
            db.execSQL("DROP TABLE IF EXISTS $tableName")
        }

        // create new tables
        onCreate(db)
    }

    /**
     * Esta función elimina físicamente y limpia la instancia de la DB para ser reconstruída la
     * la próxima vez que se quiera utilizar.
     */
    fun deleteDb() {
        // Limpiamos la instancia estática de la base de datos.
        cleanInstance()

        // Cerramos la DB
        DataBaseHelper().close()

        // Path to the just created empty db
        val outFileName = getContext().getDatabasePath(DATABASE_NAME).toString()

        try {
            val f = File(outFileName)
            if (f.exists()) {
                Log.i(javaClass.simpleName, "Eliminando: $outFileName")
                SQLiteDatabase.deleteDatabase(f)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(this.javaClass.simpleName, "Delete Database: ", e)
        }
    }

    companion object {
        const val DATABASE_VERSION = 1
        var DATABASE_NAME = "assetcontroldb.sqlite"

        private var myDataBase: SQLiteDatabase? = null

        //////////////////////////////////////////////////////////////
        /////////////// INSTANCIA ESTÁTICA (SINGLETON) ///////////////
        private var sInstance: DataBaseHelper? = null

        fun removeDataBases() {
            SyncDownload.resetSyncDates()

            IcDatabase.cleanInstance()
            DataBaseHelper().deleteDb()

            SQLiteDatabase.releaseMemory()
        }

        fun beginDataBase() {
            try {
                createDb()
            } catch (ioe: IOException) {
                throw Error(getContext().getString(R.string.unable_to_create_database))
            }
        }

        fun getReadableDb(): SQLiteDatabase {
            return getInstance()!!.readableDatabase
        }

        fun getWritableDb(): SQLiteDatabase {
            return getInstance()!!.writableDatabase
        }

        fun cleanInstance() {
            sInstance = null
        }

        private fun createDb() {
            createDataBase()
        }

        @Synchronized
        private fun getInstance(): DataBaseHelper? {
            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (sInstance == null) {
                sInstance = DataBaseHelper()
            }
            return sInstance
        }

        fun createTables(db: SQLiteDatabase) {
            db.beginTransaction()
            try {
                for (sql in allCommands) {
                    println("$sql;")
                    db.execSQL(sql)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        fun cleanTemporaryTables() {
            AssetDbHelper().deleteTemp()
        }

        private val allCommands: ArrayList<String>
            get() {
                val c: ArrayList<String> = ArrayList()

                c.add(AssetDbHelper.CREATE_TABLE)
                c.add(AssetManteinanceDbHelper.CREATE_TABLE)
                c.add(AssetReviewDbHelper.CREATE_TABLE)
                c.add(AssetReviewContentDbHelper.CREATE_TABLE)
                c.add(AssetReviewStatusDbHelper.CREATE_TABLE)
                c.add(AttributeCategoryDbHelper.CREATE_TABLE)
                c.add(AttributeCompositionDbHelper.CREATE_TABLE)
                c.add(AttributeDbHelper.CREATE_TABLE)
                c.add(BarcodeLabelCustomDbHelper.CREATE_TABLE)
                c.add(BarcodeLabelTargetDbHelper.CREATE_TABLE)
                c.add(DataCollectionContentDbHelper.CREATE_TABLE)
                c.add(DataCollectionDbHelper.CREATE_TABLE)
                c.add(DataCollectionRuleContentDbHelper.CREATE_TABLE)
                c.add(DataCollectionRuleDbHelper.CREATE_TABLE)
                c.add(DataCollectionRuleTargetDbHelper.CREATE_TABLE)
                c.add(ItemCategoryDbHelper.CREATE_TABLE)
                c.add(ManteinanceStatusDbHelper.CREATE_TABLE)
                c.add(ManteinanceTypeDbHelper.CREATE_TABLE)
                c.add(ManteinanceTypeGroupDbHelper.CREATE_TABLE)
                c.add(RouteCompositionDbHelper.CREATE_TABLE)
                c.add(RouteDbHelper.CREATE_TABLE)
                c.add(RouteProcessContentDbHelper.CREATE_TABLE)
                c.add(RouteProcessDbHelper.CREATE_TABLE)
                c.add(RouteProcessStatusDbHelper.CREATE_TABLE)
                c.add(RouteProcessStepsDbHelper.CREATE_TABLE)
                c.add(WarehouseAreaDbHelper.CREATE_TABLE)
                c.add(WarehouseDbHelper.CREATE_TABLE)
                c.add(WarehouseMovementContentDbHelper.CREATE_TABLE)
                c.add(WarehouseMovementDbHelper.CREATE_TABLE)
                c.add(UserDbHelper.CREATE_TABLE)
                c.add(UserWarehouseAreaDbHelper.CREATE_TABLE)
                c.add(UserPermissionDbHelper.CREATE_TABLE)

                for (sql in AssetDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in AssetManteinanceDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in AssetReviewDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in AssetReviewContentDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in AssetReviewStatusDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in AttributeCategoryDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in AttributeCompositionDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in AttributeDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in BarcodeLabelCustomDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in BarcodeLabelTargetDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in DataCollectionContentDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in DataCollectionDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in DataCollectionRuleContentDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in DataCollectionRuleDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in DataCollectionRuleTargetDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in ItemCategoryDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in ManteinanceStatusDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in ManteinanceTypeDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in ManteinanceTypeGroupDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in RouteCompositionDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in RouteDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in RouteProcessContentDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in RouteProcessDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in RouteProcessStatusDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in RouteProcessStepsDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in WarehouseAreaDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in WarehouseDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in WarehouseMovementContentDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in WarehouseMovementDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in UserDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in UserWarehouseAreaDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                for (sql in UserPermissionDbHelper.CREATE_INDEX) {
                    c.add(sql)
                }
                return c
            }

        /**
         * Creates a empty database on the system and rewrites it with your own database.
         */
        @Throws(IOException::class)
        fun createDataBase() {
            val dbExist = checkDataBase()
            if (!dbExist) {
                // Llamando a este método se creará un base de datos según el modelo en
                // la carpeta determinada del sistama para nuestra aplicación.
                getReadableDb()
            }
        }

        /**
         * Check if the database already exist to avoid re-copying the file each time you open the application.
         *
         * @return true if it exists, false if it doesn't
         */
        private fun checkDataBase(): Boolean {
            try {
                openDataBase()
            } catch (e: SQLiteException) {
                Log.e(
                    this::class.java.simpleName,
                    getContext()
                        .getString(R.string.database_is_not_created_yet)
                )
            }
            return myDataBase != null
        }

        class CopyDbResult(var result: Boolean, var outFile: String)

        fun copyDbToDocuments(): CopyDbResult {
            try {
                val dbFile = File(getContext().getDatabasePath(DATABASE_NAME).toString())

                //Open your local db as the input stream
                val myInput = FileInputStream(dbFile)

                // Path to the just created empty db
                val outDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                if (outDir?.exists() != true) {
                    outDir?.mkdir()
                }

                val outFile = File(outDir, DATABASE_NAME)
                if (outFile.exists()) {
                    outFile.delete()
                }

                outFile.createNewFile()

                //Open the empty db as the output stream
                val myOutput = FileOutputStream(outFile)

                //transfer bytes from the inputfile to the outputfile
                val buffer = ByteArray(1024)
                var length: Int
                while (run {
                        length = myInput.read(buffer)
                        length
                    } > 0) {
                    myOutput.write(buffer, 0, length)
                }

                //Close the streams
                myOutput.flush()
                myOutput.close()
                myInput.close()

                return CopyDbResult(true, outFile.path)
            } catch (ex: IOException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                return CopyDbResult(false, "")
            }
        }

        /**
         * Copies your database from your local assets-folder to the just created empty database in the
         * system folder, from where it can be accessed and handled.
         * This is done by transfering bytestream.
         */
        @Throws(IOException::class)
        fun copyDataBase() {
            //Open your local db as the input stream
            val myInput = getContext().assets.open(DATABASE_NAME)

            // Path to the just created empty db
            val outFileName = getContext().getDatabasePath(DATABASE_NAME).toString()

            try {
                //Open the empty db as the output stream
                val myOutput = FileOutputStream(outFileName)

                //transfer bytes from the inputfile to the outputfile
                val buffer = ByteArray(1024)
                var length: Int
                while (run {
                        length = myInput.read(buffer)
                        length
                    } > 0) {
                    myOutput.write(buffer, 0, length)
                }

                //Close the streams
                myOutput.flush()
                myOutput.close()
                myInput.close()
            } catch (e: IOException) {
                e.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, "DB write failed: $e")
            }
        }

        @Throws(IOException::class)
        fun copyDataBase(path: String) {
            //Open your local db as the input stream
            val myInput = FileInputStream(path)

            // Path to the just created empty db
            val outFileName = getContext().getDatabasePath(DATABASE_NAME).toString()

            try {
                //Open the empty db as the output stream
                val myOutput = FileOutputStream(outFileName)

                //transfer bytes from the inputfile to the outputfile
                val buffer = ByteArray(1024)
                var length: Int
                while (run {
                        length = myInput.read(buffer)
                        length
                    } > 0) {
                    myOutput.write(buffer, 0, length)
                }

                //Close the streams
                myOutput.flush()
                myOutput.close()
                myInput.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
        }

        @Throws(SQLException::class)
        fun removeTempDataBase() {
            //Open the database
            if (DATABASE_NAME.startsWith("temp")) {
                myDataBase?.close()

                val fileName = getContext().getDatabasePath(DATABASE_NAME).toString()
                val myFile = File(fileName)
                if (myFile.exists()) {
                    myFile.delete()
                }

                DATABASE_NAME = "assetcontroldb.sqlite"
            }
        }

        @Throws(SQLException::class)
        fun openDataBase() {
            //Open the database
            val myPath = getContext().getDatabasePath(DATABASE_NAME).toString()
            myDataBase = SQLiteDatabase.openDatabase(
                myPath,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
        }
    }
}