package com.example.mycollege.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mycollege.R
import com.example.mycollege.databinding.CoursetableFragmentBinding
import com.example.mycollege.entity.CoursetableData
import com.example.mycollege.helper.CoursetableDataDbHelper
import com.example.mycollege.vm.CoursetableViewModel

/**
 * 课程表显示的主体
 */

class CoursetableFragment : Fragment() {

    private lateinit var binding: CoursetableFragmentBinding
    private lateinit var coursetableViewModel: CoursetableViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //启用视图绑定
        binding = CoursetableFragmentBinding.inflate(inflater, container, false)
        //实例化vm
        coursetableViewModel = ViewModelProvider(requireActivity())[CoursetableViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //在binding内
        with(binding){
            //导航到添加课程栏
            fabAdd.setOnClickListener {
                val navController = findNavController()
                navController.navigate(R.id.coursetableInputFragment)
            }
            //导航到设置栏
            fabSettings.setOnClickListener {
                val navController = findNavController()
                navController.navigate(R.id.coursetableSettingsFragment)
            }
            //上一周
            btnPrevious.setOnClickListener{
                //处理周数小于1的情况
                //这里注意，如果是在showWeek<=1时，点击上一周将无法回头
                if(coursetableViewModel.showingWeek <= 1){
                    Toast.makeText(requireContext(), "已经到头了", Toast.LENGTH_SHORT).show()
                }
                else{
                    deleteOutText()
                    coursetableViewModel.showingWeek--
                    binding.tvWeekTitle.text = String.format(resources.getString(R.string.weekTitle), coursetableViewModel.showingWeek)
                    addInText()
                }
            }
            //下一周
            btnNext.setOnClickListener{
                //处理超限情况，其实可以不处理，但是处理了会好一点
                if(coursetableViewModel.showingWeek > coursetableViewModel.courseInfo.latestweek){
                    Toast.makeText(requireContext(), "已经到头了", Toast.LENGTH_LONG).show()
                }
                else{
                    deleteOutText()
                    coursetableViewModel.showingWeek++
                    binding.tvWeekTitle.text = String.format(resources.getString(R.string.weekTitle), coursetableViewModel.showingWeek)
                    addInText()
                }
            }
            /**
            上面这些按钮点击响应操作，在可以点击的时候已经全部实例化了info和data数据，所以不会有数据库尚未读取的风险
             */

            //初始化vm中info数据
            coursetableViewModel.getCourseTableInfo().observe(viewLifecycleOwner){
                if(it.isEmpty()){
                    //如果是第一次打开应用，没有相应数据，则初始化一次
                    coursetableViewModel.initCourseTableInfo(coursetableViewModel.courseInfo)
                }
                else{
                    //否则直接读取
                    coursetableViewModel.courseInfo = it[0]
                    //将需要显示的那一周的值存入
                    coursetableViewModel.showingWeek =
                        if(coursetableViewModel.courseInfo.haveGotFirstWeek){
                            val divide = coursetableViewModel.calculateTwoDaysDivide(
                                it[0].year, it[0].month, it[0].day,
                                coursetableViewModel.now.nowYear, coursetableViewModel.now.nowMonth, coursetableViewModel.now.nowDay
                            )
                            divide / 7 + 1
                        }
                        else
                            1
                    //修改显示的tv
                    binding.tvWeekTitle.text = String.format(resources.getString(R.string.weekTitle), coursetableViewModel.showingWeek)
                }
            }
            coursetableViewModel.getCourseTableInfo().removeObservers(viewLifecycleOwner)
            /**
             * 上面这句，由于在这里多次使用observe方法，需要将他remove掉
             * 否则可能会报内存泄漏的错
             */

            //初始化vm中data数据
            coursetableViewModel.getAllCourseTableData().observe(viewLifecycleOwner){
                coursetableViewModel.coursesData = it
                addInText()
            }
            coursetableViewModel.getAllCourseTableData().removeObservers(viewLifecycleOwner)
        }
    }

    /**
     * 具体操作方法函数
     */

