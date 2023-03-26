package com.epiahackers.pelisfavorites.models

import com.google.gson.annotations.SerializedName

data class Movie(

    var id: Long = 0,

    @SerializedName("original_title")
    var title: String = "",

    var adult: Boolean = false,

    @SerializedName("poster_path")
    var posterPath: String = "",

    var overview: String = "",

    var popularity: Double = 0.0,

    @SerializedName("original_language")
    var lang: String = "",

    @SerializedName("my_score")
    var score: Int? = null,

    @SerializedName("release_date")
    var data: String = "",

    var favorite: Boolean = false

) : java.io.Serializable, Cloneable{
    public override fun clone(): Movie {
        return super.clone() as Movie
    }
}