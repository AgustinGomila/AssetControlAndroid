package com.dacosys.assetControl.model.location

import com.dacosys.assetControl.dataBase.location.WarehouseDbHelper
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.webservice.location.WarehouseObject
import kotlinx.coroutines.*

/**
 * Create, Read, Update and Delete
 */
class WarehouseCRUD {
    companion object {
        class WarehouseCRUDResult(var resultCode: Int, var warehouse: Warehouse?)

        /**
         * Interface que recibirá el resultado de las tareas asincrónicas
         */
        interface TaskCompleted {
            // Define data you like to return from AysncTask
            fun onTaskCompleted(result: WarehouseCRUDResult)
        }

        const val RC_UPDATE_OK = 1001
        const val RC_ERROR_UPDATE = 2001
        const val RC_ERROR_OBJECT_NULL = 3001
        const val RC_INSERT_OK = 4001
        const val RC_ERROR_INSERT = 5001
    }

    class WarehouseAdd {
        private var mCallback: TaskCompleted? = null
        private var warehouseObject: WarehouseObject? = null
        private var wCRUDResult = WarehouseCRUDResult(RC_ERROR_INSERT, null)

        fun addParams(callback: TaskCompleted, WarehouseObject: WarehouseObject) {
            // list all the parameters like in normal class define
            this.warehouseObject = WarehouseObject
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
                mCallback?.onTaskCompleted(wCRUDResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (warehouseObject != null) {
                val wObj = warehouseObject!!
                if (addWarehouse(wObj).resultCode == RC_INSERT_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.Warehouse)
                    }
                } else {
                    wCRUDResult.resultCode = RC_ERROR_INSERT
                    wCRUDResult.warehouse = null
                }
            }
        }

        private fun addWarehouse(wObj: WarehouseObject): WarehouseCRUDResult {
            var description = wObj.description
            if (wObj.description.length > 255) {
                description = wObj.description.substring(0, 255)
            }

            val newId = WarehouseDbHelper().minId
            if (WarehouseDbHelper().insert(
                    newId,
                    description,
                    wObj.active == 1,
                    false
                )
            ) {
                val a = Warehouse(wObj)
                a.warehouseId = newId

                wCRUDResult.resultCode = RC_INSERT_OK
                wCRUDResult.warehouse = a
            } else {
                wCRUDResult.resultCode = RC_ERROR_INSERT
                wCRUDResult.warehouse = null
            }

            return wCRUDResult
        }
    }

    class WarehouseUpdate {
        private var mCallback: TaskCompleted? = null
        private var warehouseObject: WarehouseObject? = null
        private var wCRUDResult = WarehouseCRUDResult(RC_ERROR_UPDATE, null)

        fun addParams(callback: TaskCompleted, warehouseObject: WarehouseObject) {
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
                mCallback?.onTaskCompleted(wCRUDResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (warehouseObject != null) {
                val wObj = warehouseObject!!
                if (updateWarehouse(wObj).resultCode == RC_UPDATE_OK) {
                    if (autoSend()) {
                        SyncUpload(SyncRegistryType.Warehouse)
                    }
                }
            } else {
                wCRUDResult.resultCode = RC_ERROR_OBJECT_NULL
                wCRUDResult.warehouse = null
            }
        }

        private fun updateWarehouse(wObj: WarehouseObject): WarehouseCRUDResult {
            if (WarehouseDbHelper().update(wObj)) {
                wCRUDResult.resultCode = RC_UPDATE_OK
                wCRUDResult.warehouse = Warehouse(wObj)
            } else {
                wCRUDResult.resultCode = RC_ERROR_UPDATE
                wCRUDResult.warehouse = null
            }

            return wCRUDResult
        }
    }
}