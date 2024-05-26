package com.example.mycollege.repository

import androidx.lifecycle.LiveData
import com.example.mycollege.dao.CoursetableDataDao
import com.example.mycollege.entity.CoursetableData

class CoursetableDataRepository(private val coursetableDataDao: CoursetableDataDao) {

    suspend fun add(coursetableData: CoursetableData):Long{
        return coursetableDataDao.add(coursetableData)
    }
    suspend fun delete(coursetableData: CoursetableData){
        return coursetableDataDao.delete(coursetableData)
    }
    fun getAllCourses(): LiveData<List<CoursetableData>>{
        return coursetableDataDao.getAll()
    }
    suspend fun clear(){
        return coursetableDataDao.clear()
    }
    suspend fun update(coursetableData: CoursetableData) {
        //更改数据
        return coursetableDataDao.update(coursetableData)
    }
}