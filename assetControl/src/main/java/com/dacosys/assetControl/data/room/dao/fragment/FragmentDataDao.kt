package com.dacosys.assetControl.data.room.dao.fragment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.fragment.FragmentData
import com.dacosys.assetControl.data.room.entity.fragment.FragmentData.Entry

@Dao
interface FragmentDataDao {
    @Query("$BASIC_SELECT $BASIC_FROM")
    suspend fun select(): List<FragmentData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<FragmentData>)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
    }
}