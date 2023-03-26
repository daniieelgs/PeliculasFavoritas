package com.epiahackers.pelisfavorites

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epiahackers.pelisfavorites.databinding.ActivityMainBinding
import com.epiahackers.pelisfavorites.models.Movie
import com.epiahackers.pelisfavorites.ui.*

class NewMovieActivity : AppCompatActivity() {

    companion object{

        val EXTRA_FAVORITE_LIST = "FAVORITE_LIST"

    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ListFilmViewModel by viewModels { ListFilmViewModelFactory() }

    private lateinit var searchView: SearchView

    private lateinit var adapter: MovieAdapter

    private var maxPages: Int = 0
    private var currentPage: Int = 1
    private var lastSearch: String = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var resultFilmIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

        if(result.resultCode == Activity.RESULT_OK){

            var movieList = adapter.getList()

            val data: Intent? = result.data

            val movieExtra = data!!.let {
                try{
                    it.getSerializableExtra(FilmActivity.EXTRA_FILM, Movie::class.java)!!
                }catch (e: NoSuchMethodError){
                    it.getSerializableExtra(FilmActivity.EXTRA_FILM) as Movie
                }
            }

            val movie = movieList.first { it.id == movieExtra.id }

            if(!movieExtra.favorite) adapter.remove(movie)
            else {
                movie.score = movieExtra.score
                movie.favorite = movieExtra.favorite
            }

            adapter.update(movieList)

            val intent = Intent()

            setResult(RESULT_OK, intent)

        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.favoriteMovieList = intent.extras!!.let {
            try{
                it.getSerializable(EXTRA_FAVORITE_LIST, Array<Movie>::class.java) as Array<Movie>
            }catch (e: NoSuchMethodError){
                it.getSerializable(EXTRA_FAVORITE_LIST)!! as Array<Movie>
            }
        }

        adapter = MovieAdapter(context = this,
            itemClick = {

                val intent = Intent(this, FilmActivity::class.java)

                intent.putExtra(FilmActivity.EXTRA_FILM, it)

                resultFilmIntent.launch(intent)

            },
            favoriteClick = {

                if(it.favorite) viewModel.addFavorite(it)
                else viewModel.removeFavorite(it)

                val intent = Intent()

                setResult(RESULT_OK, intent)

            })

        binding.rvMovies.adapter = adapter

        viewModel.movies.observe(this, adapter::update)

        viewModel.moviesCount.observe(this) { binding.tvInfo.text = if(it != null) String.format(getString(R.string.number_films), it.toString()) else "" }

        viewModel.loading.observe(this) {
            binding.loader.visibility = if(it) View.VISIBLE else View.GONE
        }
        viewModel.errorApiFavoriteFilms.observe(this) { if(it != null) SnackbarCustom.showSnackbarError(this, when(it) {

            ApiErrorsType.TIME_OUT -> getString(R.string.timeOutError)
            ApiErrorsType.INTERNET_CONNECTION -> getString(R.string.internetConnectionError)
            ApiErrorsType.UNKNOWN_ERROR -> String.format(getString(R.string.unknownError), it.message)
            ApiErrorsType.CUSTOM_ERROR -> when(it.code){

                404 -> getString(R.string.error404)

                null -> getString(R.string.error) + ": " + it.message
                else -> getString(R.string.error) + ": " + it.message

            }

        }) }

        viewModel.movieChange.observe(this){

            if(it == null) return@observe

            adapter.update(adapter.getList().apply {
                first { n -> n.id == it.id}.favorite = it.favorite
            })

        }

        viewModel.moviesPages.observe(this){
            maxPages = it!!
        }

        viewModel.currentPage.observe(this){
            currentPage = it!!
        }

        viewModel.search.observe(this){
            lastSearch = it
        }

        var loaded = false

        binding.rvMovies.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvMovies.layoutManager as GridLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItemPosition >= totalItemCount - 3 && (currentPage + 1) <= maxPages) {

                    if(loaded) return

                    loaded = true

                    viewModel.loadMovies(lastSearch, currentPage + 1, true)
                }else {

                    loaded = false

                }
            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_new_movie, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.miSearch)

        searchView = MenuItemCompat.getActionView(searchItem) as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                search(p0)

                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0);

                return true;
            }

            override fun onQueryTextChange(p0: String?): Boolean {
//                search(p0)
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            android.R.id.home -> finish()

        }

        return super.onOptionsItemSelected(item)
    }

    fun search(q: String?){

        val query = q?.trim()?.lowercase()

        if(query == null || query!!.trim().isEmpty()){
            return
        }

        viewModel.loadMovies(query)

    }

}