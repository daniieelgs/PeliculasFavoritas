package com.epiahackers.pelisfavorites.db

import com.epiahackers.pelisfavorites.api.RetrofitConnection
import com.epiahackers.pelisfavorites.models.Movie

class FavoriteMovieRepository {

    companion object{

        suspend fun add(movie: Movie) = RetrofitConnection.FavoriteMoviesService.newMovie(movie)

        suspend fun remove(movie: Movie) = RetrofitConnection.FavoriteMoviesService.deleteMovie(movie.id)

        suspend fun update(movie: Movie) = RetrofitConnection.FavoriteMoviesService.updateMovie(movie.id, movie)

    }


}