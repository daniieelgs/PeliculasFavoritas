package com.epiahackers.pelisfavorites.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitConnection {

    private val okHttpClient = HttpLoggingInterceptor().run {
        level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(this).build()
    }

    private val builderLocalApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val builderNewMovies = Retrofit.Builder()
        .baseUrl("http://api.themoviedb.org/3/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val builderWeather = Retrofit.Builder()
        .baseUrl("http://api.weatherapi.com/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val FavoriteMoviesService: FavoriteMoviesEndPoints = builderLocalApi.create()
    val NewMoviesService: NewMoviesEndPoints = builderNewMovies.create()
    val WeatherService: WeatherEndPoints = builderWeather.create()
    val ConfService: ConfEndPoints = builderLocalApi.create()
}