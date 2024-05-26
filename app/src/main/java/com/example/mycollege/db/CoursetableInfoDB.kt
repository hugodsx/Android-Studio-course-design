package com.example.mycollege.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mycollege.dao.CoursetableInfoDao
import com.example.mycollege.entity.CoursetableInfo

/**
 * 课程表显示方式的数据库
 */

@Database(
    entities = [CoursetableInfo::class],  //表明本数据库中有几张表
    version = 1,  //当前数据库的版本号（重要！）
    exportSchema = false //不导出Schema
)
 abstract class CoursetableInfoDB : RoomDatabase() {
     //声明Dao
     abstract fun courseTableInfoDao(): CoursetableInfoDao
     companion object{
         @Volatile
         private var INSTANCE: CoursetableInfoDB? = null
         fun getDatabase(context: Context): CoursetableInfoDB{
             val tempInstance = INSTANCE
             if(tempInstance != null){
                 return tempInstance
             }
             synchronized(this){
                 val instance = Room.databaseBuilder(
                     context.applicationContext,
                     CoursetableInfoDB::class.java, "coursetableInfo_database"
                 ).build()
                 INSTANCE = instance
                 return instance
             }
         }
     }
}