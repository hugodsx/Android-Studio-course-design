package com.example.mycollege.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mycollege.R
import com.example.mycollege.databinding.CoursetableinputFragmentBinding
import com.example.mycollege.entity.CoursetableData
import com.example.mycollege.helper.CoursetableDataDbHelper
import com.example.mycollege.vm.CoursetableViewModel

/**
 * input课程的界面
 */

class CoursetableInputFragment : Fragment() {

    private lateinit var courseTableViewModel: CoursetableViewModel
    private lateinit var binding: CoursetableinputFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CoursetableinputFragmentBinding.inflate(inflater, container, false)
        //共享vm
        courseTableViewModel = ViewModelProvider(requireActivity())[CoursetableViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("VM", "onViewCreated: ${courseTableViewModel.coursesData}")
            //获取spnDate选定值
            var spnDateSelected = ""
            binding.spnDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    spnDateSelected = parent?.getItemAtPosition(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

            val navController = findNavController()

            binding.btnSubmit.setOnClickListener{
                //处理空输入
                if(!courseTableViewModel.isLegalInput(binding.edtName)){
                    Toast.makeText(requireContext(), "请输入课程名称", Toast.LENGTH_LONG).show()
                }
                else if(!courseTableViewModel.isLegalInput(binding.edtClassroom)){
                    Toast.makeText(requireContext(), "请输入上课地点", Toast.LENGTH_LONG).show()
                }
                else if(spnDateSelected == "请选择"){
                    Toast.makeText(requireContext(), "请选择上课时间", Toast.LENGTH_LONG).show()
                }
                else if(!courseTableViewModel.isLegalInput(binding.edtBegin)){
                    Toast.makeText(requireContext(), "请输入课程开始节次", Toast.LENGTH_LONG).show()
                }
                else if(!courseTableViewModel.isLegalInput(binding.edtEnd)){
                    Toast.makeText(requireContext(), "请输入课程持续节次", Toast.LENGTH_LONG).show()
                }
                else if(!courseTableViewModel.isLegalInput(binding.edtWeekBegin)){
                    Toast.makeText(requireContext(), "请输入课程开始周数", Toast.LENGTH_LONG).show()
                }
                else if(!courseTableViewModel.isLegalInput(binding.edtWeekEnd)){
                    Toast.makeText(requireContext(), "请输入课程结束周数", Toast.LENGTH_LONG).show()
                }
                else{
                    val tempWeekBegin = binding.edtWeekBegin.text.toString().toInt()
                    val tempWeekEnd = binding.edtWeekEnd.text.toString().toInt()
                    val tempDate = when(spnDateSelected){
                        "周一" -> 1
                        "周二" -> 2
                        "周三" -> 3
                        "周四" -> 4
                        "周五" -> 5
                        "周六" -> 6
                        "周日" -> 7
                        else -> 0
                    }
                    val tempBegin = binding.edtBegin.text.toString().toInt()
                    val tempEnd = binding.edtEnd.text.toString().toInt()
                    //处理显然错误的输入
                    if(tempBegin > tempEnd){
                        Toast.makeText(requireContext(), "课程结束时间不能早于开始时间", Toast.LENGTH_LONG).show()
                    }
                    else if(tempWeekBegin > tempWeekEnd){
                        Toast.makeText(requireContext(), "课程结束周不能早于开始周", Toast.LENGTH_LONG).show()
                    }
                    else if(tempEnd > courseTableViewModel.courseInfo.courseCount){
                        Toast.makeText(requireContext(), "一天内最多只有${courseTableViewModel.courseInfo.courseCount}节课", Toast.LENGTH_LONG).show()
                    }
                    else if(tempBegin < 0 || tempEnd < 0 || tempWeekEnd < 0 || tempWeekBegin < 0){
                        Toast.makeText(requireContext(), "不允许有非负输入", Toast.LENGTH_LONG).show()
                    }
                    else{
                        val tempNote = binding.edtNote.text.toString()
                        val tempName = binding.edtName.text.toString()
                        val tempRoom = binding.edtClassroom.text.toString()
                        val insertingCourse = CoursetableDataDbHelper.createExampleCoursetableData(
                            tempName,
                            tempRoom,
                            tempDate,
                            tempBegin,
                            tempEnd,
                            tempWeekBegin,
                            tempWeekEnd,
                            tempNote)
                        if(isLegalCourse(insertingCourse)){
                            //这里如果有冲突，则会直接toast提示
                            //没有冲突就将其存入数据库中
                            courseTableViewModel.insertCourseData(insertingCourse)
                            Toast.makeText(requireContext(), "添加成功！", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            //取消导航
            binding.btnCancel.setOnClickListener{
                navController.navigate(R.id.coursetableFragment)
            }
            //返回界面导航
            binding.fabBackInput.setOnClickListener {
                navController.navigate(R.id.coursetableFragment)
            }
    }

    //查找是否有冲突课程
    fun isLegalCourse(courseTableData: CoursetableData): Boolean{
        with(courseTableData){
            for(course in courseTableViewModel.coursesData){
                if(course.date == date &&
                    ((course.beginWeek <= beginWeek && course.endWeek >= beginWeek) || (course.beginWeek <= endWeek && course.endWeek >= endWeek))){
                    //假如列表中有一个课程，date值相同，周数上有有重叠部分
                    if((course.beginTime <= beginTime && course.endTime >= beginTime) || (course.beginTime <= endTime && course.endTime >= endTime)){
                        //课程节数上也有重复
                        Toast.makeText(requireContext(), "时间上与该课程有冲突：${course.name}, 周${course.date}, " +
                                "${course.beginWeek}-${course.endWeek}周, ${course.beginTime}-${course.endTime}", Toast.LENGTH_LONG).show()
                        return false
                    }
                }
            }
        }
        return true
    }

}