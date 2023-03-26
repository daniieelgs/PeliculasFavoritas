package com.epiahackers.pelisfavorites.ui

import android.view.View
import android.widget.RatingBar
import androidx.lifecycle.*
import com.epiahackers.pelisfavorites.ApiErrorsType
import com.epiahackers.pelisfavorites.db.FavoriteMovieRepository
import com.epiahackers.pelisfavorites.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class FilmViewModel : ViewModel() {

    private val _favorite = MutableLiveData(false)
    val favorite : LiveData<Boolean> get() = _favorite

    private val _score = MutableLiveData<Int?>()
    val score : LiveData<Int?> get() = _score

    private val _loading = MutableLiveData(false)
    val loading : LiveData<Boolean> get() = _loading

    private val _movie = MutableLiveData<Movie>()
    val movie: LiveData<Movie> get() = _movie

    private val _errorApi = MutableLiveData<ApiErrorsType?>()
    val errorApiFavoriteFilms: LiveData<ApiErrorsType?> get() = _errorApi

    fun setMovie(movie: Movie){
        _movie.value = movie
    }

    fun getMovie() = _movie.value

    fun isFavorite(b:Boolean){
        _favorite.value = b
    }

    fun onClickFavoriteButton(v: View?){
        _favorite.value = !favorite.value!!

        getMovie()?.clone().apply { this?.favorite = _favorite.value!! }?.let { if(it.favorite) addFavorite(it) else removeFavorite(it) }
    }

    fun onRatingScoreChangeListener(ratingBar: RatingBar, rating: Float, fromUser: Boolean){
        _score.value = rating.toInt()

        if(fromUser) getMovie()?.clone().apply { this?.score = _score.value!! }?.let { updateMovie(it) }
    }

    fun addFavorite(movie: Movie){

        _loading.value = true

        movie.score = movie.score ?: 0

        viewModelScope.launch {

            try{
                var moviesResponse =
                    withContext(Dispatchers.Default) {
                        FavoriteMovieRepository.add(movie)
                    }

                if(moviesResponse.isSuccessful) _movie.value = movie
                else _errorApi.value = ApiErrorsType.CUSTOM_ERROR.apply {
                    message = moviesResponse.errorBody().toString()
                    code = moviesResponse.code()
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

            if(_errorApi.value != null) _favorite.value = getMovie()!!.favorite


        }

    }

    fun removeFavorite(movie: Movie){

        _loading.value = true

        viewModelScope.launch {

            try{
                var moviesResponse =
                    withContext(Dispatchers.Default) {
                        FavoriteMovieRepository.remove(movie)
                    }
                
                if(moviesResponse.isSuccessful) _movie.value = movie
                else  _errorApi.value = ApiErrorsType.CUSTOM_ERROR.apply {
                    message = moviesResponse.errorBody().toString()
                    code = moviesResponse.code()
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

            if(_errorApi.value != null) _favorite.value = getMovie()!!.favorite

        }

    }

    fun updateMovie(movie: Movie){

        _loading.value = true

        _errorApi.value = null

        viewModelScope.launch {

            try{
                var moviesResponse =
                    withContext(Dispatchers.Default) {
                        FavoriteMovieRepository.update(movie)
                    }
                //TODO
                if(moviesResponse.isSuccessful) _movie.value = movie
                else _errorApi.value = ApiErrorsType.CUSTOM_ERROR.apply {
                    message = moviesResponse.errorBody().toString()
                    code = moviesResponse.code()
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

            if(_errorApi.value != null) _score.value = getMovie()!!.score

        }

    }

}

@Suppress("UNCHECKED_CAST")
class FilmViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return FilmViewModel() as T
    }
}