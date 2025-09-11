package com.example.assetControl.data.crud.asset

import com.example.assetControl.data.enums.common.CrudCompleted
import com.example.assetControl.data.enums.common.CrudResult
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_INSERT
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_OBJECT_NULL
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_UPDATE
import com.example.assetControl.data.enums.common.CrudStatus.INSERT_OK
import com.example.assetControl.data.enums.common.CrudStatus.UPDATE_OK
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.data.webservice.asset.AssetCollectorObject
import com.example.assetControl.data.webservice.asset.AssetObject
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.sync.SyncUpload
import com.example.assetControl.network.utils.Connection.Companion.autoSend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Create, Read, Update and Delete
 */
class AssetCRUD {
    class AssetAdd {
        var mCallback: CrudCompleted? = null
        private var assetObject: AssetObject? = null
        private var crudResult = CrudResult<Asset?>(ERROR_INSERT, null)

        fun addParams(callback: CrudCompleted, assetObject: AssetObject) {
            // list all the parameters like in normal class define
            this.assetObject = assetObject
            this.mCallback = callback
        }

        private val scope = CoroutineScope(Job() + Dispatchers.IO)

        fun cancel() {
            scope.cancel()
        }

        fun execute() {
            scope.launch { doInBackground() }
        }

        private suspend fun doInBackground() {
            coroutineScope {
                suspendFunction()
                mCallback?.onCompleted(crudResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (assetObject != null) {
                val aObj = assetObject!!
                if (addAsset(aObj).status == INSERT_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.Asset)
                    }
                } else {
                    crudResult.status = ERROR_INSERT
                    crudResult.itemResult = null
                }
            }
        }

        private fun addAsset(aObj: AssetObject): CrudResult<Asset?> {
            val asset = Asset(
                code = aObj.code.take(45),
                description = aObj.description.take(255),
                warehouseId = aObj.warehouse_id,
                warehouseAreaId = aObj.warehouse_area_id,
                active = aObj.active,
                ownershipStatus = aObj.ownership_status,
                status = aObj.status,
                missingDate = null,
                itemCategoryId = aObj.item_category_id,
                transferred = 0,
                originalWarehouseId = aObj.original_warehouse_id,
                originalWarehouseAreaId = aObj.original_warehouse_area_id,
                labelNumber = null,
                manufacturer = aObj.manufacturer,
                model = aObj.model,
                serialNumber = aObj.serial_number.take(100),
                condition = aObj.condition,
                parentId = aObj.parent_id,
                ean = aObj.ean.take(100),
                lastAssetReviewDate = aObj.last_asset_review_date.takeIf { it.isNotEmpty() }
            )
            AssetRepository().insert(asset)

            crudResult.status = INSERT_OK
            crudResult.itemResult = asset

            return crudResult
        }
    }

    class AssetUpdate {
        var mCallback: CrudCompleted? = null
        private var assetObject: AssetCollectorObject? = null
        private var crudResult = CrudResult<Asset?>(ERROR_UPDATE, null)

        fun addParams(
            callback: CrudCompleted,
            assetObject: AssetCollectorObject
        ) {
            // list all the parameters like in normal class define
            this.assetObject = assetObject
            this.mCallback = callback
        }

        private val scope = CoroutineScope(Job() + Dispatchers.IO)

        fun cancel() {
            scope.cancel()
        }

        fun execute() {
            scope.launch { doInBackground() }
        }

        private suspend fun doInBackground() {
            coroutineScope {
                suspendFunction()
                mCallback?.onCompleted(crudResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (assetObject != null) {
                val aObj = assetObject!!
                if (updateAsset(aObj).status == UPDATE_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.Asset)
                    }
                }
            } else {
                crudResult.status = ERROR_OBJECT_NULL
                crudResult.itemResult = null
            }
        }

        private fun updateAsset(aObj: AssetCollectorObject): CrudResult<Asset?> {
            val tempAsset = Asset(aObj)
            AssetRepository().update(tempAsset)

            val asset = AssetRepository().selectById(tempAsset.id)
            crudResult.status = UPDATE_OK
            crudResult.itemResult = asset

            return crudResult
        }
    }
}