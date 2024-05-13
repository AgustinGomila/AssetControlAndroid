package com.dacosys.assetControl.data.room.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.data.room.dao.asset.TempAssetDao
import com.dacosys.assetControl.data.room.dao.fragment.FragmentDataDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.DATABASE_VERSION
import com.dacosys.assetControl.data.room.entity.asset.TempAsset
import com.dacosys.assetControl.data.room.entity.fragment.FragmentData

@Database(
    entities = [FragmentData::class, TempAsset::class],
    version = DATABASE_VERSION
)
abstract class AcTempDatabase : RoomDatabase() {
    abstract fun fragmentDataDao(): FragmentDataDao
    abstract fun tempAssetDao(): TempAssetDao

    companion object {
        private val TAG = this::class.java.enclosingClass?.simpleName ?: this::class.java.simpleName
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "ac_temp.sqlite"

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
                            name = DATABASE_NAME
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
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        fun cleanInstance() {
            if (INSTANCE?.isOpen == true) {
                android.util.Log.i(TAG, "CLOSING Instance: $INSTANCE")
                INSTANCE?.close()
            }
            INSTANCE = null
        }
    }
}