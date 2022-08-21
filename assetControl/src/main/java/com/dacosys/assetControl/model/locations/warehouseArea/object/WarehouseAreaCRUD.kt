package com.dacosys.assetControl.model.locations.warehouseArea.`object`

import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaDbHelper
import com.dacosys.assetControl.model.locations.warehouseArea.wsObject.WarehouseAreaObject
import com.dacosys.assetControl.sync.functions.SyncRegistryType
import com.dacosys.assetControl.sync.functions.SyncUpload
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/**
 * Create, Read, Update and Delete
 */
class WarehouseAreaCRUD {
    companion object {
        class WarehouseAreaCRUDResult(var resultCode: Int, var warehouseArea: WarehouseArea?)

        /**
         * Interface que recibirá el resultado de las tareas asincrónicas
         */
        interface TaskCompleted {
            // Define data you like to return from AysncTask
            fun onTaskCompleted(result: WarehouseAreaCRUDResult)
        }

        const val RC_UPDATE_OK = 1001
        const val RC_ERROR_UPDATE = 2001
        const val RC_ERROR_OBJECT_NULL = 3001
        const val RC_INSERT_OK = 4001
        const val RC_ERROR_INSERT = 5001
    }

    class WarehouseAreaAdd {
        private var mCallback: TaskCompleted? = null
        private var warehouseAreaObject: WarehouseAreaObject? = null
        private var waCRUDResult = WarehouseAreaCRUDResult(RC_ERROR_INSERT, null)

        fun addParams(callback: TaskCompleted, WarehouseAreaObject: WarehouseAreaObject) {
            // list all the parameters like in normal class define
            this.warehouseAreaObject = WarehouseAreaObject
            this.mCallback = callback
        }

        fun execute() {
            doInBackground()
        }

        private var job: Job? = null

        private fun doInBackground() {
            runBlocking {
                job = launch { suspendFunction() }
                job?.join()
                mCallback?.onTaskCompleted(waCRUDResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (warehouseAreaObject != null) {
                val waObj = warehouseAreaObject!!
                if (addWarehouseArea(waObj).resultCode == RC_INSERT_OK) {
                    if (Statics.autoSend()) {
                        thread {
                            val sync = SyncUpload()
                            sync.addRegistryToSync(SyncRegistryType.WarehouseArea)
                            sync.execute()
                        }
                    }
                } else {
                    waCRUDResult.resultCode = RC_ERROR_INSERT
                    waCRUDResult.warehouseArea = null
                }
            }
        }

        private fun addWarehouseArea(waObj: WarehouseAreaObject): WarehouseAreaCRUDResult {
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

                waCRUDResult.resultCode = RC_INSERT_OK
                waCRUDResult.warehouseArea = a
            } else {
                waCRUDResult.resultCode = RC_ERROR_INSERT
                waCRUDResult.warehouseArea = null
            }

            return waCRUDResult
        }
    }

    class WarehouseAreaUpdate {
        private var mCallback: TaskCompleted? = null
        private var warehouseAreaObject: WarehouseAreaObject? = null
        private var waCRUDResult = WarehouseAreaCRUDResult(RC_ERROR_UPDATE, null)

        fun addParams(callback: TaskCompleted, warehouseAreaObject: WarehouseAreaObject) {
            // list all the parameters like in normal class define
            this.warehouseAreaObject = warehouseAreaObject
            this.mCallback = callback
        }

        fun execute() {
            doInBackground()
        }

        private var job: Job? = null

        private fun doInBackground() {
            runBlocking {
                job = launch { suspendFunction() }
                job?.join()
                mCallback?.onTaskCompleted(waCRUDResult)
            }
        }

        private suspend fun suspendFunction() = withContext(Dispatchers.IO) {
            if (warehouseAreaObject != null) {
                val waObj = warehouseAreaObject!!
                if (updateWarehouseArea(waObj).resultCode == RC_UPDATE_OK) {
                    if (Statics.autoSend()) {
                        thread {
                            val sync = SyncUpload()
                            sync.addRegistryToSync(SyncRegistryType.WarehouseArea)
                            sync.execute()
                        }
                    }
                }
            } else {
                waCRUDResult.resultCode = RC_ERROR_OBJECT_NULL
                waCRUDResult.warehouseArea = null
            }
        }

        private fun updateWarehouseArea(waObj: WarehouseAreaObject): WarehouseAreaCRUDResult {
            if (WarehouseAreaDbHelper().update(waObj)) {
                waCRUDResult.resultCode = RC_UPDATE_OK
                waCRUDResult.warehouseArea = WarehouseArea(waObj)
            } else {
                waCRUDResult.resultCode = RC_ERROR_UPDATE
                waCRUDResult.warehouseArea = null
            }

            return waCRUDResult
        }
    }
}