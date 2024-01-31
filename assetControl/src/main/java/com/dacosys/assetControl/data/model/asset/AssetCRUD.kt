package com.dacosys.assetControl.data.model.asset

import com.dacosys.assetControl.data.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.data.model.common.CrudCompleted
import com.dacosys.assetControl.data.model.common.CrudResult
import com.dacosys.assetControl.data.model.common.CrudStatus.*
import com.dacosys.assetControl.data.webservice.asset.AssetObject
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import kotlinx.coroutines.*

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
            var description = aObj.description
            if (aObj.description.length > 255) {
                description = aObj.description.substring(0, 255)
            }

            var code = aObj.code
            if (aObj.code.length > 45) {
                code = aObj.code.substring(0, 45)
            }

            var ean = aObj.ean
            if (aObj.ean.length > 100) {
                ean = aObj.ean.substring(0, 100)
            }

            var serialNumber = aObj.serial_number
            if (aObj.serial_number.length > 100) {
                serialNumber = aObj.serial_number.substring(0, 100)
            }

            var tempLastAssetReviewDate: String? = null
            if (aObj.last_asset_review_date.isNotEmpty()) {
                tempLastAssetReviewDate = aObj.last_asset_review_date
            }

            val newId = AssetDbHelper().minId

            if (AssetDbHelper().insert(
                    assetId = newId,
                    code = code,
                    description = description,
                    warehouseId = aObj.warehouse_id,
                    warehouseAreaId = aObj.warehouse_area_id,
                    active = aObj.active == 1,
                    ownershipStatus = aObj.ownership_status,
                    status = aObj.status,
                    missingDate = null,
                    itemCategoryId = aObj.item_category_id,
                    transferred = false,
                    originalWarehouseId = aObj.original_warehouse_id,
                    originalWarehouseAreaId = aObj.original_warehouse_area_id,
                    labelNumber = null,
                    manufacturer = aObj.manufacturer,
                    model = aObj.model,
                    serialNumber = serialNumber,
                    condition = aObj.condition,
                    parentId = aObj.parent_id,
                    ean = ean,
                    lastAssetReviewDate = tempLastAssetReviewDate
                )
            ) {
                val a = Asset(aObj)
                a.assetId = newId

                crudResult.status = INSERT_OK
                crudResult.itemResult = a
            } else {
                crudResult.status = ERROR_INSERT
                crudResult.itemResult = null
            }

            return crudResult
        }
    }

    class AssetUpdate {
        var mCallback: CrudCompleted? = null
        private var assetObject: com.dacosys.assetControl.data.webservice.asset.AssetCollectorObject? = null
        private var crudResult = CrudResult<Asset?>(ERROR_UPDATE, null)

        fun addParams(
            callback: CrudCompleted,
            assetObject: com.dacosys.assetControl.data.webservice.asset.AssetCollectorObject
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

        private fun updateAsset(aObj: com.dacosys.assetControl.data.webservice.asset.AssetCollectorObject): CrudResult<Asset?> {
            if (AssetDbHelper().update(aObj)) {
                crudResult.status = UPDATE_OK
                crudResult.itemResult = Asset(aObj)
            } else {
                crudResult.status = ERROR_UPDATE
                crudResult.itemResult = null
            }

            return crudResult
        }
    }
}