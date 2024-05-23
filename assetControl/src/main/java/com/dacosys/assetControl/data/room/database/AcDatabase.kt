package com.dacosys.assetControl.data.room.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.data.room.dao.asset.AssetDao
import com.dacosys.assetControl.data.room.dao.attribute.AttributeCategoryDao
import com.dacosys.assetControl.data.room.dao.attribute.AttributeCompositionDao
import com.dacosys.assetControl.data.room.dao.attribute.AttributeDao
import com.dacosys.assetControl.data.room.dao.barcode.BarcodeLabelCustomDao
import com.dacosys.assetControl.data.room.dao.barcode.BarcodeLabelTargetDao
import com.dacosys.assetControl.data.room.dao.category.ItemCategoryDao
import com.dacosys.assetControl.data.room.dao.dataCollection.*
import com.dacosys.assetControl.data.room.dao.location.WarehouseAreaDao
import com.dacosys.assetControl.data.room.dao.location.WarehouseDao
import com.dacosys.assetControl.data.room.dao.maintenance.AssetMaintenanceDao
import com.dacosys.assetControl.data.room.dao.maintenance.MaintenanceStatusDao
import com.dacosys.assetControl.data.room.dao.maintenance.MaintenanceTypeDao
import com.dacosys.assetControl.data.room.dao.maintenance.MaintenanceTypeGroupDao
import com.dacosys.assetControl.data.room.dao.movement.WarehouseMovementContentDao
import com.dacosys.assetControl.data.room.dao.movement.WarehouseMovementDao
import com.dacosys.assetControl.data.room.dao.review.AssetReviewContentDao
import com.dacosys.assetControl.data.room.dao.review.AssetReviewDao
import com.dacosys.assetControl.data.room.dao.review.StatusDao
import com.dacosys.assetControl.data.room.dao.route.*
import com.dacosys.assetControl.data.room.dao.user.UserDao
import com.dacosys.assetControl.data.room.dao.user.UserPermissionDao
import com.dacosys.assetControl.data.room.dao.user.UserWarehouseAreaDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.DATABASE_VERSION
import com.dacosys.assetControl.data.room.entity.asset.AssetEntity
import com.dacosys.assetControl.data.room.entity.attribute.AttributeCategoryEntity
import com.dacosys.assetControl.data.room.entity.attribute.AttributeCompositionEntity
import com.dacosys.assetControl.data.room.entity.attribute.AttributeEntity
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustomEntity
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTargetEntity
import com.dacosys.assetControl.data.room.entity.category.ItemCategoryEntity
import com.dacosys.assetControl.data.room.entity.dataCollection.*
import com.dacosys.assetControl.data.room.entity.location.WarehouseAreaEntity
import com.dacosys.assetControl.data.room.entity.location.WarehouseEntity
import com.dacosys.assetControl.data.room.entity.maintenance.AssetMaintenanceEntity
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceStatusEntity
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeEntity
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceTypeGroupEntity
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContentEntity
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementEntity
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContentEntity
import com.dacosys.assetControl.data.room.entity.review.AssetReviewEntity
import com.dacosys.assetControl.data.room.entity.review.AssetReviewStatusEntity
import com.dacosys.assetControl.data.room.entity.route.*
import com.dacosys.assetControl.data.room.entity.user.UserEntity
import com.dacosys.assetControl.data.room.entity.user.UserPermissionEntity
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseAreaEntity
import java.io.File

