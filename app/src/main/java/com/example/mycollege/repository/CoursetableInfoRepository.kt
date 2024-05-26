package com.example.mycollege.repository

import androidx.lifecycle.LiveData
import com.example.mycollege.dao.CoursetableInfoDao
import com.example.mycollege.entity.CoursetableInfo

class CoursetableInfoRepository(private val coursetableInfoDao: CoursetableInfoDao) {
    suspend fun init(coursetableInfo: CoursetableInfo): Long{
        return coursetableInfoDao.init(coursetableInfo)
    }

    suspend fun delete(coursetableInfo: CoursetableInfo){
        return coursetableInfoDao.deleteInfo(coursetableInfo)
    }

    fun getAll(): LiveData<List<CoursetableInfo>>{
        return coursetableInfoDao.getInfo()
    }

    suspend fun update(coursetableInfo: CoursetableInfo){
        return coursetableInfoDao.updateInfo(coursetableInfo)
    }

    suspend fun clear(){
        return coursetableInfoDao.clear()
    }
}