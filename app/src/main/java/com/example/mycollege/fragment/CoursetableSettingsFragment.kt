package com.example.mycollege.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mycollege.R
import com.example.mycollege.databinding.CoursetablesettingsFragmentBinding
import com.example.mycollege.entity.CoursetableInfo
import com.example.mycollege.vm.CoursetableViewModel
import java.util.Calendar

/**
 * 设置界面的fragment
 */

class CoursetableSettingsFragment : Fragment() {

    private lateinit var binding: CoursetablesettingsFragmentBinding
    private lateinit var coursetableViewModel: CoursetableViewModel
    private lateinit var temp: CoursetableInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //视图绑定
        binding = CoursetablesettingsFragmentBinding.inflate(inflater, container, false)
        //共享vm
        coursetableViewModel = ViewModelProvider(requireActivity())
            .get(CoursetableViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //页面初始化
        with(binding){
            var setYear = 2024
            var setMonth = 5
            var setDay = 20

            //这里使用observe，因为可能在打开时vm中暂存的info变量还没有获取
            //而显示的内容都是基于数据库中存储的值来确定的
            coursetableViewModel.getCourseTableInfo().observe(viewLifecycleOwner){
                swiShowSatSettings.isChecked = it[0].showSat
                swiShowSunSettings.isChecked = it[0].showSun
                swihighLightSettings.isChecked = it[0].highLightToday
                swiUseChooseDateSettings.isChecked = it[0].haveGotFirstWeek
                temp = it[0]
            }
            coursetableViewModel.getCourseTableInfo().removeObservers(viewLifecycleOwner)

            //选择日期的控件初始化
            cldChooseDateSettings.init(
                coursetableViewModel.courseInfo.year,
                coursetableViewModel.courseInfo.month - 1,
                coursetableViewModel.courseInfo.day, null
            )
            cldChooseDateSettings.setOnDateChangedListener{ _, year, monthOfYear, dayOfMonth ->
                // 获取用户选择的日期
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, monthOfYear, dayOfMonth)
                Log.i("TAG", "$year, ${monthOfYear + 1}, $dayOfMonth")
                //根据选择的日期，算出当前是第几周或者哪一周开始是第一周
                val dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK)
                if(dayOfWeek == 1){
                    selectedCalendar.add(Calendar.DAY_OF_MONTH, -6)
                }
                else{
                    selectedCalendar.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - 2))
                }

                val weekYear: Int = selectedCalendar.get(Calendar.YEAR)
                val weekMonth: Int = selectedCalendar.get(Calendar.MONTH) + 1
                val weekDayOfMonth: Int = selectedCalendar.get(Calendar.DAY_OF_MONTH)
                //计算两日期间日期差
                val divideDay = coursetableViewModel.calculateTwoDaysDivide(
                    weekYear, weekMonth, weekDayOfMonth,
                    coursetableViewModel.now.nowYear, coursetableViewModel.now.nowMonth, coursetableViewModel.now.nowDay
                )
                //第一周在今天之前
                if(divideDay >= 0){
                    tvChooseDateShow.text = "当前是在第${divideDay/7 + 1}周"
                }
                //之后
                else{
                    tvChooseDateShow.text = "第一周在${weekYear}年${weekMonth}月${weekDayOfMonth}日开始"
                }
                setYear = weekYear
                setMonth = weekMonth
                setDay = weekDayOfMonth
            }

            fabBackSetting.setOnClickListener {
                val navController = findNavController()
                navController.navigate(R.id.coursetableFragment)
            }

            //删除所有课程内容
            btnDeleteSettings.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                builder.setTitle("确认删除？")
                builder.setMessage("删除后，课表信息将全部清空，无法保留")
                // 添加确认按钮
                builder.setPositiveButton("确认"
                ) { dialogInterface, _ -> // 关闭对话框
                    coursetableViewModel.clearCourseTableData()
                    Toast.makeText(requireContext(), "删除成功！", Toast.LENGTH_LONG).show()
                    dialogInterface.dismiss()
                }

                // 添加删除按钮
                builder.setNegativeButton("取消"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }

                // 显示对话框
                val dialog = builder.create()
                dialog.show()

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED)
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            }

            btnConfirmSettings.setOnClickListener{
                //确定按钮的点击处理
                val tempCourseCount = edtInputCountSettings.text.toString().toIntOrNull()
                val courseCount = tempCourseCount ?: coursetableViewModel.courseInfo.courseCount
                val showSat = swiShowSatSettings.isChecked
                val showSun = swiShowSunSettings.isChecked
                val gotFirstWeek = swiUseChooseDateSettings.isChecked
                val highLight = swihighLightSettings.isChecked
                val tempCoursetableInfo = CoursetableInfo(temp.id, courseCount, gotFirstWeek, setYear, setMonth, setDay,
                    showSat, showSun, highLight, temp.latestweek)
                Log.d("temp_id", "onViewCreated: ${temp.id}")
                coursetableViewModel.updateCourseTableInfo(tempCoursetableInfo)
                Toast.makeText(requireContext(), "修改成功！", Toast.LENGTH_SHORT).show()
            }
        }
    }
}