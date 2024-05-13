package com.dacosys.assetControl.data.room.dao.manteinance

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.manteinance.AssetMaintenanceCollector
import com.dacosys.assetControl.data.room.entity.manteinance.AssetMaintenanceCollector.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetMaintenanceCollectorDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllAssetMaintenanceCollectors(): Flow<List<AssetMaintenanceCollector>>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ASSET_ID} = :assetId")
    fun getByAssetId(assetId: Long): Flow<List<AssetMaintenanceCollector>>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ASSET_ID} = :assetId AND ${Entry.ID} = :id")
    fun getById(assetId: Long, id: Long): Flow<AssetMaintenanceCollector>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetMaintenanceCollector(assetMaintenanceCollector: AssetMaintenanceCollector)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assetMaintenanceCollectors: List<AssetMaintenanceCollector>)

    @Update
    suspend fun updateAssetMaintenanceCollector(assetMaintenanceCollector: AssetMaintenanceCollector)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ASSET_ID} = :assetId AND ${Entry.ID} = :id")
    suspend fun deleteById(assetId: Long, id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}
