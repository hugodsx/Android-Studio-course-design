package com.example.mycollege.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mycollege.dao.CoursetableDataDao
import com.example.mycollege.entity.CoursetableData

/**
 * 课程内容数据库
 */
@Database(
    entities = [CoursetableData::class],  //表明本数据库中有几张表
    version = 1,  //当前数据库的版本号（重要！）
    exportSchema = false //不导出Schema
)
abstract class CoursetableDataDB : RoomDatabase() {
    //声明Dao
    abstract fun courseTableDataDao(): CoursetableDataDao
    companion object{
        @Volatile
        private var INSTANCE: CoursetableDataDB? = null
        fun getDatabase(context: Context): CoursetableDataDB{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CoursetableDataDB::class.java, "coursetable_database"//coursetableData_database
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}