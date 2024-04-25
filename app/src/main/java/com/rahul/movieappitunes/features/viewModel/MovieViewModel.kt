package com.rahul.movieappitunes.features.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.network.repository.MovieRepository
import com.rahul.movieappitunes.utils.SingleLiveEvent


class MovieViewModel : ViewModel() {
    private val repository = MovieRepository()
    private val search = MutableLiveData<String>()
    private val selectedMovie = MutableLiveData<String>()

    // Using switchMap directly on search LiveData

    val watchedMovies = repository.getWatchHistory()

    val favoriteMovies = repository.getAllFavorite()

    // Using switchMap directly on selectedMovie LiveData
    val viewedMovie = selectedMovie.switchMap { movieId ->
        repository.getMovie(movieId)
    }

    val searchedMovies = search.switchMap {
        repository.getMovies(it).boundary
    }

    private val _openFragment = SingleLiveEvent<Void>()
    val openFragment: LiveData<Void>
        get() {
            return _openFragment
        }

    fun search(keyword: String) {
        if (search.value != keyword) {
            search.value = keyword
        }
    }

    fun openMovie(movieId: String) {
        _openFragment.call()
        selectedMovie.value = movieId
    }

    fun addMovieAsFavorite(movieId: String) {
        repository.addAsFavorite(movieId)
    }

    fun removeMovieAsFavorite(movieId: String) {
        repository.removeAsFavorite(movieId)
    }

}