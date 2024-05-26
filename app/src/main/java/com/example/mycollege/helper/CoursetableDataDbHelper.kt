package com.example.mycollege.helper

import com.example.mycollege.entity.CoursetableData

object CoursetableDataDbHelper{
    //用于创建coursetableData的数据实体类
    fun createExampleCoursetableData
                (name: String,
                 classroom: String,
                 date: Int,
                 begin: Int,
                 end: Int,
                 weekBegin: Int,
                 weekEnd: Int,
                 note: String): CoursetableData{
        return CoursetableData( 0, name, classroom, date, begin, end, weekBegin, weekEnd, note)
    }
}
