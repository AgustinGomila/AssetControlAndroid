package com.example.assetControl.data.async.location

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.utils.errorLog.ErrorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetLocationAsync {
    interface GetLocationAsyncListener {
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
            result = deferred?.await() == true
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
                context
                    .getString(R.string.you_must_enter_at_least_one_letter_in_the_area_or_warehouse_description),
                ProgressStatus.canceled,
                completeList
            )
            return
        }

        try {
            completeList = ArrayList(
                WarehouseAreaRepository().selectByDescription(
                    waDescription = waDescription,
                    wDescription = wDescription,
                    onlyActive = onlyActive
                )
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