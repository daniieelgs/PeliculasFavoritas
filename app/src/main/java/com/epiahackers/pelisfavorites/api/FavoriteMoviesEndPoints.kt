package com.epiahackers.pelisfavorites.api

import com.epiahackers.pelisfavorites.models.Movie
import retrofit2.Response
import retrofit2.http.*

interface FavoriteMoviesEndPoints {

    @GET("movies")
    suspend fun listMovies(): Response<List<Movie>>

    @GET("movies")
    suspend fun listMovies(@Query("_order") _order: String = "asc", @Query("_sort") _sort: String): Response<List<Movie>>

    @POST("movies")
    suspend fun newMovie(@Body movie: Movie?): Response<Movie>

    @PUT("movies/{id}")
    suspend fun updateMovie(@Path("id") id: Long, @Body movie: Movie?): Response<Movie>

    @DELETE("movies/{id}")
    suspend fun deleteMovie(@Path("id") id: Long): Response<Movie>

}