package com.epiahackers.pelisfavorites

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.epiahackers.pelisfavorites.databinding.ActivityMainBinding
import com.epiahackers.pelisfavorites.models.Movie
import com.epiahackers.pelisfavorites.ui.ListFilmViewModel
import com.epiahackers.pelisfavorites.ui.ListFilmViewModelFactory

class MainActivity : AppCompatActivity() {

    private val viewModel: ListFilmViewModel by viewModels { ListFilmViewModelFactory() }
    private lateinit var binding: ActivityMainBinding

    private var miReload: MenuItem? = null
    private var miOrder: MenuItem? = null

    private lateinit var adapter: MovieAdapter

    private var orderAsc: Boolean = true
        set(value){
            field = value

            miOrder?.icon = getDrawable((if(orderAsc) R.drawable.ic_baseline_order_asc_24 else R.drawable.ic_baseline_order_desc_24))

            order(orderBy, orderType)

        }

    private val orderType: String get() = (if(orderAsc) "asc" else "desc")+",asc"

    private var orderBy: OrderFields = OrderFields.TITLE
        set(value){

            field = value

            order(field, orderType)

        }

    private var resultFilmIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

        if(result.resultCode == Activity.RESULT_OK){

            viewModel.loadFavoriteMovies(orderBy.field, orderType)

        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        adapter = MovieAdapter(context = this,
            itemClick =  {

                val intent = Intent(this, FilmActivity::class.java)

                intent.putExtra(FilmActivity.EXTRA_FILM, it)

                resultFilmIntent.launch(intent)

            },
            favoriteClick = {

                    if(it.favorite) viewModel.addFavorite(it)
                    else viewModel.removeFavorite(it)

            }
        )

        binding.rvMovies.adapter = adapter

        viewModel.movies.observe(this, adapter::update)

        viewModel.moviesCount.observe(this) { binding.tvInfo.text = if(it != null) String.format(getString(R.string.number_films), it.toString()) else "" }

        viewModel.loading.observe(this) {
            binding.loader.visibility = if(it) View.VISIBLE else View.GONE
            miReload?.isEnabled = !it

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

        viewModel.movieChange.observe(this) {

            if(it == null) return@observe

            val m = adapter.getList().first { n -> n.id == it.id}
            m.favorite = it.favorite

            if(m.favorite) adapter.update(adapter.getList())
            else adapter.remove(m)

        }

        viewModel.loadFavoriteMovies(orderBy.field, orderType)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        miReload = menu?.findItem(R.id.miReload)
        miOrder = menu?.findItem(R.id.miOrder)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.miReload -> viewModel.loadFavoriteMovies(orderBy.field, orderType)
            R.id.miNewMovie -> newMovie()
            R.id.miOrder -> orderAsc = !orderAsc

            R.id.miOrderTitle -> orderBy = OrderFields.TITLE
            R.id.miOrderScore -> orderBy = OrderFields.SCORE
            R.id.miWeather -> startWeather()

        }

        item.isChecked = true

        return super.onOptionsItemSelected(item)
    }

    private fun startWeather(){

        startActivity(Intent(this, WeatherActivity::class.java))

    }

    private fun order(orderField: OrderFields, asc: String){

        viewModel.loadFavoriteMovies(orderField.field, asc)

    }

    private fun newMovie(){

        val intent = Intent(this, NewMovieActivity::class.java)

        intent.putExtra(NewMovieActivity.EXTRA_FAVORITE_LIST, adapter.getList().toTypedArray())

        resultFilmIntent.launch(intent)


    }

    private enum class OrderFields(val field: String? = null) {
        SCORE("my_score,original_title"), TITLE("original_title,my_score")
    }
}