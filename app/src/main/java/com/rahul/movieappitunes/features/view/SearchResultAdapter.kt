package com.rahul.movieappitunes.features.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rahul.movieappitunes.R
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.databinding.ItemSearchedMovieBinding
import com.rahul.movieappitunes.features.viewModel.MovieViewModel
import com.rahul.movieappitunes.utils.toCurrency
import com.squareup.picasso.Picasso

/**
 * It arranges the [MovieEntity] in the [androidx.recyclerview.widget.RecyclerView].
 * This operation will occur in the background thread.
 */
private object DifferSearchMovie : DiffUtil.ItemCallback<MovieEntity>() {
    override fun areItemsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
        return oldItem == newItem
    }
}

/**
 * The [androidx.recyclerview.widget.RecyclerView.Adapter] for Movies.
 */
class SearchedMoviesAdapter(private val viewModel: MovieViewModel) :
    PagedListAdapter<MovieEntity, SearchedMoviesAdapter.SearchResultViewHolder>(DifferSearchMovie) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = ItemSearchedMovieBinding.inflate(inflater, parent, false)
        view.parent.setOnClickListener {
            view.movie?.let {
                viewModel.openMovie(it.id)
            }
        }
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val item = getItem(position)
        // When null it means SearchResultViewHolder is a Placeholder
        if (item != null) {
            holder.bind(item)
        } else {
            holder.clear()
        }
    }

    inner class SearchResultViewHolder(private val binder: ItemSearchedMovieBinding) :
        RecyclerView.ViewHolder(binder.root) {

        /**
         * Set the movie information to the View.
         */
        init {
            binder.favorite.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                item?.let {
                    if (item.favorite == true) {
                        viewModel.removeMovieAsFavorite(item.id)

                    } else {
                        viewModel.addMovieAsFavorite(item.id)

                    }
                }
            }
        }

        fun bind(item: MovieEntity) {
            binder.movie = item
            binder.apply {
                movieTitle.text = item.name
                price.text = item.price.toCurrency(item.currency)
                movieShortDesc.text = item.shortDesc
                genre.text = item.genre
                Picasso.get()
                    .load(item.image)
                    .placeholder(R.drawable.ic_film)
                    .centerCrop()
                    .fit()
                    .into(movieImage)

                // Set favorite icon based on the favorite status
                favorite.setImageResource(if (item.favorite == true) R.drawable.favorite else R.drawable.not_favorite)
            }

        }

        /**
         * Remove the movie information from the view.
         */
        fun clear() {
            binder.movie = null
            binder.apply {
                movieImage.setImageBitmap(null)
                movieTitle.text = null
                price.text = null
                movieShortDesc.text = null
                genre.text = null
            }
        }
    }

}