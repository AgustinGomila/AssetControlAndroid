package com.dacosys.assetControl.data.room.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.data.room.dao.asset.TempAssetDao
import com.dacosys.assetControl.data.room.dao.fragment.FragmentDataDao
import com.dacosys.assetControl.data.room.dao.location.TempWarehouseAreaDao
import com.dacosys.assetControl.data.room.dao.movement.TempMovementContentDao
import com.dacosys.assetControl.data.room.dao.review.TempReviewContentDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.DATABASE_VERSION
import com.dacosys.assetControl.data.room.entity.asset.TempAsset
import com.dacosys.assetControl.data.room.entity.fragment.FragmentData
import com.dacosys.assetControl.data.room.entity.location.TempWarehouseArea
import com.dacosys.assetControl.data.room.entity.movement.TempMovementContent
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent
import com.dacosys.assetControl.data.room.entity.review.TempReviewContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@TypeConverters(Converters::class)
@Database(
    entities = [FragmentData::class,
        TempAsset::class,
        TempWarehouseArea::class,
        TempMovementContent::class,
        TempReviewContent::class],
    version = DATABASE_VERSION
)
abstract class AcTempDatabase : RoomDatabase() {
    abstract fun fragmentDataDao(): FragmentDataDao
    abstract fun tempAssetDao(): TempAssetDao
    abstract fun tempWarehouseAreaDao(): TempWarehouseAreaDao
    abstract fun tempMovementContentDao(): TempMovementContentDao
    abstract fun tempReviewContentDao(): TempReviewContentDao

    companion object {
        private val TAG = this::class.java.enclosingClass?.simpleName ?: this::class.java.simpleName
        const val DATABASE_VERSION = 2
        const val TEMP_DATABASE_NAME = "ac_temp.sqlite"

        private val context get() = getContext()

        @Volatile
        private var INSTANCE: AcTempDatabase? = null

        val database: AcTempDatabase
            get() {
                synchronized(this) {
                    var instance = INSTANCE
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context = context,
                            klass = AcTempDatabase::class.java,
                            name = TEMP_DATABASE_NAME
                        )
                            .addMigrations(MIGRATION_1_2)
                            .build()
                        INSTANCE = instance
                        android.util.Log.i(TAG, "NEW Instance: $INSTANCE")
                    }
                    return instance
                }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateAssetReviewContent(db)
                migrateWarehouseMovementContent(db)
            }

            private fun migrateWarehouseMovementContent(database: SupportSQLiteDatabase) {
                val entry = WarehouseMovementContent.Entry
                // Aquí ejecutaremos la sentencia SQL para modificar la columna
                database.execSQL("ALTER TABLE ${entry.TABLE_NAME} ADD COLUMN ${entry.ASSET_ID}_TEMP BIGINT NOT NULL")

                // Copiamos los datos de la columna antigua a la nueva
                database.execSQL("UPDATE ${entry.TABLE_NAME} SET ${entry.ASSET_ID}_TEMP = ${entry.ASSET_ID}")

                // Eliminamos la columna antigua
                database.execSQL("ALTER TABLE ${entry.TABLE_NAME} RENAME TO ${entry.TABLE_NAME}_old")

                // Renombramos la nueva columna para que tenga el nombre correcto
                database.execSQL("ALTER TABLE ${entry.TABLE_NAME}_old RENAME TO ${entry.TABLE_NAME}")

                // Eliminamos la columna temporal
                database.execSQL("PRAGMA foreign_keys=off")
                database.execSQL("BEGIN TRANSACTION")
                database.execSQL("CREATE TABLE ${entry.TABLE_NAME}_new (${entry.ASSET_ID} BIGINT NOT NULL)")
                database.execSQL("INSERT INTO ${entry.TABLE_NAME}_new SELECT * FROM ${entry.TABLE_NAME}")
                database.execSQL("DROP TABLE ${entry.TABLE_NAME}")
                database.execSQL("ALTER TABLE ${entry.TABLE_NAME}_new RENAME TO ${entry.TABLE_NAME}")
                database.execSQL("COMMIT TRANSACTION")
                database.execSQL("PRAGMA foreign_keys=on")
            }

            private fun migrateAssetReviewContent(database: SupportSQLiteDatabase) {
                val entry = AssetReviewContent.Entry
                // Aquí ejecutaremos la sentencia SQL para modificar la columna
                database.execSQL("ALTER TABLE ${entry.TABLE_NAME} ADD COLUMN ${entry.ASSET_ID}_TEMP BIGINT NOT NULL")

                // Copiamos los datos de la columna antigua a la nueva
                database.execSQL("UPDATE ${entry.TABLE_NAME} SET ${entry.ASSET_ID}_TEMP = ${entry.ASSET_ID}")

                // Eliminamos la columna antigua
                database.execSQL("ALTER TABLE ${entry.TABLE_NAME} RENAME TO ${entry.TABLE_NAME}_old")

                // Renombramos la nueva columna para que tenga el nombre correcto
                database.execSQL("ALTER TABLE ${entry.TABLE_NAME}_old RENAME TO ${entry.TABLE_NAME}")

                // Eliminamos la columna temporal
                database.execSQL("PRAGMA foreign_keys=off")
                database.execSQL("BEGIN TRANSACTION")
                database.execSQL("CREATE TABLE ${entry.TABLE_NAME}_new (${entry.ASSET_ID} BIGINT NOT NULL)")
                database.execSQL("INSERT INTO ${entry.TABLE_NAME}_new SELECT * FROM ${entry.TABLE_NAME}")
                database.execSQL("DROP TABLE ${entry.TABLE_NAME}")
                database.execSQL("ALTER TABLE ${entry.TABLE_NAME}_new RENAME TO ${entry.TABLE_NAME}")
                database.execSQL("COMMIT TRANSACTION")
                database.execSQL("PRAGMA foreign_keys=on")
            }
        }

        fun cleanInstance() {
            if (INSTANCE?.isOpen == true) {
                android.util.Log.i(TAG, "CLOSING Instance: $INSTANCE")
                INSTANCE?.close()
            }
            INSTANCE = null
        }

        suspend fun cleanDatabase() {
            withContext(Dispatchers.IO) {
                database.tempAssetDao().deleteAll()
                database.tempReviewContentDao().deleteAll()
                database.tempWarehouseAreaDao().deleteAll()
                database.tempAssetDao().deleteAll()
                database.fragmentDataDao().deleteAll()
            }
        }
    }
}