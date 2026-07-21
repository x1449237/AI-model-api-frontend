package com.aiaggregator.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClusterRecordDao {
    @Query("SELECT * FROM cluster_records ORDER BY created_at DESC")
    fun getAllRecords(): Flow<List<ClusterRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ClusterRecordEntity)

    @Delete
    suspend fun deleteRecord(record: ClusterRecordEntity)

    @Query("DELETE FROM cluster_records")
    suspend fun deleteAllRecords()
}