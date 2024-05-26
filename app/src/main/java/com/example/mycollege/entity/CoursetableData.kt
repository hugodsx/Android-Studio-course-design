package com.example.mycollege.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "courseTable_table")

/**
一行数据包括：该课程持续周数（如1-8周，9-12周）
该课程在一周之内的时间，如周二
该课程在该天开始时间，如第4节
该课程在该天结束时间，如第5节
该课程名称和地点
该课程起始周
同名课程如果有相同名称，允许在不同时间有不同持续时间、开始时间、地点等
不允许在同一周的同一天某段时间内有不同课程
*/

data class CoursetableData(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val classroom: String,
    val date: Int,
    val beginTime: Int,
    val endTime: Int,
    val beginWeek: Int,
    val endWeek: Int,
    val note: String
):Parcelable