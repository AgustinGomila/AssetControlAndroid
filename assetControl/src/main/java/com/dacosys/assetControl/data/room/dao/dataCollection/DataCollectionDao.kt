package com.dacosys.assetControl.data.room.dao.dataCollection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection.Entry
import com.dacosys.assetControl.data.room.entity.route.RouteProcess
import java.util.*

@Dao
interface DataCollectionDao {
    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id")
    fun selectByCollectorId(id: Long): DataCollection?

    @Query("$BASIC_SELECT $BASIC_FROM $NO_TRANSFERRED_LEFT_JOIN $NO_TRANSFERRED_WHERE")
    fun selectByNoTransferred(): List<DataCollection>

    @Query("SELECT MAX(${Entry.ID}) $BASIC_FROM")
    fun selectMaxId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: DataCollection)


    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue, ${Entry.TRANSFERRED_DATE} = :date WHERE ${Entry.ID} = :oldValue")
    suspend fun updateId(oldValue: Long, newValue: Long, date: Date = Date())


    @Query("DELETE $BASIC_FROM WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query(
        "DELETE $BASIC_FROM " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} NOT IN ( " +
                "SELECT ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_PROCESS_ID} FROM ${rpEntry.TABLE_NAME}) "
    )
    suspend fun deleteOrphans()

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID}"

        private val rpEntry = RouteProcess.Entry
        const val NO_TRANSFERRED_WHERE =
            "WHERE (${Entry.TABLE_NAME}.${Entry.TRANSFERRED_DATE} IS NULL) AND " +
                    "(${Entry.TABLE_NAME}.${Entry.COMPLETED} = 1) AND " +
                    "(((${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} IS NOT NULL) " +
                    "AND(${rpEntry.TABLE_NAME}.${rpEntry.COMPLETED} = 1) " +
                    "AND (${rpEntry.TABLE_NAME}.${rpEntry.TRANSFERRED_DATE} IS NULL)) " +
                    "OR (${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} IS NULL))"
        const val NO_TRANSFERRED_LEFT_JOIN =
            " LEFT OUTER JOIN ${rpEntry.TABLE_NAME} ON ${rpEntry.TABLE_NAME}.${rpEntry.ROUTE_PROCESS_ID} = ${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} " +
                    "OR (${Entry.TABLE_NAME}.${Entry.COLLECTOR_ROUTE_PROCESS_ID} IS NULL)"
    }
}