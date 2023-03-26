package com.epiahackers.pelisfavorites.api

import com.epiahackers.pelisfavorites.models.Movie
import com.epiahackers.pelisfavorites.models.NewMovie
import com.epiahackers.pelisfavorites.models.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherEndPoints {

    @GET("current.json")
    suspend fun getWeather(@Query("key") key: String, @Query("q") q: String): Response<Weather>

}