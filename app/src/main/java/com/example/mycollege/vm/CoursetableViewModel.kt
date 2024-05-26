package com.example.mycollege.vm


import android.app.Application
import android.icu.util.Calendar
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.mycollege.db.CoursetableDataDB
import com.example.mycollege.db.CoursetableInfoDB
import com.example.mycollege.entity.CoursetableData
import com.example.mycollege.entity.CoursetableInfo
import com.example.mycollege.repository.CoursetableDataRepository
import com.example.mycollege.repository.CoursetableInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//继承AndroidViewModel，因为需要context参数
class CoursetableViewModel(app: Application) : AndroidViewModel(app) {
    //数据库实例化
    //实例化Dao
    private val courseTableDataDao = CoursetableDataDB.getDatabase(app).courseTableDataDao()
    private val courseTableDataRepository = CoursetableDataRepository(courseTableDataDao)
    private val courseTableInfoDao = CoursetableInfoDB.getDatabase(app).courseTableInfoDao()
    private val courseTableInfoRepository = CoursetableInfoRepository(courseTableInfoDao)


    var courseInfo = CoursetableInfo(0,14 ,false,
        2024, 5, 20, showSat = true, showSun = true, highLightToday = true, 16
    )
    var coursesData : List<CoursetableData> = listOf()

    //courseData数据库CRUD
    //insert
    fun insertCourseData(courseTableData: CoursetableData){
        viewModelScope.launch(Dispatchers.IO) {
            courseTableDataRepository.add(courseTableData)
        }
    }
    //delete
    fun deleteCourseTableData(courseTableData: CoursetableData){
        viewModelScope.launch(Dispatchers.IO) {
            courseTableDataRepository.delete(courseTableData)
        }
    }
    //clear
    fun clearCourseTableData(){
        viewModelScope.launch(Dispatchers.IO) {
            courseTableDataRepository.clear()
        }
    }
    //update
    fun updateCourseTableData(courseTableData: CoursetableData){
        viewModelScope.launch(Dispatchers.IO) {
            courseTableDataRepository.update(courseTableData)
        }
    }
    //getAll
    fun getAllCourseTableData(): LiveData<List<CoursetableData>>{
        return courseTableDataRepository.getAllCourses()
    }
    //Info数据库
    //init代替添加
    fun initCourseTableInfo(courseTableInfo: CoursetableInfo){
        viewModelScope.launch(Dispatchers.IO) {
            courseTableInfoRepository.init(courseTableInfo)
        }
    }
    //delete
    fun deleteCourseTableInfo(courseTableInfo: CoursetableInfo){
        viewModelScope.launch(Dispatchers.IO) {
            courseTableInfoRepository.delete(courseTableInfo)
        }
    }
    //update
    fun updateCourseTableInfo(courseTableInfo: CoursetableInfo){
        viewModelScope.launch(Dispatchers.IO) {
            courseTableInfoRepository.update(courseTableInfo)
        }
    }
    //getInfo
    fun getCourseTableInfo(): LiveData<List<CoursetableInfo>>{
        return courseTableInfoRepository.getAll()
    }
    //clear
    fun clearCourseTableInfo(){
        viewModelScope.launch(Dispatchers.IO) {
            courseTableInfoRepository.clear()
        }
    }

    //辅助类
    class CNow{
        //获取当前日期信息
        val calendar: Calendar = Calendar.getInstance()
        var nowYear: Int = calendar[Calendar.YEAR]
        var nowMonth: Int = calendar[Calendar.MONTH] + 1
        //月份在系统中从0开始到11，故要加一
        var nowDay: Int = calendar[Calendar.DAY_OF_MONTH]
        //月份中的天，直接获取
    }

    //其他无需存储在数据库的组件
    val now = CNow()
    var showingWeek = 1
    val textViewInfo =  HashMap<Int, String>()

    //工具类的函数
    //计算两日之差
    fun calculateTwoDaysDivide(y_1: Int, m_1: Int, d_1: Int, y_2: Int, m_2: Int, d_2: Int): Int{
        //y2 > y1,...
        var y1 = y_1
        var y2 = y_2
        var m1 = m_1
        var m2 = m_2
        var d1 = d_1
        var d2 = d_2
        var backLater = true
        if(y_1 > y_2 || (y_1 == y_2 && m_1 > m_2) || (y_1 == y_2 && m_1 == m_2 && d_1 > d_2)){
            y1 = y_2
            y2 = y_1
            m1 = m_2
            m2 = m_1
            d1 = d_2
            d2 = d_1
            backLater = false
        }
        var res = 0
        for(i in y1 until y2){
            res += if(isLeafYear(i)){
                366
            } else{
                365
            }
        }
        val monthList : Array<Array<Int>> = arrayOf(
            arrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31),
            arrayOf(0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        )
        for(i in 1 until m1){
            res -= monthList[if(isLeafYear(y1)) 1 else 0][i]
        }
        for(i in 1 until m2){
            res += monthList[if(isLeafYear(y2)) 1 else 0][i]
        }
        return (res + d2 - d1) * if(backLater) 1 else -1
    }
    private fun isLeafYear(year: Int): Boolean{
        return year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)
    }

    //判断是否为空输入
    fun isLegalInput(editText: EditText): Boolean{
        return !("" == editText.text.toString() || null == editText.text)
    }

    //判断是否要高亮该日
    fun shouldHighLight(date: Int): Boolean{
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(
            courseInfo.year,
            courseInfo.month - 1,
            courseInfo.day
        )
        tempCalendar.add(Calendar.DAY_OF_YEAR, (showingWeek - 1) * 7 + date - 1)
        Log.d("ymd11", "${tempCalendar[Calendar.DAY_OF_MONTH]}, ${tempCalendar[Calendar.MONTH]}")
        Log.d("ymd112", "${now.calendar[Calendar.DAY_OF_MONTH]}, ${now.calendar[Calendar.MONTH]}")
        return tempCalendar[Calendar.DAY_OF_MONTH] == now.calendar[Calendar.DAY_OF_MONTH]
                && tempCalendar[Calendar.MONTH] == now.calendar[Calendar.MONTH]
                && tempCalendar[Calendar.YEAR] == now.calendar[Calendar.YEAR]
    }
}