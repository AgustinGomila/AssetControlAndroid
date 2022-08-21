package com.dacosys.assetControl.dataBase

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics.AssetControl.Companion.getContext
import com.dacosys.assetControl.utils.Statics.Companion.DATABASE_NAME
import com.dacosys.assetControl.utils.Statics.Companion.DATABASE_VERSION
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.attributes.attribute.dbHelper.AttributeDbHelper
import com.dacosys.assetControl.model.assets.attributes.attributeCategory.dbHelper.AttributeCategoryDbHelper
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionDbHelper
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryDbHelper
import com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.dbHelper.AssetManteinanceDbHelper
import com.dacosys.assetControl.model.assets.manteinances.manteinanceStatus.dbHelper.ManteinanceStatusDbHelper
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.dbHelper.ManteinanceTypeDbHelper
import com.dacosys.assetControl.model.assets.manteinances.manteinanceTypeGroup.dbHelper.ManteinanceTypeGroupDbHelper
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomDbHelper
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelTarget.dbHelper.BarcodeLabelTargetDbHelper
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseDbHelper
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaDbHelper
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementDbHelper
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewDbHelper
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentDbHelper
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.dbHelper.DataCollectionRuleDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.dbHelper.DataCollectionRuleContentDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetDbHelper
import com.dacosys.assetControl.model.routes.route.dbHelper.RouteDbHelper
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionDbHelper
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessDbHelper
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentDbHelper
import com.dacosys.assetControl.model.routes.routeProcessStatus.dbHelper.RouteProcessStatusDbHelper
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsDbHelper
import com.dacosys.assetControl.model.users.user.dbHelper.UserDbHelper
import com.dacosys.assetControl.model.users.userPermission.dbHelper.UserPermissionDbHelper
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaDbHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class DataBaseHelper : SQLiteOpenHelper(
    getContext(),
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    @Synchronized
    override fun close() {
        if (myDataBase != null)
            myDataBase!!.close()

        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)

        // Limpiamos la instancia estática de la base de datos para
        // forzar que una nueva se cree la próxima vez.
        cleanInstance()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
    }

    companion object {
        private var myDataBase: SQLiteDatabase? = null

        //////////////////////////////////////////////////////////////
        /////////////// INSTANCIA ESTÁTICA (SINGLETON) ///////////////
        private var sInstance: DataBaseHelper? = null

        fun cleanInstance() {
            sInstance = null
        }

        @Synchronized
        fun getInstance(): DataBaseHelper? {
            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (sInstance == null) {
                sInstance = DataBaseHelper()
            }
            return sInstance
        }

        fun createTables(db: SQLiteDatabase) {
            for (sql in allCommands) {
                println("$sql;")
                db.execSQL(sql)
            }
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
                c.add(com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionDbHelper.CREATE_TABLE)
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
                for (sql in com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionDbHelper.CREATE_INDEX) {
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
                StaticDbHelper.getReadableDb()
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