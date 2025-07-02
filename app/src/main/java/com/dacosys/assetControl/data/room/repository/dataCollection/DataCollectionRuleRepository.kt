package com.dacosys.assetControl.data.room.repository.dataCollection

import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dao.dataCollection.DataCollectionRuleDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRule
import com.dacosys.assetControl.data.webservice.dataCollection.DataCollectionRuleObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class DataCollectionRuleRepository {
    private val dao: DataCollectionRuleDao
        get() = database.dataCollectionRuleDao()

    fun selectById(id: Long) = runBlocking {
        dao.selectById(id)
    }

    fun selectByDescription(description: String) = runBlocking {
        dao.selectByDescription(description)
    }

    fun selectByTargetAssetIdDescription(assetId: Long, description: String, onlyActive: Boolean) = runBlocking {
        if (onlyActive) dao.selectByTargetAssetIdDescriptionActive(assetId, description)
        else dao.selectByTargetAssetIdDescription(assetId, description)
    }

    fun selectByTargetWarehouseAreaIdDescription(waId: Long, description: String, onlyActive: Boolean) = runBlocking {
        if (onlyActive) dao.selectByTargetWarehouseAreaIdDescriptionActive(waId, description)
        else dao.selectByTargetWarehouseAreaIdDescription(waId, description)
    }

    fun selectByTargetItemCategoryIdDescription(icId: Long, description: String, onlyActive: Boolean) = runBlocking {
        if (onlyActive) dao.selectByTargetItemCategoryIdDescriptionActive(icId, description)
        else dao.selectByTargetItemCategoryIdDescription(icId, description)
    }


    suspend fun sync(
        routeObjects: Array<DataCollectionRuleObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.DataCollectionRule

        val routes: ArrayList<DataCollectionRule> = arrayListOf()
        routeObjects.mapTo(routes) { DataCollectionRule(it) }
        val partial = routes.count()

        withContext(Dispatchers.IO) {
            dao.insert(routes) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = context.getString(R.string.synchronizing_data_collection_rules),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }
}