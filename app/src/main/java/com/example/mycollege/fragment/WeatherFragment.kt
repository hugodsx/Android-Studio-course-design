package com.example.mycollege.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.amap.api.location.AMapLocationClient
import com.example.mycollege.R
import com.example.mycollege.databinding.WeatherFragmentBinding
import com.example.mycollege.vm.WeatherViewModel
import com.qweather.sdk.view.HeConfig


//天气界面的显示
class WeatherFragment : Fragment() {

    companion object {
        const val TAG1 = "ON_ERROR"
        const val TAG2 = "ON_SUCCESS"
        const val TAG3 = "FAILED_CODE"
        const val TAG = "CONNECTION_CHECK"

        const val publicID = "HE2404231048501251"
        const val Key = "f3fec7d3061a4428afcf5049b4ac7b13"
    }
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var binding: WeatherFragmentBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //使用数据绑定
        binding = DataBindingUtil.inflate(inflater, R.layout.weather_fragment, container, false)
        binding.lifecycleOwner = this

        binding.weatherviewmodel = weatherViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //天气sdk初始化，勿动
        weatherInit()
        //天气sdk初始化，勿动
        //定位隐私权限检查，勿动
        AMapLocationClient.updatePrivacyShow(context, true, true)
        AMapLocationClient.updatePrivacyAgree(context,true)
        //定位隐私权限检查，勿动

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                //处理用户权限授予结果
                Log.d("tag3", "onViewCreated: ")
                permissionCallBack(it)
            }

        //向用户请求定位权限
        if(hasLocationPermission()){
            weatherViewModel.checkNetWorkConnection(binding.tvNoConnection, false)
            binding.tvRefresh.visibility = View.INVISIBLE
        }
        else{
            requestPermissions()
        }

        binding.rfsWeather.setOnRefreshListener { // 执行下拉刷新的逻辑
            if (hasLocationPermission()) {
                weatherViewModel.checkNetWorkConnection(binding.tvNoConnection, true)
                binding.tvRefresh.visibility = View.INVISIBLE
            } else {//未获得权限,提示用户
                Toast.makeText(requireContext(), "用户未给予定位权限", Toast.LENGTH_LONG).show()
            }
            binding.rfsWeather.isRefreshing = false
        }

    }

    //回调函数
    private fun permissionCallBack(
        permissions: Map<String, Boolean>
    ){
        Log.d("tag2", "permissionCallBack: ")
        permissions.forEach{
            permissions, enabled ->
            Log.d("permissions", "$permissions, $enabled")
            if(permissions == "android.permission.ACCESS_FINE_LOCATION" && enabled){
                weatherViewModel.checkNetWorkConnection(binding.tvNoConnection ,false)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    //请求用户授予特定的权限
    private fun requestPermissions() {
        //获取App当前还没有权限清单
        val permissionsToRequest = mutableListOf<String>()
        if (!hasLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        //如果还有权限没有被授予……
        if (permissionsToRequest.isNotEmpty()) {
            //打开系统权限对话框，要求用户给与授权
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
            binding.tvRefresh.visibility = View.INVISIBLE
        }
    }

    private fun weatherInit(){
        //下面两个参数分别是public ID, KEY
        HeConfig.init(
            publicID, Key
        )
        //切换为免费订阅
        HeConfig.switchToDevService()
    }
}