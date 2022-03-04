package com.example.zxingsample.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecordEntity::class], version = 1, exportSchema = true)
abstract class MyRoomDatabase : RoomDatabase(){
    abstract fun getRoomDAO() : RecordDAO

    companion object {
        private var instance: MyRoomDatabase? = null
        fun getInstance(context: Context) : MyRoomDatabase {
            instance?.let {
                return it
            } ?: run {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyRoomDatabase::class.java,
                    "recordRoom.db"
                ).build()
                return instance!!
            }
        }
    }
}