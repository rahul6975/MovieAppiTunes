package com.rahul.movieappitunes.features.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.rahul.movieappitunes.R
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.databinding.FragmentFavoritesBinding

class FavoriteFragment : Fragment() {
    private lateinit var binder: FragmentFavoritesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binder =
            DataBindingUtil.inflate(inflater, R.layout.fragment_favorites, container, false)


        return binder.root
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        val viewModel = activity.homeViewModel

        val favoriteAdapter = WatchedMovieAdapter(viewModel)
        binder.favoriteMovies.apply {
            adapter = favoriteAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        viewModel.favoriteMovies.observe(this) {
            if (it.isNotEmpty()) {
                favoriteAdapter.movies = it
                binder.tvNoFav.visibility = View.GONE
            } else {
                binder.tvNoFav.visibility = View.VISIBLE
            }
        }

    }

}