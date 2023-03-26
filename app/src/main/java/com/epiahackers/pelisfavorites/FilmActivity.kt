package com.epiahackers.pelisfavorites

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.epiahackers.pelisfavorites.databinding.ActivityFilmBinding
import com.epiahackers.pelisfavorites.models.Movie
import com.epiahackers.pelisfavorites.ui.FilmViewModel
import com.epiahackers.pelisfavorites.ui.FilmViewModelFactory

class FilmActivity : AppCompatActivity() {

    companion object{

        const val EXTRA_FILM = "FILM"

    }

    private val viewModel: FilmViewModel by viewModels { FilmViewModelFactory() }

    private lateinit var binding: ActivityFilmBinding

    private lateinit var movie: Movie

    private var initFavorite: Boolean = false
    private var initRating: Int = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFilmBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        movie = intent.extras!!.let {
            try{
                it.getSerializable(EXTRA_FILM, Movie::class.java)!!
            }catch (e: NoSuchMethodError){
                it.getSerializable(EXTRA_FILM)!! as Movie
            }
        }

        viewModel.setMovie(movie)

        initFavorite = movie.favorite
        initRating = movie.score ?: 0

        binding.tvTitle.text = movie.title

        var progress = makeSpinner().also { it.start() }

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(progress)
            .timeout(10000)
            .error(R.drawable.image_not_found)

        Glide.with(binding.ivPost)
            .load(movie.posterPath)
            .apply(requestOptions)
            .into(binding.ivPost)

        binding.tvDescription.text = movie.overview
        binding.tvData.text = movie.data
        binding.tvLanguaje.text = movie.lang
        binding.cbAdult.isChecked = movie.adult
        binding.tvPopularity.text = movie.popularity.toString()
        binding.rbScore.rating = movie.score?.toFloat() ?: 0.0f

        viewModel.favorite.observe(this) {
            binding.btnFavorite.setImageDrawable(getDrawable(if(it) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_outline_favorite_border_24))
        }

        viewModel.loading.observe(this) { binding.loadingIndicator.visibility = if(it) View.VISIBLE else View.GONE }

        viewModel.isFavorite(initFavorite)

        viewModel.score.observe(this) {
            binding.rbScore.rating = it?.toFloat() ?: 0.0f
        }

        viewModel.errorApiFavoriteFilms.observe(this){ if(it != null) SnackbarCustom.showSnackbarError(this, when(it) {

            ApiErrorsType.TIME_OUT -> getString(R.string.timeOutError)
            ApiErrorsType.INTERNET_CONNECTION -> getString(R.string.internetConnectionError)
            ApiErrorsType.UNKNOWN_ERROR -> String.format(getString(R.string.unknownError), it.message)
            ApiErrorsType.CUSTOM_ERROR -> when(it.code){

                404 -> getString(R.string.movieError404)

                null -> getString(R.string.error) + ": " + it.message
                else -> getString(R.string.error) + ": " + it.message

            }

        }) }

        binding.btnFavorite.setOnClickListener(viewModel::onClickFavoriteButton)

        binding.rbScore.setOnRatingBarChangeListener(viewModel::onRatingScoreChangeListener)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            android.R.id.home -> finish()

        }

        return super.onOptionsItemSelected(item)
    }

    private fun makeSpinner(): CircularProgressDrawable = CircularProgressDrawable(this).apply {
        this.strokeWidth = 5f
        this.centerRadius = 30f
        this.setTint(getColor(R.color.textColor))
        this.setColorSchemeColors(getColor(R.color.textColor))
    }

    override fun finish() {

        if(initFavorite != viewModel.getMovie()!!.favorite || initRating != viewModel.getMovie()!!.score){

            val intent = Intent()

            intent.putExtra(EXTRA_FILM, viewModel.getMovie())

            setResult(RESULT_OK, intent)

        }

        super.finish()
    }
}