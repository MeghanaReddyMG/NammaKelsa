package com.nammakelsa.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers")
    fun getAllWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE uid = :uid")
    fun getWorker(uid: String): Flow<WorkerEntity?>

    @Query("SELECT * FROM workers WHERE skill = :skill AND isAvailable = 1")
    fun getAvailableWorkersBySkill(skill: String): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE isAvailable = 1")
    fun getAllAvailableWorkers(): Flow<List<WorkerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<WorkerEntity>)

    @Query("DELETE FROM workers WHERE uid = :uid")
    suspend fun deleteWorker(uid: String)
}
