package com.dacosys.assetControl.model.location

import com.dacosys.assetControl.dataBase.location.WarehouseAreaDbHelper
import com.dacosys.assetControl.model.common.CrudCompleted
import com.dacosys.assetControl.model.common.CrudResult
import com.dacosys.assetControl.model.common.CrudStatus.*
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.webservice.location.WarehouseAreaObject
import kotlinx.coroutines.*

/**
 * Create, Read, Update and Delete
 */
class WarehouseAreaCRUD {
    class WarehouseAreaAdd {
        private var mCallback: CrudCompleted? = null
        private var warehouseAreaObject: WarehouseAreaObject? = null
        private var crudResult = CrudResult<WarehouseArea?>(ERROR_INSERT, null)

        fun addParams(callback: CrudCompleted, areaObject: WarehouseAreaObject) {
            // list all the parameters like in normal class define
            this.warehouseAreaObject = areaObject
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
            if (warehouseAreaObject != null) {
                val waObj = warehouseAreaObject!!
                if (addWarehouseArea(waObj).status == INSERT_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.WarehouseArea)
                    }
                } else {
                    crudResult.status = ERROR_INSERT
                    crudResult.itemResult = null
                }
            }
        }

        private fun addWarehouseArea(waObj: WarehouseAreaObject): CrudResult<WarehouseArea?> {
            var description = waObj.description
            if (waObj.description.length > 255) {
                description = waObj.description.substring(0, 255)
            }

            val newId = WarehouseAreaDbHelper().minId
            if (WarehouseAreaDbHelper().insert(
                    newId,
                    description,
                    waObj.active == 1,
                    waObj.warehouse_id,
                    false
                )
            ) {
                val a = WarehouseArea(waObj)
                a.warehouseAreaId = newId

                crudResult.status = INSERT_OK
                crudResult.itemResult = a
            } else {
                crudResult.status = ERROR_INSERT
                crudResult.itemResult = null
            }

            return crudResult
        }
    }

    class WarehouseAreaUpdate {
        private var mCallback: CrudCompleted? = null
        private var warehouseAreaObject: WarehouseAreaObject? = null
        private var crudResult = CrudResult<WarehouseArea?>(ERROR_UPDATE, null)

        fun addParams(callback: CrudCompleted, warehouseAreaObject: WarehouseAreaObject) {
            // list all the parameters like in normal class define
            this.warehouseAreaObject = warehouseAreaObject
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
            if (warehouseAreaObject != null) {
                val waObj = warehouseAreaObject!!
                if (updateWarehouseArea(waObj).status == UPDATE_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.WarehouseArea)
                    }
                }
            } else {
                crudResult.status = ERROR_OBJECT_NULL
                crudResult.itemResult = null
            }
        }

        private fun updateWarehouseArea(waObj: WarehouseAreaObject): CrudResult<WarehouseArea?> {
            if (WarehouseAreaDbHelper().update(waObj)) {
                crudResult.status = UPDATE_OK
                crudResult.itemResult = WarehouseArea(waObj)
            } else {
                crudResult.status = ERROR_UPDATE
                crudResult.itemResult = null
            }

            return crudResult
        }
    }
}