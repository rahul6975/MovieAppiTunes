package com.rahul.movieappitunes.features.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.databinding.ItemWatchedMovieBinding
import com.rahul.movieappitunes.features.viewModel.MovieViewModel
import com.squareup.picasso.Picasso

/**
 * The [androidx.recyclerview.widget.RecyclerView.Adapter] for Movies.
 */
class WatchedMovieAdapter(private val viewModel: MovieViewModel) :
    RecyclerView.Adapter<WatchResultViewHolder>() {

    var movies: List<MovieEntity>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = movies?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = ItemWatchedMovieBinding.inflate(inflater, parent, false)
        view.parent.setOnClickListener {
            view.movie?.let {
                viewModel.openMovie(it.id)
            }
        }
        return WatchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatchResultViewHolder, position: Int) {
        val item = movies?.get(position)!!
        holder.binder.apply {
            movie = item
            movieTitle.text = item.name
            Picasso.get()
                .load(item.image)
                .centerCrop()
                .fit()
                .into(movieImage)
        }
    }

}