    //将课程信息写入
    fun addInText(){
        //先删除里面不应该显示的textview(tv)
        deleteOutText()
        //遍历所有网格布局，然后向其中写入
        var gridLayout: GridLayout
        for(date in 0 until 8) {
            gridLayout = when (date) {
                1 -> binding.gridMonday
                2 -> binding.gridTuesday
                3 -> binding.gridWednesday
                4 -> binding.gridThursday
                5 -> binding.gridFriday
                6 -> binding.gridSaturday
                7 -> binding.gridSunday
                else -> binding.gridNumber
            }
            //填充文字的函数
            loadData(gridLayout, date)
            //以下是处理高亮今日的方法
            if(coursetableViewModel.courseInfo.haveGotFirstWeek && coursetableViewModel.shouldHighLight(date)
                && coursetableViewModel.courseInfo.highLightToday){
                //高亮今日
                gridLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
            }
            else{
                //不是今日或者不高亮
                //其中背景颜色必须每次都要手动设置
                gridLayout.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    if(date == 1 || date == 3 || date == 5){
                        R.color.skyblue
                    }
                    else if(date == 2 || date == 4){
                        R.color.yellow
                    }
                    else{
                        R.color.brown
                    }
                    ))
            }
        }
        //这里是在头部的gridlayout中填入日期
        if(coursetableViewModel.courseInfo.haveGotFirstWeek){
            for(i in 1 until 8) {
                gridLayout = when (i) {
                    1 -> binding.gridMondayDay
                    2 -> binding.gridTuesdayDay
                    3 -> binding.gridWednesdayDay
                    4 -> binding.gridThursdayDay
                    5 -> binding.gridFridayDay
                    6 -> binding.gridSaturdayDay
                    7 -> binding.gridSundayDay
                    else -> binding.gridNumber
                }
                //先删除已有的日期文本
                val deleteTv = gridLayout.findViewById<TextView>(-i * 100)
                deleteTv?.let {
                    gridLayout.removeView(it)
                    Log.d("delete:", "$i")
                }
                //然后根据存入的第一周时间和需要显示的星期数来写入时间
                val tempCalendar = Calendar.getInstance()
                tempCalendar.set(
                    coursetableViewModel.courseInfo.year,
                    coursetableViewModel.courseInfo.month - 1,
                    coursetableViewModel.courseInfo.day
                )
                tempCalendar.add(Calendar.DAY_OF_YEAR, (coursetableViewModel.showingWeek - 1) * 7 + i - 1)
                Log.d("ymd11", "addInText:${(coursetableViewModel.showingWeek - 1) * 7 + i - 1}")
                //日期文本
                val textView = TextView(requireContext())
                textView.id = -i * 100
                textView.gravity = Gravity.CENTER
                textView.text = "${tempCalendar[Calendar.MONTH] + 1}.${tempCalendar[Calendar.DAY_OF_MONTH]}"

                gridLayout.addView(textView)
                //日期位置同样处理高亮
                if(coursetableViewModel.shouldHighLight(i) && coursetableViewModel.courseInfo.highLightToday){
                    gridLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
                }
                else{
                    gridLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow))
                }
            }
        }
    }

    fun deleteOutText(){
        //删除文本的函数
        /**
         * 一开始想要直接在写入的位置上直接修改，不全部移除
         * 后面发现达不到想要的效果
         * 于是还是转变为每次都先删去后添加的办法解决
         */
        for(date in 0 until 8){
            val gridLayout =
                when(date){
                    1 -> binding.gridMonday
                    2 -> binding.gridTuesday
                    3 -> binding.gridWednesday
                    4 -> binding.gridThursday
                    5 -> binding.gridFriday
                    6 -> binding.gridSaturday
                    7 -> binding.gridSunday
                    else -> binding.gridNumber
                }
            for(i in 0 until coursetableViewModel.courseInfo.courseCount){
                val textViewToRemove = gridLayout.findViewById<TextView>(i + date * 1000) ?: null
                // 如果找到了要删除的 TextView
                textViewToRemove?.let {
                    // 从 GridLayout 中移除 TextView
                    Log.d("id", "deleteOutText: ${textViewToRemove.id}")
                    gridLayout.removeView(it)
                }
            }
        }
    }

    //创建课程文本的方法
    fun inputTextViewOfCourse(course: CoursetableData, readyId: Int): TextView{
        //使用哈希表显示对应信息文字
        coursetableViewModel.textViewInfo[readyId] = "课程名称:${course.name}\n" +
                "课程地点:${course.classroom}\n" +
                "课程时间:${course.beginTime}-${course.endTime}\n" +
                "课程信息:${course.note}\n"
        val tvInput = TextView(requireContext()).apply {
            gravity = Gravity.CENTER
            id = readyId
            textSize = 10F
            maxLines = 5
            ellipsize = TextUtils.TruncateAt.END
            text = "${course.name}\n${course.classroom}\n${course.beginTime}-${course.endTime}节"
            background = ResourcesCompat.getDrawable(resources, R.drawable.boarder_tv, null)
        }
        //设置显示权重
        tvInput.layoutParams = GridLayout.LayoutParams().apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = 0
            rowSpec = GridLayout.spec(course.beginTime - 1, 1F*(course.endTime - course.beginTime + 1))
        }
        //设置点击事件
        tvInput.setOnClickListener {

            //这里就显示哈希表存的值，如果是直接用循环中的变量填写一个字符串进去
            //可能会出现报错
            //因此哈希表key用tv的唯一拥有id,value就是要显示的文本
            showDetailsDialog(coursetableViewModel.textViewInfo[readyId] ?: "", course)
        }
        return tvInput
    }

    /**
     * 实际编程中发现，如果不用空白文本填充剩余部分，怎么都没法将课程显示在想要的位置
     * 所以还是要在空白的地方填入空白文本
     */
    fun inputTextViewOfBlank(readyId: Int, start: Int): TextView{
        val tvInput = TextView(requireContext()).apply {
            gravity = Gravity.CENTER
            id = readyId
            textSize = 12F
            maxLines = 5
            ellipsize = TextUtils.TruncateAt.END
            text = ""
        }
        tvInput.layoutParams = GridLayout.LayoutParams().apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = 0
            rowSpec = GridLayout.spec(start, 1F)//权重设为1，意为每次都只填写一个课程的空白
        }
        return tvInput
    }

    //显示课程表在UI界面上
    @SuppressLint("ResourceAsColor")
    fun loadData(gridLayout: GridLayout, date: Int){
        val inputCourses: MutableList<CoursetableData> = mutableListOf()
        for(course in coursetableViewModel.coursesData){
            if(course.date == date && course.beginWeek <= coursetableViewModel.showingWeek && course.endWeek >= coursetableViewModel.showingWeek){
                //将需要显示的课程添加进来
                inputCourses.add(course)
            }
        }
        var j = 0
        //j代表着填入了第几节课的位置
        if(inputCourses.size != 0){
            while(j < coursetableViewModel.courseInfo.courseCount){
                for(course in inputCourses){
                    //填入课程
                    if(course.beginTime == j + 1){
                        val tvInput = inputTextViewOfCourse(course, j + date * 1000)
                        //填入tv变量
                        //tv有一个id，可以在删除的时候靠这个id查找
                        //传入course变量以在点击的时候显示课程信息
                        gridLayout.addView(tvInput)
                        //添加文本到这个网格布局中
                        j += course.endTime - course.beginTime + 1
                        //j响应向下移课程持续时间
                    }
                }
                //填入空白
                if(j < coursetableViewModel.courseInfo.courseCount){
                    val tvInput = inputTextViewOfBlank(j + date * 1000, j)
                    Log.d("id", "loadData: ${j + date * 1000}")
                    gridLayout.addView(tvInput)
                    j++
                }
            }
        }

        //处理特殊情况

        //处理显示课程节的最左侧的布局，要在这里显示1 2 3...的节数
        if(date == 0){
            for (k in 0 until coursetableViewModel.courseInfo.courseCount){
                val tvInput = inputTextViewOfBlank(k + date * 1000, k)
                tvInput.text = (k + 1).toString()
                gridLayout.addView(tvInput)
            }
        }
        //是否显示周六的处理
        else if(date == 6){
            gridLayout.visibility = if(coursetableViewModel.courseInfo.showSat) View.VISIBLE else View.GONE
            binding.gridSaturdayTop.visibility = gridLayout.visibility
            binding.gridSaturdayDay.visibility = gridLayout.visibility
        }
        //是否显示周日的处理
        else if(date == 7){
            gridLayout.visibility = if(coursetableViewModel.courseInfo.showSun) View.VISIBLE else View.GONE
            binding.gridSundayTop.visibility = gridLayout.visibility
            binding.gridSundayDay.visibility = gridLayout.visibility
        }
    }//显示指定周和日期的数据

    //点击事件，点击后显示对话框，对话框中有课程信息与修改，删除方法
    private fun showDetailsDialog(text: String, course: CoursetableData){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.context)
        builder.setTitle("课程详情")
        builder.setMessage(text)
        // 添加确认按钮
        builder.setPositiveButton("确认"
        ) { dialogInterface, _ -> // 关闭对话框
            dialogInterface.dismiss()
        }

        // 添加删除按钮
        builder.setNegativeButton("删除"
        ) { dialogInterface, _ ->
            showDeleteDialog(course)
            dialogInterface.dismiss()
        }

        // 添加修改按钮
        builder.setNeutralButton("修改"
        ) { dialogInterface, _ ->
            showModifyDialog(course)
            dialogInterface.dismiss()
        }

        // 显示对话框
        val dialog = builder.create()
        dialog.show()

        //对敏感操作标红处理
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED)
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.RED)
    }

    //尝试删除课程时显示的dialog
    private fun showDeleteDialog(courseTableData: CoursetableData) {
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle("确认删除？")
        builder.setMessage("删除的课程数据无法恢复。\n请选择删除的方式：\n")

        // 添加删除所有同名课程按钮
        builder.setNegativeButton(
            "删除所有同时同名课程"
        ) { dialogInterface, _ ->
            //直接删除数据库中该数据即可
            for(course in coursetableViewModel.coursesData){
                if(course.date == courseTableData.date
                    && course.classroom == courseTableData.classroom
                    && course.name == courseTableData.name
                    && course.beginTime == courseTableData.beginTime
                    && course.endTime == courseTableData.endTime){
                    coursetableViewModel.deleteCourseTableData(course)
                }
            }
            dialogInterface.dismiss()
        }

        // 添加只删除该课程按钮
        builder.setPositiveButton(
            "只删除该节课程"
        ) { dialogInterface, _ ->


            val course1 = CoursetableDataDbHelper.createExampleCoursetableData(
                courseTableData.name,
                courseTableData.classroom,
                courseTableData.date,
                courseTableData.beginTime,
                courseTableData.endTime,
                courseTableData.beginWeek,
                coursetableViewModel.showingWeek - 1,
                courseTableData.note)
            val course2 = CoursetableDataDbHelper.createExampleCoursetableData(
                courseTableData.name,
                courseTableData.classroom,
                courseTableData.date,
                courseTableData.beginTime,
                courseTableData.endTime,
                coursetableViewModel.showingWeek + 1,
                courseTableData.endWeek,
                courseTableData.note)
            coursetableViewModel.deleteCourseTableData(courseTableData)
            coursetableViewModel.insertCourseData(course1)
            coursetableViewModel.insertCourseData(course2)
            //新建两条数据，这两条数据是原课程的基础上删除了显示周的课程；然后删除原数据
            dialogInterface.dismiss()
        }

        //取消删除操作
        builder.setNeutralButton(
            "取消"
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED)
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.BLACK)
    }

    //尝试修改时显示对话框
    private fun showModifyDialog(coursetableData: CoursetableData) {
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle("修改课程信息")

        //使用自定义布局
        val view = layoutInflater.inflate(R.layout.course_dialog_modify, null)
        val edtName = view.findViewById<EditText>(R.id.edtName_modify)
        val edtRoom = view.findViewById<EditText>(R.id.edtClassroom_modify)
        val edtBegin = view.findViewById<EditText>(R.id.edtBegin_modify)
        val edtEnd = view.findViewById<EditText>(R.id.edtEnd_modify)
        val edtWeekBegin = view.findViewById<EditText>(R.id.edtWeekBegin_modify)
        val edtWeekEnd = view.findViewById<EditText>(R.id.edtWeekEnd_modify)
        val edtNote = view.findViewById<EditText>(R.id.edtNote_modify)
        builder.setView(view)

        /**
         * 修改该届课程和修改全部同时同名课程
         * 其中还要判断数据的合法性
         * 可能还有能够复用、构造函数的地方
         */
        builder.setPositiveButton(
            "修改该节课程"
        ) { dialogInterface, _ ->
            var modifyName = ""
            var modifyRoom = ""
            var modifyBegin = -1
            var modifyEnd = -1
            var modifyWeekBegin = -1
            var modifyWeekEnd = -1
            var modifyNote = ""
            if(coursetableViewModel.isLegalInput(edtName)){
                modifyName = edtName.text.toString()
            }
            if(coursetableViewModel.isLegalInput(edtRoom)){
                modifyRoom = edtRoom.text.toString()
            }
            if(coursetableViewModel.isLegalInput(edtBegin)){
                modifyBegin = edtBegin.text.toString().toInt()
            }
            if(coursetableViewModel.isLegalInput(edtEnd)){
                modifyEnd = edtEnd.text.toString().toInt()
            }
            if(coursetableViewModel.isLegalInput(edtWeekBegin)){
                modifyWeekBegin = edtWeekBegin.text.toString().toInt()
            }
            if(coursetableViewModel.isLegalInput(edtWeekEnd)){
                modifyWeekEnd = edtWeekEnd.text.toString().toInt()
            }
            if(coursetableViewModel.isLegalInput(edtNote)){
                modifyNote = edtNote.text.toString()
            }
            /**这里处理空变量和input里面的不同
             * 如果是空输入，那么就不改变课程的该变量的值
             */
            if(modifyName == "") modifyName = coursetableData.name
            if(modifyRoom == "") modifyRoom = coursetableData.classroom
            if(modifyBegin == -1) modifyBegin = coursetableData.beginTime
            if(modifyEnd == -1) modifyEnd = coursetableData.endTime
            if(modifyWeekBegin == -1) modifyWeekBegin = coursetableData.beginWeek
            if(modifyWeekEnd == -1) modifyWeekEnd = coursetableData.endWeek
            if(modifyNote == "") modifyNote = coursetableData.note
            if(modifyBegin > modifyEnd){
                Toast.makeText(requireContext(), "课程结束时间不能早于开始时间，修改失败", Toast.LENGTH_LONG).show()
            }
            else if(modifyWeekBegin > modifyWeekEnd){
                Toast.makeText(requireContext(), "课程结束周不能早于开始周，修改失败", Toast.LENGTH_LONG).show()
            }
            else if(modifyEnd > coursetableViewModel.courseInfo.courseCount){
                Toast.makeText(requireContext(), "一天内最多只有${coursetableViewModel.courseInfo.courseCount}节课，修改失败", Toast.LENGTH_LONG).show()
            }
            //处理其他的错误情况，这里要提示修改失败，因为点击按钮后对话框会直接消失
            else{
                //合规输入
                val course1 = CoursetableDataDbHelper.createExampleCoursetableData(
                    coursetableData.name,
                    coursetableData.classroom,
                    coursetableData.date,
                    coursetableData.beginTime,
                    coursetableData.endTime,
                    coursetableData.beginWeek,
                    coursetableViewModel.showingWeek - 1,
                    coursetableData.note)
                val course2 = CoursetableDataDbHelper.createExampleCoursetableData(
                    coursetableData.name,
                    coursetableData.classroom,
                    coursetableData.date,
                    coursetableData.beginTime,
                    coursetableData.endTime,
                    coursetableViewModel.showingWeek + 1,
                    coursetableData.endWeek,
                    coursetableData.note)
                val course3 = CoursetableData(
                    coursetableData.id,
                    modifyName,
                    modifyRoom,
                    coursetableData.date,
                    modifyBegin,
                    modifyEnd,
                    coursetableViewModel.showingWeek,
                    coursetableViewModel.showingWeek,
                    modifyNote)
                //新建两条数据，然后修改原数据
                coursetableViewModel.insertCourseData(course1)
                coursetableViewModel.insertCourseData(course2)
                coursetableViewModel.updateCourseTableData(course3)
                Toast.makeText(requireContext(), "修改成功！", Toast.LENGTH_LONG).show()
                dialogInterface.dismiss()
            }
        }

        // 添加取消按钮
        builder.setNegativeButton(
            "取消"
        ) { dialogInterface, _ -> dialogInterface.dismiss() }

        builder.setNeutralButton(
            "修改所有同时同名课程"
        ) { dialogInterface, _ -> // 实现删除所有同名课程的逻辑
            var modifyName = ""
            var modifyRoom = ""
            var modifyBegin = -1
            var modifyEnd = -1
            var modifyWeekBegin = -1
            var modifyWeekEnd = -1
            var modifyNote = ""
            if(coursetableViewModel.isLegalInput(edtName)){
                modifyName = edtName.text.toString()
            }
            if(coursetableViewModel.isLegalInput(edtRoom)){
                modifyRoom = edtRoom.text.toString()
            }
            if(coursetableViewModel.isLegalInput(edtBegin)){
                modifyBegin = edtBegin.text.toString().toInt()
            }
            if(coursetableViewModel.isLegalInput(edtEnd)){
                modifyEnd = edtEnd.text.toString().toInt()
            }
            if(coursetableViewModel.isLegalInput(edtWeekBegin)){
                modifyWeekBegin = edtWeekBegin.text.toString().toInt()
            }
            if(coursetableViewModel.isLegalInput(edtWeekEnd)){
                modifyWeekEnd = edtWeekEnd.text.toString().toInt()
            }
            if(coursetableViewModel.isLegalInput(edtNote)){
                modifyNote = edtNote.text.toString()
            }
            if(modifyName == "") modifyName = coursetableData.name
            if(modifyRoom == "") modifyRoom = coursetableData.classroom
            if(modifyBegin == -1) modifyBegin = coursetableData.beginTime
            if(modifyEnd == -1) modifyEnd = coursetableData.endTime
            if(modifyWeekBegin == -1) modifyWeekBegin = coursetableData.beginWeek
            if(modifyWeekEnd == -1) modifyWeekEnd = coursetableData.endWeek
            if(modifyNote == "") modifyNote = coursetableData.note
            if(modifyBegin > modifyEnd){
                Toast.makeText(requireContext(), "修改失败:课程结束时间不能早于开始时间", Toast.LENGTH_LONG).show()
            }
            else if(modifyWeekBegin > modifyWeekEnd){
                Toast.makeText(requireContext(), "修改失败:课程结束周不能早于开始周", Toast.LENGTH_LONG).show()
            }
            else if(modifyEnd > coursetableViewModel.courseInfo.courseCount){
                Toast.makeText(requireContext(), "一天内最多只有${coursetableViewModel.courseInfo.courseCount}节课，修改失败", Toast.LENGTH_LONG).show()
            }
            else{
                val course3 = CoursetableData(
                    coursetableData.id,
                    modifyName,
                    modifyRoom,
                    coursetableData.date,
                    modifyBegin,
                    modifyEnd,
                    modifyWeekBegin,
                    modifyWeekEnd,
                    modifyNote)
                //新建两条数据，然后修改原数据
                coursetableViewModel.updateCourseTableData(course3)
                Toast.makeText(requireContext(), "修改成功！", Toast.LENGTH_LONG).show()
                dialogInterface.dismiss()
            }

        }
        // 显示对话框
        builder.create().show()
    }
}