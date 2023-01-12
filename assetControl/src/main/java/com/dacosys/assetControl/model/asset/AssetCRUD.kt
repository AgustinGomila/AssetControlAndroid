package com.dacosys.assetControl.model.asset

import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.webservice.asset.AssetCollectorObject
import com.dacosys.assetControl.webservice.asset.AssetObject
import kotlinx.coroutines.*

/**
 * Create, Read, Update and Delete
 */
class AssetCRUD {
    companion object {
        class AssetCRUDResult(var resultCode: Int, var asset: Asset?)

        /**
         * Interface que recibirá el resultado de las tareas asincrónicas
         */
        interface TaskCompleted {
            // Define data you like to return from AysncTask
            fun onTaskCompleted(result: AssetCRUDResult)
        }

        const val RC_UPDATE_OK = 1001
        const val RC_ERROR_UPDATE = 2001
        const val RC_ERROR_OBJECT_NULL = 3001
        const val RC_INSERT_OK = 4001
        const val RC_ERROR_INSERT = 5001
    }

    class AssetAdd {
        var mCallback: TaskCompleted? = null
        private var assetObject: AssetObject? = null
        private var assetCRUDResult = AssetCRUDResult(RC_ERROR_INSERT, null)

        fun addParams(callback: TaskCompleted, AssetObject: AssetObject) {
            // list all the parameters like in normal class define
            this.assetObject = AssetObject
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
                mCallback?.onTaskCompleted(assetCRUDResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (assetObject != null) {
                val aObj = assetObject!!
                if (addAsset(aObj).resultCode == RC_INSERT_OK) {
                    if (Statics.autoSend()) {
                        SyncUpload(SyncRegistryType.Asset)
                    }
                } else {
                    assetCRUDResult.resultCode = RC_ERROR_INSERT
                    assetCRUDResult.asset = null
                }
            }
        }

        private fun addAsset(aObj: AssetObject): AssetCRUDResult {
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
                    warehouse_id = aObj.warehouse_id,
                    warehouse_area_id = aObj.warehouse_area_id,
                    active = aObj.active == 1,
                    ownership_status = aObj.ownership_status,
                    status = aObj.status,
                    missing_date = null,
                    item_category_id = aObj.item_category_id,
                    transferred = false,
                    original_warehouse_id = aObj.original_warehouse_id,
                    original_warehouse_area_id = aObj.original_warehouse_area_id,
                    label_number = null,
                    manufacturer = aObj.manufacturer,
                    model = aObj.model,
                    serial_number = serialNumber,
                    condition = aObj.condition,
                    parent_id = aObj.parent_id,
                    ean = ean,
                    last_asset_review_date = tempLastAssetReviewDate
                )
            ) {
                val a = Asset(aObj)
                a.assetId = newId

                assetCRUDResult.resultCode = RC_INSERT_OK
                assetCRUDResult.asset = a
            } else {
                assetCRUDResult.resultCode = RC_ERROR_INSERT
                assetCRUDResult.asset = null
            }

            return assetCRUDResult
        }
    }

    class AssetUpdate {
        var mCallback: TaskCompleted? = null
        private var assetObject: AssetCollectorObject? = null
        private var assetCRUDResult = AssetCRUDResult(RC_ERROR_UPDATE, null)

        fun addParams(callback: TaskCompleted, assetObject: AssetCollectorObject) {
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
                mCallback?.onTaskCompleted(assetCRUDResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (assetObject != null) {
                val aObj = assetObject!!
                if (updateAsset(aObj).resultCode == RC_UPDATE_OK) {
                    if (Statics.autoSend()) {
                        SyncUpload(SyncRegistryType.Asset)
                    }
                }
            } else {
                assetCRUDResult.resultCode = RC_ERROR_OBJECT_NULL
                assetCRUDResult.asset = null
            }
        }

        private fun updateAsset(aObj: AssetCollectorObject): AssetCRUDResult {
            if (AssetDbHelper().update(aObj)) {
                assetCRUDResult.resultCode = RC_UPDATE_OK
                assetCRUDResult.asset = Asset(aObj)
            } else {
                assetCRUDResult.resultCode = RC_ERROR_UPDATE
                assetCRUDResult.asset = null
            }

            return assetCRUDResult
        }
    }
}