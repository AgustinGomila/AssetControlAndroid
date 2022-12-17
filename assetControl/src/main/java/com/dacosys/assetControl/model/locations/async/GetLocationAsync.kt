package com.dacosys.assetControl.model.locations.async

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaDbHelper
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import kotlinx.coroutines.*

class GetLocationAsync {
    interface GetLocationAsyncListener {
        // Define data you like to return from AysncTask
        fun onGetLocationProgress(
            msg: String,
            progressStatus: ProgressStatus,
            completeList: ArrayList<WarehouseArea>,
        )
    }

    private var mCallback: GetLocationAsyncListener? = null

    private var waDescription: String = ""
    private var wDescription: String = ""
    private var onlyActive: Boolean = true

    private var completeList: ArrayList<WarehouseArea> = ArrayList()

    private fun preExecute() {
        mCallback?.onGetLocationProgress(
            "",
            ProgressStatus.starting,
            completeList
        )
    }

    fun addParams(callback: GetLocationAsyncListener) {
        this.mCallback = callback
    }

    fun addExtraParams(
        waDescription: String,
        wDescription: String,
        onlyActive: Boolean,
    ) {
        this.waDescription = waDescription
        this.wDescription = wDescription
        this.onlyActive = onlyActive
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    fun execute() {
        preExecute()
        scope.launch { doInBackground() }
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground(): Boolean {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false
        }
        return result
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        try {
            getLocationList()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            mCallback?.onGetLocationProgress(
                ex.message.toString(),
                ProgressStatus.crashed,
                completeList
            )
            return@withContext false
        }
        return@withContext true
    }

    private fun getLocationList() {
        mCallback?.onGetLocationProgress(
            "",
            ProgressStatus.running,
            completeList
        )

        if (waDescription.isEmpty() && wDescription.isEmpty()) {
            mCallback?.onGetLocationProgress(
                getContext()
                    .getString(R.string.you_must_enter_at_least_one_letter_in_the_area_or_warehouse_description),
                ProgressStatus.canceled,
                completeList
            )
            return
        }

        try {
            completeList = WarehouseAreaDbHelper().selectByDescription(
                waDescription = waDescription,
                wDescription = wDescription,
                onlyActive = onlyActive
            )
            mCallback?.onGetLocationProgress(
                "",
                ProgressStatus.finished,
                completeList
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)

            mCallback?.onGetLocationProgress(
                ex.message.toString(),
                ProgressStatus.crashed,
                completeList
            )
        }
    }
}