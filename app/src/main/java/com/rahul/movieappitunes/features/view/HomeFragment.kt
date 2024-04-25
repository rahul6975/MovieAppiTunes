package com.rahul.movieappitunes.features.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.rahul.movieappitunes.R
import com.rahul.movieappitunes.databinding.FragmentHomeBinding

/**
 * A Fragment that shows recently viewed movies and searched movies.
 */
class HomeFragment : Fragment() {


    private lateinit var binder: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binder = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )
        return binder.root
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binder.searchBox.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        val viewModel = activity.homeViewModel

        val watchedMoviesAdapter = WatchedMovieAdapter(viewModel)
        val searchedMoviesAdapter = SearchedMoviesAdapter(viewModel)
        binder.watchedMovies.apply {
            adapter = watchedMoviesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        binder.searchedMovies.apply {
            adapter = searchedMoviesAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.search("star")
        viewModel.watchedMovies.observe(this, Observer {
            if (it.isEmpty()) {
                binder.recentLabel.visibility = View.GONE
                binder.watchedMovies.visibility = View.GONE
            } else {
                binder.recentLabel.visibility = View.VISIBLE
                binder.watchedMovies.visibility = View.VISIBLE
            }

            watchedMoviesAdapter.movies = it
        })
        viewModel.searchedMovies.observe(this) {
            if (it.isNotEmpty()) {
                searchedMoviesAdapter.submitList(it)
                binder.allRVs.visibility = View.VISIBLE
            } else {
                binder.allRVs.visibility = View.INVISIBLE
            }
        }

        binder.searchBox.setOnEditorActionListener { _, actionId, _ ->
            val keyword = binder.searchBox.text.toString()
            if (keyword.isNotEmpty()) {
                viewModel.search(keyword)
                hideKeyboard()
                true
            }
            false
        }

        binder.favorites.setOnClickListener {
            val favoriteFragment = FavoriteFragment()
            parentFragmentManager.beginTransaction()
                .add(R.id.container, favoriteFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}