package com.epiahackers.pelisfavorites.ui

import androidx.lifecycle.*
import com.epiahackers.pelisfavorites.ApiErrorsType
import com.epiahackers.pelisfavorites.api.RetrofitConnection
import com.epiahackers.pelisfavorites.db.FavoriteMovieRepository
import com.epiahackers.pelisfavorites.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ListFilmViewModel : ViewModel() {

    private val _movies = MutableLiveData<MutableList<Movie>>(mutableListOf())
    val movies: LiveData<MutableList<Movie>> get() = _movies

    private val _moviesCount = MutableLiveData<Int?>()
    val moviesCount: LiveData<Int?> get() = _moviesCount

    private val _moviesPages = MutableLiveData<Int?>()
    val moviesPages: LiveData<Int?> get() = _moviesPages

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> get() = _currentPage

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _errorApi = MutableLiveData<ApiErrorsType>()
    val errorApiFavoriteFilms: LiveData<ApiErrorsType> get() = _errorApi

    private val _movieChange = MutableLiveData<Movie?>(null)
    val movieChange: LiveData<Movie?> = _movieChange

    private val _search = MutableLiveData("")
    val search: LiveData<String> get() = _search

    var favoriteMovieList: Array<Movie> = emptyArray()

    companion object{

        private const val API_KEY = "YOUR_API_KEY"

    }

    fun loadFavoriteMovies(orderField: String? = null, orderAsc: String = "asc"){

        _loading.value = true

        _moviesCount.value = null

        viewModelScope.launch {

            try{
                var moviesResponse = if(orderField != null) RetrofitConnection.FavoriteMoviesService.listMovies(orderAsc, orderField) else RetrofitConnection.FavoriteMoviesService.listMovies()

                if (moviesResponse.isSuccessful) {
                    _movies.value = moviesResponse.body().apply { this?.map { it.favorite = true } } as MutableList<Movie>
                    _moviesCount.value = moviesResponse.body()!!.size
                }
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

        }

    }

    fun loadMovies(q: String, page: Int = 1, addNextPage: Boolean = false){

        _loading.value = true

        _moviesCount.value = null

        _search.value = q

        viewModelScope.launch {

            try{
                var moviesResponse = RetrofitConnection.NewMoviesService.listMovies(API_KEY, q, page) //TODO no cargar todas, ir por paginado

                if (moviesResponse.isSuccessful) {

                    val newMovieBody = moviesResponse.body()

                    var listNewMovies = (newMovieBody?.results as MutableList<Movie>).apply { this.map {

                        favoriteMovieList.firstOrNull() { n -> n.id == it.id }?.let { favorite ->
                            it.favorite = true
                            it.score = favorite.score
                        }

                        it.posterPath = "https://image.tmdb.org/t/p/original" + it.posterPath


                    } }

                    listNewMovies = listNewMovies.sortedBy { n -> n.title }.toMutableList()

                    if(addNextPage && _movies.value != null) _movies.value = _movies.value!!.apply { addAll(listNewMovies) }
                    else _movies.value = listNewMovies.toMutableList()

                    _moviesCount.value =  newMovieBody.total_results

                    _currentPage.value = newMovieBody.page
                    _moviesPages.value = newMovieBody.total_pages
                }
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

        }

    }

    fun addFavorite(movie: Movie){

        _loading.value = true

        viewModelScope.launch {

            try {
                var moviesResponse =
                    withContext(Dispatchers.Default) {
                        FavoriteMovieRepository.add(movie)
                    }

                if(!moviesResponse.isSuccessful) _errorApi.value = ApiErrorsType.CUSTOM_ERROR.apply {
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

            if(_errorApi.value != null) movie.favorite = false

            _movieChange.value = movie


        }

    }


    fun removeFavorite(movie: Movie){

        _loading.value = true

        viewModelScope.launch {

            try {
                var moviesResponse =
                    withContext(Dispatchers.Default) {
                        FavoriteMovieRepository.remove(movie)
                    }

                if(!moviesResponse.isSuccessful) _errorApi.value = ApiErrorsType.CUSTOM_ERROR.apply {
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

            if(_errorApi.value != null)  movie.favorite = true

            _movieChange.value = movie

        }


    }

}

@Suppress("UNCHECKED_CAST")
class ListFilmViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return ListFilmViewModel() as T
    }
}