@TypeConverters(Converters::class)
@Database(
    entities = [
        AssetEntity::class,
        AssetMaintenanceEntity::class,
        AssetReviewEntity::class,
        AssetReviewContentEntity::class,
        AttributeEntity::class,
        AttributeCategoryEntity::class,
        AttributeCompositionEntity::class,
        BarcodeLabelCustomEntity::class,
        BarcodeLabelTargetEntity::class,
        DataCollectionEntity::class,
        DataCollectionContentEntity::class,
        DataCollectionRuleEntity::class,
        DataCollectionRuleContentEntity::class,
        DataCollectionRuleTargetEntity::class,
        ItemCategoryEntity::class,
        MaintenanceStatusEntity::class,
        MaintenanceTypeEntity::class,
        MaintenanceTypeGroupEntity::class,
        RouteEntity::class,
        RouteCompositionEntity::class,
        RouteProcessEntity::class,
        RouteProcessContentEntity::class,
        RouteProcessStatusEntity::class,
        RouteProcessStepsEntity::class,
        AssetReviewStatusEntity::class,
        UserEntity::class,
        UserPermissionEntity::class,
        UserWarehouseAreaEntity::class,
        WarehouseEntity::class,
        WarehouseAreaEntity::class,
        WarehouseMovementEntity::class,
        WarehouseMovementContentEntity::class,
    ],
    version = DATABASE_VERSION
)
abstract class AcDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun assetMaintenanceCollectorDao(): AssetMaintenanceDao
    abstract fun assetReviewContentDao(): AssetReviewContentDao
    abstract fun assetReviewDao(): AssetReviewDao
    abstract fun attributeCategoryDao(): AttributeCategoryDao
    abstract fun attributeCompositionDao(): AttributeCompositionDao
    abstract fun attributeDao(): AttributeDao
    abstract fun barcodeLabelCustomDao(): BarcodeLabelCustomDao
    abstract fun barcodeLabelTargetDao(): BarcodeLabelTargetDao
    abstract fun dataCollectionContentDao(): DataCollectionContentDao
    abstract fun dataCollectionDao(): DataCollectionDao
    abstract fun dataCollectionRuleContentDao(): DataCollectionRuleContentDao
    abstract fun dataCollectionRuleDao(): DataCollectionRuleDao
    abstract fun dataCollectionRuleTargetDao(): DataCollectionRuleTargetDao
    abstract fun itemCategoryDao(): ItemCategoryDao
    abstract fun maintenanceStatusDao(): MaintenanceStatusDao
    abstract fun maintenanceTypeDao(): MaintenanceTypeDao
    abstract fun maintenanceTypeGroupDao(): MaintenanceTypeGroupDao
    abstract fun routeCompositionDao(): RouteCompositionDao
    abstract fun routeDao(): RouteDao
    abstract fun routeProcessContentDao(): RouteProcessContentDao
    abstract fun routeProcessDao(): RouteProcessDao
    abstract fun routeProcessStatusDao(): RouteProcessStatusDao
    abstract fun routeProcessStepsDao(): RouteProcessStepsDao
    abstract fun statusDao(): StatusDao
    abstract fun userDao(): UserDao
    abstract fun userPermissionDao(): UserPermissionDao
    abstract fun userWarehouseAreaDao(): UserWarehouseAreaDao
    abstract fun warehouseAreaDao(): WarehouseAreaDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun warehouseMovementContentDao(): WarehouseMovementContentDao
    abstract fun warehouseMovementDao(): WarehouseMovementDao

    companion object {
        private val TAG = this::class.java.enclosingClass?.simpleName ?: this::class.java.simpleName
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ac.sqlite"
        private var currentDatabaseName = DATABASE_NAME

        private val context get() = getContext()

        @Volatile
        private var INSTANCE: AcDatabase? = null

        val database: AcDatabase
            get() {
                synchronized(this) {
                    return createDatabase()
                }
            }

        private fun createDatabase(): AcDatabase {
            var instance = INSTANCE
            if (instance == null) {

                // La base de datos no tiene número de versión, se trata como una base SQLite
                // de Milestone13 y se fuerza la migración a primera versión de Room
                if (getDatabaseVersion() == 0) {
                    runMigrationZero()
                }

                instance = Room.databaseBuilder(
                    context = context,
                    klass = AcDatabase::class.java,
                    name = currentDatabaseName
                )
                    .createFromAsset(DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                Log.i(TAG, "NEW Instance: $INSTANCE")
            }
            return instance
        }

        private fun getDatabaseVersion(): Int {
            var version = 0

            val dbPath = context.getDatabasePath(DATABASE_NAME).absolutePath
            val dbFile = File(dbPath)
            if (dbFile.exists()) {
                var db: SQLiteDatabase? = null
                try {
                    db = SQLiteDatabase.openDatabase(
                        dbPath,
                        null,
                        SQLiteDatabase.OPEN_READONLY
                    )
                    version = db.version
                } catch (e: SQLiteException) {
                    version = 0
                } finally {
                    db?.close()
                }
            }
            return version
        }

        fun cleanInstance() {
            if (INSTANCE?.isOpen == true) {
                Log.i(TAG, "CLOSING Instance: $INSTANCE")
                INSTANCE?.close()
            }
            INSTANCE = null
        }

        @Synchronized
        fun backDatabase() {
            cleanInstance()
            currentDatabaseName = DATABASE_NAME
        }

        @Synchronized
        fun changeDatabase(name: String) {
            cleanInstance()
            currentDatabaseName = name
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
            }
        }

        /**
         * Run migration zero
         * Migración que convierte la base de datos SQLite (version 0) en la primera versión de Room
         */
        private fun runMigrationZero() {
            val dbPath = context.getDatabasePath(DATABASE_NAME).absolutePath
            val dbFile = File(dbPath)
            if (dbFile.exists()) {
                var db: SQLiteDatabase? = null
                try {
                    db = SQLiteDatabase.openDatabase(
                        dbPath,
                        null,
                        SQLiteDatabase.OPEN_READWRITE
                    )
                    runMigration(SQLiteDB(db), migrationZero)
                } catch (e: SQLiteException) {
                    println(e)
                } finally {
                    db?.close()
                }
            }
        }

        /**
         * Room migration zero
         * Script de creación de la tabla maestra de Room (version 1)
         * @return
         */
        private fun roomMigrationZero(): List<String> {
            return listOf(
                "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)",
                "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '${IDENTITY_HASH_V1}')"
            )
        }

        private const val IDENTITY_HASH_V1 = "3362b43f956a3ab25f2ce3d7e98c1828"

        private val migrationZero: List<String>
            get() {
                val r = mutableListOf<String>()

                r.addAll(roomMigrationZero())

                r.addAll(AssetEntity.migrationZero())
                r.addAll(AssetMaintenanceEntity.migrationZero())
                r.addAll(AssetReviewEntity.migrationZero())
                r.addAll(AssetReviewContentEntity.migrationZero())
                r.addAll(AssetReviewStatusEntity.migrationZero())
                r.addAll(AttributeEntity.migrationZero())
                r.addAll(AttributeCategoryEntity.migrationZero())
                r.addAll(AttributeCompositionEntity.migrationZero())

                r.addAll(BarcodeLabelCustomEntity.migrationZero())
                r.addAll(BarcodeLabelTargetEntity.migrationZero())

                r.addAll(DataCollectionEntity.migrationZero())
                r.addAll(DataCollectionContentEntity.migrationZero())
                r.addAll(DataCollectionRuleEntity.migrationZero())
                r.addAll(DataCollectionRuleContentEntity.migrationZero())
                r.addAll(DataCollectionRuleTargetEntity.migrationZero())

                r.addAll(ItemCategoryEntity.migrationZero())

                r.addAll(MaintenanceStatusEntity.migrationZero())
                r.addAll(MaintenanceTypeEntity.migrationZero())
                r.addAll(MaintenanceTypeGroupEntity.migrationZero())

                r.addAll(RouteEntity.migrationZero())
                r.addAll(RouteCompositionEntity.migrationZero())
                r.addAll(RouteProcessEntity.migrationZero())
                r.addAll(RouteProcessContentEntity.migrationZero())
                r.addAll(RouteProcessStatusEntity.migrationZero())
                r.addAll(RouteProcessStepsEntity.migrationZero())

                r.addAll(UserEntity.migrationZero())
                r.addAll(UserPermissionEntity.migrationZero())
                r.addAll(UserWarehouseAreaEntity.migrationZero())

                r.addAll(WarehouseEntity.migrationZero())
                r.addAll(WarehouseAreaEntity.migrationZero())
                r.addAll(WarehouseMovementEntity.migrationZero())
                r.addAll(WarehouseMovementContentEntity.migrationZero())

                return r
            }
    }
}