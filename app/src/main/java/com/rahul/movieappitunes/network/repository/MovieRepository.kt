package com.rahul.movieappitunes.network.repository

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import com.rahul.movieappitunes.database.ItunesMovieCache
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.database.WatchHistoryEntity
import com.rahul.movieappitunes.network.IO_EXECUTOR
import com.rahul.movieappitunes.network.Network
import com.rahul.movieappitunes.network.paginationChecker.CheckerBundle
import com.rahul.movieappitunes.network.paginationChecker.MovieChecker
import java.util.*

class MovieRepository {
    private val api = Network.service
    private val db = ItunesMovieCache.database

    /**
     * @return The single source of truth for cached movies. This comes with an observable
     * therefore you can use this to observe for any changes.
     *
     * @param itemCount the number of items to load in the [androidx.recyclerview.widget.RecyclerView]
     *             during first query. This implements a "limit" query internally in Room DB.
     */
    fun getMovies(keyword: String, itemCount: Int = 50): CheckerBundle<MovieEntity> {
        val movieBoundary = MovieChecker(keyword, api, db, itemCount)
        val movieFactory = db.movieDao().getMoviesByKeyword(keyword)
        val pageList = LivePagedListBuilder(movieFactory, itemCount).apply {
            setBoundaryCallback(movieBoundary)
        }.build()
        return CheckerBundle(pageList, movieBoundary.itemCountSignal)
    }

    /**
     * @return the movie with an id equal to [movieId]. This function
     * will also store date during when it is last accessed.
     */
    fun getMovie(movieId: String): LiveData<MovieEntity> {
        IO_EXECUTOR.execute {
            db.watchHistoryDao().insert(WatchHistoryEntity(movieId, Date()))
        }
        return db.movieDao().getMovie(movieId)
    }

    fun getAllFavorite(): LiveData<List<MovieEntity>> {
        return db.movieDao().getAllFavoriteMovies()
    }

    fun addAsFavorite(movieId: String) {
        IO_EXECUTOR.execute {
            db.movieDao().addFavorite(movieId)
        }
    }

    fun removeAsFavorite(movieId: String) {
        IO_EXECUTOR.execute {
            db.movieDao().removeFavorite(movieId)
        }
    }


    fun getWatchHistory() = db.watchHistoryDao().getMovieHistory()
}