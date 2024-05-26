package com.example.mycollege.vm

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.migration.Migration
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.example.mycollege.fragment.WeatherFragment
import com.google.gson.Gson
import com.qweather.sdk.bean.base.Code
import com.qweather.sdk.bean.base.IndicesType
import com.qweather.sdk.bean.base.Lang
import com.qweather.sdk.bean.geo.GeoBean
import com.qweather.sdk.bean.indices.IndicesBean
import com.qweather.sdk.bean.weather.WeatherDailyBean
import com.qweather.sdk.bean.weather.WeatherNowBean
import com.qweather.sdk.view.HeConfig
import com.qweather.sdk.view.QWeather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

//同样需要context变量，所以继承AndroidViewModel
class WeatherViewModel(private val application: Application) : AndroidViewModel(application) {
    //数据绑定
    private val _city = MutableLiveData<String>()
    val city: LiveData<String>
        get() = _city
    private val _temp = MutableLiveData<String>()
    val temp: LiveData<String>
        get() = _temp
    private val _text = MutableLiveData<String>()
    val text: LiveData<String>
        get() = _text
    private val _weather = MutableLiveData<String>()
    val weather: LiveData<String>
        get() = _weather
    private val _lifeRate = MutableLiveData<String>()
    val lifeRate: LiveData<String>
        get() = _lifeRate

    init{
        _city.value = ""
        _temp.value = ""
        _text.value = ""
        _weather.value = ""
        _lifeRate.value = ""
    }
    fun updateCity(newCity: String){
        _city.value = newCity
    }
    fun updateTemp(newTemp: String){
        _temp.value = newTemp
    }
    fun updateText(newText: String){
        _text.value = newText
    }
    fun updateWeather(newWeather: String){
        _weather.value = newWeather
    }
    fun updateLifeRate(newLifeRate: String){
        _lifeRate.value = newLifeRate
    }

    //缓存天气信息，以免多次定位和获取
    private class weatherClass{
        var city = ""
        var temp = ""
        var text = ""
        var weather = ""
        var lifeRate = ""
        var latitude = 0.0
        var longitude = 0.0

        fun memoryLatitude(latitude: Double){
            this.latitude = latitude
        }

        fun memoryLongitude(longitude: Double){
            this.longitude = longitude
        }

        fun memoryCity(city: String){
            this.city = city
        }

        fun memoryTemp(temp: String){
            this.temp = temp
        }

        fun memoryText(text: String){
            this.text = text
        }

        fun memoryWeather(weather: String){
            this.weather = weather
        }

        fun memoryIndices(indices: String){
            this.lifeRate = indices
        }

        fun haveGotWeather(): Boolean{
            return city != "" && temp != "" && text != "" && weather != "" && lifeRate != null
        }
    }

    private var mWeatherClass = weatherClass()
    //定位获取经纬度
    private var latitude = 0.0
    private var longitude = 0.0

