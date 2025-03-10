package com.rahul.movieappitunes.database.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.database.SearchResultsEntity

/**
 * Manage the Read and Write query for our Movies.
 */
@Dao
abstract class MovieDao {

    /**
     * Insert movie in the database if not exist.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertMovie(movie: MovieEntity)

    /**
     * Insert each movie not unless it does exist already.
     * This INSERT runs inside a db Transaction!
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertMovies(movies: List<MovieEntity>)

    /**
     * Map the movie searches to a search keyword.
     * This INSERT runs inside a db Transaction!
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertSearches(genres: List<SearchResultsEntity>)

    /**
     * @return an observable for our cached movies that matches the [keyword].
     */
    @Query("SELECT movies.* FROM movies INNER JOIN search_results ON movies.movie_id = search_results.movie_id WHERE search_results.keyword = :keyword")
    abstract fun getMoviesByKeyword(keyword: String): DataSource.Factory<Int, MovieEntity>

    /**
     * @return an observable for our cached movie that matches the [movieId].
     */
    @Query("SELECT * FROM movies WHERE movie_id = :movieId")
    abstract fun getMovie(movieId: String): LiveData<MovieEntity>

    @Query("UPDATE movies SET favorite = 1 WHERE movie_id = :movieId")
    abstract fun addFavorite(movieId: String)


    @Query("UPDATE movies SET favorite = 0 WHERE movie_id = :movieId")
    abstract fun removeFavorite(movieId: String)

    /**
     * @return an list observable for our cached movie that has favorite as true
     */
    @Query("SELECT * FROM movies WHERE favorite = 1")
    abstract fun getAllFavoriteMovies(): LiveData<List<MovieEntity>>

}