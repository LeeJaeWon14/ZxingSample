package com.example.zxingsample.room

import androidx.room.*

@Dao
interface RecordDAO {
    @Query("SELECT * FROM RecordEntity")
    fun selectRecord() : List<RecordEntity>

    @Update
    fun updateRecord(entity: RecordEntity)

    @Insert
    fun insertRecord(entity: RecordEntity)

    @Delete
    fun deleteRecord(entity: RecordEntity)
}