package com.example.mycollege.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mycollege.entity.CoursetableInfo

/**
 * 关于课程表显示方式的Dao，实际上始终只有一条的数据
 * 所以不他需要用到delete方法
 */

@Dao
interface CoursetableInfoDao {
    //插入函数即为初始化函数，只在应用第一次使用时进行
    @Insert
    suspend fun init(coursetableInfo: CoursetableInfo): Long
    //允许用户删除整个课表
    @Delete
    suspend fun deleteInfo(coursetableInfo: CoursetableInfo)
    //获得全部数据，实际上只有一条数据
    @Query("select* from coursetableInfo_table")
    fun getInfo(): LiveData<List<CoursetableInfo>>
    //修改数据
    @Update
    suspend fun updateInfo(coursetableInfo: CoursetableInfo)
    //删除所有表数据
    @Query("delete from coursetableInfo_table")
    suspend fun clear()
}