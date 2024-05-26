package com.example.mycollege.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mycollege.entity.CoursetableData

/**
 * 存储所有输入的课程的Dao
 */

@Dao
interface CoursetableDataDao {

    @Insert
    suspend fun add(coursetabledata: CoursetableData):Long

    @Delete
    suspend fun delete(coursetabledata: CoursetableData)

    @Query("delete from coursetable_table")
    suspend fun clear()

    @Update
    suspend fun update(coursetabledata: CoursetableData)

    @Query("select * from coursetable_table order by beginWeek asc")
    fun getAll(): LiveData<List<CoursetableData>>

}