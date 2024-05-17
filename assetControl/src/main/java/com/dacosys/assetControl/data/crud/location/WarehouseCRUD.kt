package com.dacosys.assetControl.data.crud.location

import com.dacosys.assetControl.data.enums.common.CrudCompleted
import com.dacosys.assetControl.data.enums.common.CrudResult
import com.dacosys.assetControl.data.enums.common.CrudStatus.*
import com.dacosys.assetControl.data.room.entity.location.Warehouse
import com.dacosys.assetControl.data.room.repository.location.WarehouseRepository
import com.dacosys.assetControl.data.webservice.location.WarehouseObject
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import kotlinx.coroutines.*

/**
 * Create, Read, Update and Delete
 */
class WarehouseCRUD {
    class WarehouseAdd {
        private var mCallback: CrudCompleted? = null
        private var warehouseObject: WarehouseObject? = null
        private var crudResult = CrudResult<Warehouse?>(ERROR_INSERT, null)

        fun addParams(callback: CrudCompleted, warehouseObject: WarehouseObject) {
            // list all the parameters like in normal class define
            this.warehouseObject = warehouseObject
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
            if (warehouseObject != null) {
                val wObj = warehouseObject!!
                if (addWarehouse(wObj).status == INSERT_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.Warehouse)
                    }
                } else {
                    crudResult.status = ERROR_INSERT
                    crudResult.itemResult = null
                }
            }
        }

        private fun addWarehouse(wObj: WarehouseObject): CrudResult<Warehouse?> {
            var description = wObj.description
            if (wObj.description.length > 255) {
                description = wObj.description.substring(0, 255)
            }

            val newId = WarehouseRepository().minId
            val w = Warehouse(
                id = newId,
                description = description,
                mActive = wObj.active,
                transferred = 0
            )
            WarehouseRepository().insert(w)

            crudResult.status = INSERT_OK
            crudResult.itemResult = w

            return crudResult
        }
    }

    class WarehouseUpdate {
        private var mCallback: CrudCompleted? = null
        private var warehouseObject: WarehouseObject? = null
        private var crudResult = CrudResult<Warehouse?>(ERROR_UPDATE, null)

        fun addParams(callback: CrudCompleted, warehouseObject: WarehouseObject) {
            // list all the parameters like in normal class define
            this.warehouseObject = warehouseObject
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
            if (warehouseObject != null) {
                val wObj = warehouseObject!!
                if (updateWarehouse(wObj).status == UPDATE_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.Warehouse)
                    }
                }
            } else {
                crudResult.status = ERROR_OBJECT_NULL
                crudResult.itemResult = null
            }
        }

        private fun updateWarehouse(wObj: WarehouseObject): CrudResult<Warehouse?> {
            val w = Warehouse(wObj)
            WarehouseRepository().update(w)
            crudResult.status = UPDATE_OK
            crudResult.itemResult = w

            return crudResult
        }
    }
}