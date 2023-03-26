package com.epiahackers.pelisfavorites.api

import com.epiahackers.pelisfavorites.models.Conf
import com.epiahackers.pelisfavorites.models.Movie
import retrofit2.Response
import retrofit2.http.*

interface ConfEndPoints {

    @GET("conf")
    suspend fun listConf(): Response<List<Conf>>

    @GET("conf/{id}")
    suspend fun conf(@Path("id") id: Long): Response<Conf>

    @PUT("conf/{id}")
    suspend fun updateConf(@Path("id") id: Long, @Body conf: Conf?): Response<Conf>


}