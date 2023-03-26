package com.epiahackers.pelisfavorites.ui

import androidx.lifecycle.*
import com.epiahackers.pelisfavorites.ApiErrorsType
import com.epiahackers.pelisfavorites.api.RetrofitConnection
import com.epiahackers.pelisfavorites.models.Conf
import com.epiahackers.pelisfavorites.models.Weather
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WeatherViewModel: ViewModel(){


    companion object{
        private const val API_KEY = "YOUR_API_KEY"
    }

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _errorApi = MutableLiveData<ApiErrorsType?>(null)
    val errorApi: LiveData<ApiErrorsType?> get() = _errorApi

    private val _conf = MutableLiveData<Conf>()
    val conf: LiveData<Conf> = _conf

    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather> get() = _weather

    init {
        loadConf()
    }

    fun loadConf(){

        _loading.value = true

        viewModelScope.launch {

            try{

                val confResponse = RetrofitConnection.ConfService.listConf()

                if(confResponse.isSuccessful) _conf.value = confResponse.body()?.first()
                else _errorApi.value = ApiErrorsType.CUSTOM_ERROR.apply {
                    message = confResponse.errorBody().toString()
                    code = confResponse.code()
                }

            }catch (e: SocketTimeoutException) {
                _errorApi.value = ApiErrorsType.TIME_OUT
            }catch (e: UnknownHostException){
                _errorApi.value = ApiErrorsType.INTERNET_CONNECTION
            }catch (e: Exception){
                e.printStackTrace()
                _errorApi.value = ApiErrorsType.UNKNOWN_ERROR.apply { message = e.message }
            }

            _loading.value = false

        }

    }

    fun loadWeather(conf: Conf){

        _loading.value = true

        viewModelScope.launch {

            try{

                val weatherResponse = RetrofitConnection.WeatherService.getWeather(API_KEY, conf.city)

                if(weatherResponse.isSuccessful) _weather.value = weatherResponse.body()
                else _errorApi.value = ApiErrorsType.CUSTOM_ERROR.apply {
                    message = weatherResponse.errorBody().toString()
                    code = weatherResponse.code()
                }

            }catch (e: SocketTimeoutException) {
                _errorApi.value = ApiErrorsType.TIME_OUT
            }catch (e: UnknownHostException){
                _errorApi.value = ApiErrorsType.INTERNET_CONNECTION
            }catch (e: Exception){
                e.printStackTrace()
                _errorApi.value = ApiErrorsType.UNKNOWN_ERROR.apply { message = e.message }
            }

            _loading.value = false
        }

    }

    fun updateConf(conf: Conf){

        _loading.value = true

        viewModelScope.launch {

            try{

                val confResponse = RetrofitConnection.ConfService.updateConf(conf.id, conf)

                if(!confResponse.isSuccessful)  _errorApi.value = ApiErrorsType.CUSTOM_ERROR.apply {
                    message = confResponse.errorBody().toString()
                    code = confResponse.code()
                }

            }catch (e: SocketTimeoutException) {
                _errorApi.value = ApiErrorsType.TIME_OUT
            }catch (e: UnknownHostException){
                _errorApi.value = ApiErrorsType.INTERNET_CONNECTION
            }catch (e: Exception){
                e.printStackTrace()
                _errorApi.value = ApiErrorsType.UNKNOWN_ERROR.apply { message = e.message }
            }

            _loading.value = true

        }

    }

}

@Suppress("UNCHECKED_CAST")
class WeatherViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return WeatherViewModel() as T
    }
}