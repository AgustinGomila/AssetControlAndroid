package com.dacosys.assetControl.data.room.repository.asset

import com.dacosys.assetControl.data.room.dao.asset.AssetDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.asset.Asset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetRepository {
    private val dao: AssetDao by lazy {
        database.assetDao()
    }

    fun getAllAssets() = dao.getAllAssets()

    suspend fun insertAsset(asset: Asset) {
        withContext(Dispatchers.IO) {
            dao.insertAsset(asset)
        }
    }

    suspend fun insertAll(assets: List<Asset>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(assets)
        }
    }

    suspend fun updateAsset(asset: Asset) {
        withContext(Dispatchers.IO) {
            dao.updateAsset(asset)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
