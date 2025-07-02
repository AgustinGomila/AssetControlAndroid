package com.dacosys.assetControl.data.room.dao.fragment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.fragment.FragmentDataEntity
import com.dacosys.assetControl.data.room.entity.fragment.FragmentDataEntry

@Dao
interface FragmentDataDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    suspend fun select(): List<FragmentDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<FragmentDataEntity>)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${FragmentDataEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${FragmentDataEntry.TABLE_NAME}"
    }
}