package com.example.whatsappsaver.data.local.dao

import androidx.room.*
import com.example.whatsappsaver.data.local.entity.AiHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface AiHistoryDao {
    @Query("SELECT * FROM ai_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AiHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: AiHistory): Long

    @Delete
    suspend fun delete(history: AiHistory)

    @Query("DELETE FROM ai_history")
    suspend fun deleteAll()
}
