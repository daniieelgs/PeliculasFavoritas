package com.epiahackers.pelisfavorites.api

import com.epiahackers.pelisfavorites.models.Movie
import com.epiahackers.pelisfavorites.models.NewMovie
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewMoviesEndPoints {

    @GET("search/movie")
    suspend fun listMovies(@Query("api_key") api_key: String, @Query("query") query: String, @Query("page") page: Int = 1): Response<NewMovie>

}