package com.aiaggregator.app.data

import androidx.room.*

@Entity(tableName = "cluster_records")
data class ClusterRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "prompt") val prompt: String,
    @ColumnInfo(name = "model_ids") val modelIds: String, // JSON array
    @ColumnInfo(name = "results_json") val resultsJson: String, // JSON array of results
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)