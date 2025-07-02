package com.dacosys.assetControl.data.room.repository.attribute

import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dao.attribute.AttributeDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.attribute.Attribute
import com.dacosys.assetControl.data.webservice.attribute.AttributeObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttributeRepository {
    private val dao: AttributeDao
        get() = database.attributeDao()

    suspend fun sync(
        attributeObjects: Array<AttributeObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.Attribute

        val attributes: ArrayList<Attribute> = arrayListOf()
        attributeObjects.mapTo(attributes) { Attribute(it) }
        val partial = attributes.count()

        withContext(Dispatchers.IO) {
            dao.insert(attributes) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = context.getString(R.string.synchronizing_attributes),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }
}