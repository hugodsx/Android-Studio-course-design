package com.example.mycollege.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
@Entity(tableName = "courseTableInfo_table")
//这里记录了课程表中相关性数据，包括个性化调整等
//相关操作只有初始化和修改，不能删除或者添加
data class CoursetableInfo (
    //主键为id变量，实际中不使用
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    //记录课表中一天的总节数，默认为13节
    val courseCount: Int,
    //记录课表是否定义了第一周时间
    val haveGotFirstWeek: Boolean,
    //记录第一周周一的日期
    val year: Int,
    val month: Int,
    val day: Int,
    //这些日期如果在没有设定情况下，建议默认为2000年1月1日，以防出现严重错误
    //记录用户是否点击显示周六
    val showSat: Boolean,
    //记录是否显示周日
    val showSun: Boolean,
    //记录是否高亮今日
    val highLightToday: Boolean,
    //记录最后有课一周是哪一周
    val latestweek: Int
    //待补充
):Parcelable