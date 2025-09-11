package com.example.assetControl.data.room.repository.attribute

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dao.attribute.AttributeCategoryDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.attribute.AttributeCategory
import com.example.assetControl.data.webservice.attribute.AttributeCategoryObject
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttributeCategoryRepository {
    private val dao: AttributeCategoryDao
        get() = database.attributeCategoryDao()

    suspend fun sync(
        categoryObjects: Array<AttributeCategoryObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.AttributeCategory

        val categories: ArrayList<AttributeCategory> = arrayListOf()
        categoryObjects.mapTo(categories) { AttributeCategory(it) }
        val partial = categories.count()

        withContext(Dispatchers.IO) {
            dao.insert(categories) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = context.getString(R.string.synchronizing_attribute_categories),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }
}
