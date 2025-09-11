package com.example.assetControl.data.room.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.data.room.dao.asset.TempAssetDao
import com.example.assetControl.data.room.dao.fragment.FragmentDataDao
import com.example.assetControl.data.room.dao.location.TempWarehouseAreaDao
import com.example.assetControl.data.room.dao.movement.TempMovementContentDao
import com.example.assetControl.data.room.dao.review.TempReviewContentDao
import com.example.assetControl.data.room.database.AcTempDatabase.Companion.DATABASE_VERSION
import com.example.assetControl.data.room.entity.asset.TempAssetEntity
import com.example.assetControl.data.room.entity.fragment.FragmentDataEntity
import com.example.assetControl.data.room.entity.location.TempWarehouseAreaEntity
import com.example.assetControl.data.room.entity.movement.TempMovementContentEntity
import com.example.assetControl.data.room.entity.review.TempReviewContentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@TypeConverters(Converters::class)
@Database(
    entities = [FragmentDataEntity::class,
        TempAssetEntity::class,
        TempWarehouseAreaEntity::class,
        TempMovementContentEntity::class,
        TempReviewContentEntity::class],
    version = DATABASE_VERSION,
    exportSchema = true
)
abstract class AcTempDatabase : RoomDatabase() {
    abstract fun fragmentDataDao(): FragmentDataDao
    abstract fun tempAssetDao(): TempAssetDao
    abstract fun tempWarehouseAreaDao(): TempWarehouseAreaDao
    abstract fun tempMovementContentDao(): TempMovementContentDao
    abstract fun tempReviewContentDao(): TempReviewContentDao

    companion object {
        private val TAG = this::class.java.enclosingClass?.simpleName ?: this::class.java.simpleName
        const val DATABASE_VERSION = 1
        const val TEMP_DATABASE_NAME = "ac_temp.sqlite"

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
                            .build()
                        INSTANCE = instance
                        android.util.Log.i(TAG, "NEW Instance: $INSTANCE")
                    }
                    return instance
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