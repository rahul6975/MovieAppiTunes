package com.rahul.movieappitunes.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rahul.movieappitunes.application.ApplicationClass
import com.rahul.movieappitunes.database.dao.MovieDao
import com.rahul.movieappitunes.database.dao.WatchHistoryDao

/**
 * This database is for caching Movie information. This will server
 * as the single source truth.
 */
@Database(
    entities = [MovieEntity::class,
        SearchResultsEntity::class,
        WatchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    value = [
        DateConverter::class,
        UriConverter::class
    ]
)
abstract class ItunesMovieCache : RoomDatabase() {

    /**
     * @return all supported query for [MovieDao]
     */
    abstract fun movieDao(): MovieDao

    /**
     * @return all supported query for [WatchHistoryDao]
     */
    abstract fun watchHistoryDao(): WatchHistoryDao

    companion object {
        const val DATABASE_NAME = "ItunesMovieCache"

        /**
         * The created database instance. This is a singleton therefore
         * we do not need to recreate a new instance everytime we
         * need it!
         */
        val database = Room.databaseBuilder(
            ApplicationClass.globalContext,
            ItunesMovieCache::class.java,
            DATABASE_NAME
        ).build()
    }
}
