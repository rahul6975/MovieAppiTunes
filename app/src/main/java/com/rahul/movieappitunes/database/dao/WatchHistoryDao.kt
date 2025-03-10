package com.rahul.movieappitunes.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rahul.movieappitunes.database.MovieEntity
import com.rahul.movieappitunes.database.WatchHistoryEntity

@Dao
abstract class WatchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(movie: WatchHistoryEntity)

    @Query("SELECT movies.* FROM movies INNER JOIN watch_history ON movies.movie_id = watch_history.movie_id ORDER BY watch_history.last_watch DESC")
    abstract fun getMovieHistory(): LiveData<List<MovieEntity>>
}