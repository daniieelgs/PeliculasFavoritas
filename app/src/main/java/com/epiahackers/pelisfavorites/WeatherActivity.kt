package com.epiahackers.pelisfavorites

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.core.view.MenuItemCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.epiahackers.pelisfavorites.databinding.ActivityMainBinding
import com.epiahackers.pelisfavorites.databinding.ActivityWeatherBinding
import com.epiahackers.pelisfavorites.models.Conf
import com.epiahackers.pelisfavorites.models.Weather
import com.epiahackers.pelisfavorites.ui.WeatherViewModel
import com.epiahackers.pelisfavorites.ui.WeatherViewModelFactory

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding

    private val viewModel: WeatherViewModel by viewModels { WeatherViewModelFactory() }

    private lateinit var conf: Conf
    private lateinit var weather: Weather

    private var miReload: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWeatherBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.loading.observe(this) {
            binding.loader.visibility = if(it) View.VISIBLE else View.GONE
            miReload?.let {n -> n.isEnabled = !it }
        }

        viewModel.errorApi.observe(this) {

            if(it == null) return@observe

            val errMessage: String? = when(it) {

                ApiErrorsType.TIME_OUT -> getString(R.string.timeOutError)
                ApiErrorsType.INTERNET_CONNECTION -> getString(R.string.internetConnectionError)
                ApiErrorsType.UNKNOWN_ERROR -> String.format(getString(R.string.unknownError), it.message)
                ApiErrorsType.CUSTOM_ERROR -> when(it.code){

                    404 -> getString(R.string.error404)

                    null -> getString(R.string.error) + ": " + it.message
                    else -> getString(R.string.error) + ": " + it.message

                }

            }

            SnackbarCustom.showSnackbarError(this, errMessage ?: getString(R.string.error))

        }

        viewModel.conf.observe(this){
            conf = it

            viewModel.loadWeather(conf)
        }

        viewModel.weather.observe(this) {

            weather = it


            var progress = makeSpinner().also { n -> n.start() }

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(progress)
                .timeout(10000)
                .error(R.drawable.image_not_found)

            Glide.with(binding.imgIconCondition)
                .load("http:" + it.current.condition.icon)
                .apply(requestOptions)
                .into(binding.imgIconCondition)

            binding.tvCity.text = String.format("%s, %s (%s)", it.location.name, it.location.region, it.location.country)
            binding.tvTemp.text = it.current.temp.toString() + getString(R.string.tempSymbol)
            binding.tvTextCondition.text = it.current.condition.text
            binding.tvFeelsLike.text = String.format(getString(R.string.feels_like_temp), (it.current.temp_feelslike.toString() + getString(R.string.tempSymbol)))
            binding.tvUvIndex.text = it.current.uv.toString()
            binding.tvWind.text = it.current.wind_vel.toString() + getString(R.string.vel)
            binding.tvHumidity.text = it.current.humidity.toString() + getString(R.string.humidityMesure)
            binding.tvCloud.text = it.current.cloud.toString() + "%"
            binding.tvWindDirection.text = it.current.wind_dir
            binding.tvPrecip.text = it.current.precip.toString() + "%"
            binding.tvLastUpdate.text = getString(R.string.last_update, it.current.last_updated)

        }

    }

    private fun makeSpinner(): CircularProgressDrawable = CircularProgressDrawable(this).apply {
        this.strokeWidth = 5f
        this.centerRadius = 30f
        this.setTint(getColor(R.color.textColor))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_weather, menu)

        miReload = menu?.findItem(R.id.miReload)

        val searchItem: MenuItem? = menu?.findItem(R.id.miSearch)

        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                search(p0)

                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0);

                return true;
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    fun search(q: String?){

        val query = q?.trim()?.lowercase()

        if(query == null || query.isEmpty()){
            return
        }

        conf.city = query

        viewModel.updateConf(conf)
        viewModel.loadWeather(conf)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.miReload -> viewModel.loadWeather(conf)
            android.R.id.home -> finish()

        }

        return super.onOptionsItemSelected(item)
    }
}