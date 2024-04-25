package com.rahul.movieappitunes.network.paginationChecker

import androidx.core.net.toUri
import androidx.paging.PagedList
import com.rahul.movieappitunes.database.ItunesMovieCache
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.database.SearchResultsEntity
import com.rahul.movieappitunes.features.model.Movie
import com.rahul.movieappitunes.network.ApiService
import com.rahul.movieappitunes.network.IO_EXECUTOR
import com.rahul.movieappitunes.network.NETWORK_EXECUTOR
import com.rahul.movieappitunes.utils.toDate
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MovieChecker(
    private val searchKeyword: String,
    private val api: ApiService,
    private val db: ItunesMovieCache,
    private val downloadCount: Int
) : PagedList.BoundaryCallback<MovieEntity>() {

    private var itemCount = 0
    private val movieDao = db.movieDao()
    val helper = PagingRequestHelper(IO_EXECUTOR)

    val itemCountSignal: (Int) -> Unit = {
        itemCount = it
    }

    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            NETWORK_EXECUTOR.execute {
                api.searchMovies(
                    keyword = searchKeyword,
                    limit = downloadCount
                ).callbackSuccess(it) {
                    db.runInTransaction {
                        val movieEntities = it.body()?.movies?.map(Movie::toEntity)
                        movieEntities?.run { movieDao.insertMovies(this) }
                        val searchEntities =
                            movieEntities?.map { SearchResultsEntity(searchKeyword, it.id) }
                        searchEntities?.run { movieDao.insertSearches(this) }
                    }
                }
            }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: MovieEntity) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            NETWORK_EXECUTOR.execute {
                api.searchMovies(
                    keyword = searchKeyword,
                    limit = downloadCount,
                    offset = itemCount.toOffset(downloadCount)
                ).callbackSuccess(it) {
                    db.runInTransaction {
                        val movieEntities = it.body()?.movies?.map(Movie::toEntity)
                        movieEntities?.run { movieDao.insertMovies(this) }
                        val searchEntities =
                            movieEntities?.map { SearchResultsEntity(searchKeyword, it.id) }
                        searchEntities?.run { movieDao.insertSearches(this) }
                    }
                }
            }
        }
    }
}

/**
 * Handles the exception when doing network call!
 */
private inline fun <T> Call<T>.callbackSuccess(
    callback: PagingRequestHelper.Callback,
    func: (Response<T>) -> Unit
) {
    try {
        val response = execute()
        if (response.isSuccessful) {
            func(response)
            callback.recordSuccess()
        } else {
            callback.recordFailure(HttpException(response))
        }
    } catch (ex: IOException) {

        callback.recordFailure(ex)
    }
}

/**
 * Convert [Movie] to [MovieEntity].
 */
private fun Movie.toEntity() = MovieEntity(
    trackId.toString(),
    trackName,
    trackPrice ?: 0.0,
    currency,
    shortDescription,
    longDescription,
    releaseDate!!.toDate(),
    primaryGenreName!!,
    artistName!!,
    previewUrl?.toUri(),
    artworkUrl100?.replace("100x100", "600x600")?.toUri(),
    false
)

/**
 * Convert the total item count to offset base on limit.
 */
private fun Int.toOffset(limit: Int) = if (this % limit == 0) {
    this / limit
} else {
    (this / limit) + 1
}
