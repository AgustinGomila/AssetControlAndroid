package com.dacosys.assetControl.data.room.dao.fragment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.dacosys.assetControl.data.room.entity.fragment.FragmentData

@Dao
interface FragmentDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fragmentData: FragmentData)

    @Delete
    suspend fun delete(fragmentData: FragmentData)

}