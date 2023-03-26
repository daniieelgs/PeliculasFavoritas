package com.epiahackers.pelisfavorites.models

import com.google.gson.annotations.SerializedName

data class Weather (

    var location: WeatherLocation,
    var current: WeatherCurrent,

    )

data class WeatherLocation(
    var name: String,
    var region: String,
    var country: String
)

data class WeatherCurrent(
    var last_updated: String,

    @SerializedName("temp_c")
    var temp: Double,

    @SerializedName("feelslike_c")
    var temp_feelslike: Double,

    var condition: WeatherCondition,

    @SerializedName("wind_kph")
    var wind_vel: Double,

    var wind_dir: String,

    var humidity: Int,

    @SerializedName("precip_mm")
    var precip: Double,

    var uv: Double,

    var cloud: Int

)

data class WeatherCondition(
    var text: String,
    var icon: String
)