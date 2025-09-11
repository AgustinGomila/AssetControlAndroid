package com.example.assetControl.data.room.repository.barcode

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dao.barcode.BarcodeLabelCustomDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.barcode.BarcodeLabelCustom
import com.example.assetControl.data.webservice.barcode.BarcodeLabelCustomObject
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class BarcodeLabelCustomRepository {
    private val dao: BarcodeLabelCustomDao
        get() = database.barcodeLabelCustomDao()

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectByBarcodeLabelTargetId(barcodeLabelTargetId: Int, onlyActive: Boolean): List<BarcodeLabelCustom> =
        runBlocking {
            if (onlyActive) dao.selectByBarcodeLabelTargetIdActive(barcodeLabelTargetId)
            else dao.selectByBarcodeLabelTargetId(barcodeLabelTargetId)
        }


    suspend fun sync(
        assetsObj: Array<BarcodeLabelCustomObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.BarcodeLabelCustom

        val labels: ArrayList<BarcodeLabelCustom> = arrayListOf()
        assetsObj.mapTo(labels) { BarcodeLabelCustom(it) }
        val partial = labels.count()

        withContext(Dispatchers.IO) {
            dao.insert(labels) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = context.getString(R.string.synchronizing_barcode_labels),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }

}