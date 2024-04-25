package com.rahul.movieappitunes.features.view

import androidx.recyclerview.widget.RecyclerView
import com.rahul.movieappitunes.R
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.databinding.ItemSearchedMovieBinding
import com.rahul.movieappitunes.databinding.ItemWatchedMovieBinding
import com.rahul.movieappitunes.utils.toCurrency
import com.squareup.picasso.Picasso



/**
 * Manages how the [ItemWatchedMovieBinding] will look like and its state!
 */
class WatchResultViewHolder(val binder: ItemWatchedMovieBinding) : RecyclerView.ViewHolder(binder.root)