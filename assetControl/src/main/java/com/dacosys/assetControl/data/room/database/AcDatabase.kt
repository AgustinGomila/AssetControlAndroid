package com.dacosys.assetControl.data.room.database

import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.data.room.dao.asset.AssetDao
import com.dacosys.assetControl.data.room.dao.asset.StatusDao
import com.dacosys.assetControl.data.room.dao.attribute.AttributeCategoryDao
import com.dacosys.assetControl.data.room.dao.attribute.AttributeCompositionDao
import com.dacosys.assetControl.data.room.dao.attribute.AttributeDao
import com.dacosys.assetControl.data.room.dao.barcode.BarcodeLabelCustomDao
import com.dacosys.assetControl.data.room.dao.barcode.BarcodeLabelTargetDao
import com.dacosys.assetControl.data.room.dao.category.ItemCategoryDao
import com.dacosys.assetControl.data.room.dao.dataCollection.*
import com.dacosys.assetControl.data.room.dao.location.WarehouseAreaDao
import com.dacosys.assetControl.data.room.dao.location.WarehouseDao
import com.dacosys.assetControl.data.room.dao.manteinance.AssetMaintenanceCollectorDao
import com.dacosys.assetControl.data.room.dao.manteinance.ManteinanceStatusDao
import com.dacosys.assetControl.data.room.dao.manteinance.ManteinanceTypeDao
import com.dacosys.assetControl.data.room.dao.manteinance.ManteinanceTypeGroupDao
import com.dacosys.assetControl.data.room.dao.movement.WarehouseMovementContentDao
import com.dacosys.assetControl.data.room.dao.movement.WarehouseMovementDao
import com.dacosys.assetControl.data.room.dao.review.AssetReviewContentDao
import com.dacosys.assetControl.data.room.dao.review.AssetReviewDao
import com.dacosys.assetControl.data.room.dao.route.*
import com.dacosys.assetControl.data.room.dao.user.UserDao
import com.dacosys.assetControl.data.room.dao.user.UserPermissionDao
import com.dacosys.assetControl.data.room.dao.user.UserWarehouseAreaDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.DATABASE_VERSION
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.asset.Status
import com.dacosys.assetControl.data.room.entity.attribute.Attribute
import com.dacosys.assetControl.data.room.entity.attribute.AttributeCategory
import com.dacosys.assetControl.data.room.entity.attribute.AttributeComposition
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustom
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.data.room.entity.category.ItemCategory
import com.dacosys.assetControl.data.room.entity.dataCollection.*
import com.dacosys.assetControl.data.room.entity.location.Warehouse
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.manteinance.AssetMaintenanceCollector
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceStatus
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceType
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceTypeGroup
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent
import com.dacosys.assetControl.data.room.entity.review.AssetReview
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent
import com.dacosys.assetControl.data.room.entity.route.*
import com.dacosys.assetControl.data.room.entity.user.User
import com.dacosys.assetControl.data.room.entity.user.UserPermission
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseArea

@Database(
    entities = [
        Asset::class,
        AssetMaintenanceCollector::class,
        AssetReview::class,
        AssetReviewContent::class,
        Attribute::class,
        AttributeCategory::class,
        AttributeComposition::class,
        BarcodeLabelCustom::class,
        BarcodeLabelTarget::class,
        DataCollection::class,
        DataCollectionContent::class,
        DataCollectionRule::class,
        DataCollectionRuleContent::class,
        DataCollectionRuleTarget::class,
        ItemCategory::class,
        ManteinanceStatus::class,
        ManteinanceType::class,
        ManteinanceTypeGroup::class,
        Route::class,
        RouteComposition::class,
        RouteProcess::class,
        RouteProcessContent::class,
        RouteProcessStatus::class,
        RouteProcessSteps::class,
        Status::class,
        User::class,
        UserPermission::class,
        UserWarehouseArea::class,
        Warehouse::class,
        WarehouseArea::class,
        WarehouseMovement::class,
        WarehouseMovementContent::class,
    ],
    version = DATABASE_VERSION
)
abstract class AcDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun assetMaintenanceCollectorDao(): AssetMaintenanceCollectorDao
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
    abstract fun manteinanceStatusDao(): ManteinanceStatusDao
    abstract fun manteinanceTypeDao(): ManteinanceTypeDao
    abstract fun manteinanceTypeGroupDao(): ManteinanceTypeGroupDao
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
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "ac.sqlite"

        private val context get() = getContext()

        @Volatile
        private var INSTANCE: AcDatabase? = null

        val database: AcDatabase
            get() {
                synchronized(this) {
                    var instance = INSTANCE
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context = context,
                            klass = AcDatabase::class.java,
                            name = DATABASE_NAME
                        )
                            .createFromAsset(DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build()
                        INSTANCE = instance
                        Log.i(TAG, "NEW Instance: $INSTANCE")
                    }
                    return instance
                }
            }

        fun cleanInstance() {
            if (INSTANCE?.isOpen == true) {
                Log.i(TAG, "CLOSING Instance: $INSTANCE")
                INSTANCE?.close()
            }
            INSTANCE = null
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
            }
        }
    }
}
