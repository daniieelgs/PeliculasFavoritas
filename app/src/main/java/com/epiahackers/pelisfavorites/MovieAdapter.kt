package com.epiahackers.pelisfavorites

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.epiahackers.pelisfavorites.databinding.MovieItemBinding
import com.epiahackers.pelisfavorites.models.Movie

class MovieAdapter(private var moviesList : MutableList<Movie> = mutableListOf(), private val context: Context ) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    private lateinit var itemClick : (Movie) -> Unit
    private lateinit var favoriteClick : (Movie) -> Unit

    constructor(moviesList : MutableList<Movie> = mutableListOf(), context: Context, itemClick : (Movie) -> Unit) : this(moviesList, context ){
        this.itemClick = itemClick
    }

    constructor(moviesList : MutableList<Movie> = mutableListOf(), context: Context, itemClick : (Movie) -> Unit, favoriteClick : (Movie) -> Unit) : this(moviesList, context, itemClick ){
        this.favoriteClick = favoriteClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.movie_item, parent, false), context)    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = moviesList[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int = moviesList.size

    fun getList() = moviesList

    fun update(moviesList: MutableList<Movie>){

        this.moviesList = moviesList

        println("----------------------------------------------------------------//")
        println(moviesList)

        notifyDataSetChanged()

    }

    fun remove(position: Int){

        moviesList.removeAt(position)

        notifyItemRemoved(position)
        notifyItemRangeChanged(position, moviesList.size)

    }

    fun remove(movie: Movie){
        remove(moviesList.indexOf(movie))
    }

    inner class ViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view){

        private val binding = MovieItemBinding.bind(view)

        fun bind(movie: Movie){

            var progress = makeSpinner().also { it.start() }

            binding.tvTitle.text = movie.title

            binding.tvScore.text = movie.score.toString()
            binding.tvYear.text = movie.data.split("-")[0]

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(progress)
                .timeout(10000)
                .error(R.drawable.image_not_found)

            Glide.with(binding.imgPoster)
                .load(movie.posterPath)
                .apply(requestOptions)
                .into(binding.imgPoster)

            binding.btnFavorite.setImageDrawable(context.getDrawable(if(movie.favorite) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_outline_favorite_border_24))

            if(::itemClick.isInitialized) itemView.setOnClickListener{ itemClick(movie) };
            if(::favoriteClick.isInitialized) binding.btnFavorite.setOnClickListener{

                movie.favorite = !movie.favorite

                binding.btnFavorite.setImageDrawable(context.getDrawable(if(movie.favorite) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_outline_favorite_border_24))

                favoriteClick(movie)

            }

        }

        private fun makeSpinner(): CircularProgressDrawable = CircularProgressDrawable(context).apply {
                this.strokeWidth = 5f
                this.centerRadius = 30f
                this.setTint(context.getColor(R.color.textColor))
                this.setColorSchemeColors(context.getColor(R.color.textColor))

        }

    }

}