    /**
     * 以下多个函数，来源于官方文档
     */
    //获取定位信息回调的函数
    @SuppressLint("DefaultLocale")
    private val mAMapLocationListener = object : AMapLocationListener {
        @SuppressLint("SetTextI18n")
        override fun onLocationChanged(amapLocation : AMapLocation) {
            //处理返回的信息
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //成功定位，记录经纬度信息以查询城市和当地查询天气
                    latitude = amapLocation.getLatitude()
                    longitude = amapLocation.getLongitude()
                    //缓存在mWeatherClass中
                    mWeatherClass.memoryLatitude(latitude)
                    mWeatherClass.memoryLongitude(longitude)
                    //获取城市信息
                    getAndChangeCityText()
                    //获取天气
                    getAndChangeNowWeather()
                    //获取7天天气
                    getAndChange7DWeathers()
                    //获取生活指数
                    getLifeRate()

                }//可在其中解析amapLocation获取相应内容。
                else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo()
                    )
                }
            }
        }
    }

    //获取城市信息
    @SuppressLint("DefaultLocale")
    private fun getAndChangeCityText(){
        QWeather.getGeoCityLookup(
            application,
            "${String.format("%.2f", longitude)},${String.format("%.2f", latitude)}",
            object : QWeather.OnResultGeoListener{
                override fun onError(e: Throwable?) {
                    //错误log
                    Log.i(WeatherFragment.TAG1, "onError, getGeoCityLookup", e)
                }
                override fun onSuccess(geoBean: GeoBean) {
                    Log.i(WeatherFragment.TAG2, "getGeoCity Success: $geoBean")
                    if(Code.OK == geoBean.code){
                        val city: MutableList<GeoBean.LocationBean>? = geoBean.getLocationBean()
                        val cityStr = city?.get(0)?.getName()
                        MainScope().launch {
                            //更新显示界面
                            updateCity("地区：$cityStr")
                        }
                        if(cityStr != null){
                            //缓存
                            mWeatherClass.memoryCity("地区：$cityStr")
                        }
                    }
                    else{
                        val code: Code = geoBean.code
                        Log.i(WeatherFragment.TAG3, "failed code: $code")
                    }
                }
            })
    }

    //获取当前天气
    @SuppressLint("DefaultLocale")
    private fun getAndChangeNowWeather(){
        QWeather.getWeatherNow(
            application,
            "${String.format("%.2f", longitude)},${String.format("%.2f", latitude)}",
            object : QWeather.OnResultWeatherNowListener{
                override fun onError(e: Throwable) {
                    Log.i(WeatherFragment.TAG1, "onError, getWeatherNow", e)
                }
                override fun onSuccess(weatherBean: WeatherNowBean) {
                    Log.i(WeatherFragment.TAG2, "getWeather onSuccess: ${Gson().toJson(weatherBean)}")
                    if (Code.OK == weatherBean.code) {
                        val now: WeatherNowBean.NowBaseBean? = weatherBean.getNow()
                        val tempStr = now?.getTemp()
                        val WeatherStr = now?.getText()
                        MainScope().launch {
                            //更新UI
                            updateTemp("温度：$tempStr")
                            updateWeather("天气：$WeatherStr")
                        }
                        //缓存
                        if(tempStr != null){
                            mWeatherClass.memoryTemp("温度：$tempStr")
                        }
                        if(WeatherStr != null){
                            mWeatherClass.memoryWeather("天气：$WeatherStr")
                        }
                    }
                    else {
                        // 在此查看返回数据失败的原因
                        val code: Code = weatherBean.code
                        Log.i(WeatherFragment.TAG3, "failed code: $code")
                    }
                }
            })
    }

    //获取7天天气
    @SuppressLint("DefaultLocale")
    private fun getAndChange7DWeathers(){
        QWeather.getWeather7D(
            application,
            "${String.format("%.2f", longitude)},${String.format("%.2f", latitude)}",
            object : QWeather.OnResultWeatherDailyListener{
                override fun onError(e: Throwable?) {
                    Log.i(WeatherFragment.TAG1, "onError: getWeather7D", e)
                }
                override fun onSuccess(weatherDailyBean: WeatherDailyBean){
                    Log.i(WeatherFragment.TAG2, "getWeather7D onSuccess: ${Gson().toJson(weatherDailyBean)}")
                    if(Code.OK == weatherDailyBean.code){
                        val daily: List<WeatherDailyBean.DailyBean>? = weatherDailyBean.getDaily()
                        var strText = "天气预报：\n"
                        for(i in 0 until 7){
                            strText += daily?.get(i)?.fxDate.toString() + "，"
                            strText += daily?.get(i)?.textDay + "，"
                            strText += daily?.get(i)?.tempMin + "-" + daily?.get(i)?.tempMax + "度"
                            strText += '\n'
                        }
                        MainScope().launch {
                            //更新UI
                            updateText(strText)
                        }
                        mWeatherClass.memoryText(strText)

                    }
                    else{
                        val code: Code = weatherDailyBean.code
                        Log.i(WeatherFragment.TAG3, "failed code: $code")
                    }
                }
            })
    }

    //获取生活指数
    @SuppressLint("DefaultLocale")
    private fun getLifeRate(){
        val rateType  = listOf(IndicesType.COMF)
        val lang: Lang = Lang.ZH_HANS
        QWeather.getIndices1D(
            application,
            "${String.format("%.2f", longitude)},${String.format("%.2f", latitude)}",
            lang,
            rateType,
            object : QWeather.OnResultIndicesListener {
                override fun onError(e: Throwable?) {
                    Log.i(WeatherFragment.TAG1, "onError: getIndices1D", e)
                }
                override fun onSuccess(indicesBean: IndicesBean) {
                    Log.i(WeatherFragment.TAG2, "getIndices1D onSuccess: ${Gson().toJson(indicesBean)}")
                    if(Code.OK == indicesBean.code){
                        val dailyList : List<IndicesBean.DailyBean>? = indicesBean.dailyList
                        val strIndices =
                            "${dailyList?.get(0)?.name}：${dailyList?.get(0)?.category}，${dailyList?.get(0)?.text}\n"
                        MainScope().launch {
                            updateLifeRate(strIndices)
                            Toast.makeText(application, "更新成功！", Toast.LENGTH_LONG).show()
                        }
                        mWeatherClass.memoryIndices(strIndices)
                    }
                    else{
                        val code: Code = indicesBean.code
                        Log.i(WeatherFragment.TAG3, "failed code: $code")
                    }
                }
            }
        )
    }

    private fun weatherInit(){
        //下面两个参数分别是public ID, KEY
        HeConfig.init(
            WeatherFragment.publicID, WeatherFragment.Key
        )
        //切换为免费订阅
        HeConfig.switchToDevService()
    }

    fun getLocationAndWeather(notClicked: Boolean){
        //如果不是点击按钮更新，且已经有相关数据，则不需要重新定位
        if(mWeatherClass.haveGotWeather() && !notClicked){
            updateTemp(mWeatherClass.temp)
            updateCity(mWeatherClass.city)
            updateText(mWeatherClass.text)
            updateWeather(mWeatherClass.weather)
            updateLifeRate(mWeatherClass.lifeRate)
            return
        }

        //声明AMapLocationClient对象和定位类型AMapLocationClientOption对象
        val mLocationClient = AMapLocationClient(application)

        //天气sdk初始化，勿动
        weatherInit()
        //天气sdk初始化，勿动
        //定位sdk隐私权限检查，勿动
        AMapLocationClient.updatePrivacyShow(application, true, true)
        AMapLocationClient.updatePrivacyAgree(application, true)
        //定位sdk隐私权限检查，勿动

        mLocationClient.setLocationListener(mAMapLocationListener)
        val mLocationOption = AMapLocationClientOption()
        //设置定位模式为AMapLocationMode.High_Accuracy，高精度模式。为默认值
        //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving)

        //设置连续定位的间隔为1小时，避免连续定位
        mLocationOption.setInterval(1000 * 3600)
        //设置定位请求超时时间，此处为20秒
        mLocationOption.setHttpTimeOut(20000)
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption)
        //启动定位
        mLocationClient.startLocation()
        //定位信息将自动异步返回
    }

    //检查网络连接，有网络则自动进行查询
    fun checkNetWorkConnection(textView: TextView, requireRefresh: Boolean){
        val connMgr = application.getSystemService(ConnectivityManager::class.java)
        //注册监听器对象
        connMgr.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object: ConnectivityManager.NetworkCallback(){

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    MainScope().launch {
                        textView.visibility = View.INVISIBLE
                        getLocationAndWeather(requireRefresh)
                    }
                    Log.d(WeatherFragment.TAG, "onAvailable: 已连接网络")
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    //显示失误信息
                    Log.d(WeatherFragment.TAG, "onUnAvailable: 未连接网络, $requireRefresh")
                    MainScope().launch {
                        if(!requireRefresh){
                            Toast.makeText(application, "无网络连接", Toast.LENGTH_LONG).show()
                        }
                        else{
                            updateTemp(mWeatherClass.temp)
                            updateCity(mWeatherClass.city)
                            updateText(mWeatherClass.text)
                            updateWeather(mWeatherClass.weather)
                            updateLifeRate(mWeatherClass.lifeRate)
                        }
                    }
                }
            }
        )
    }
}