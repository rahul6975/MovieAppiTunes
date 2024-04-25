package com.rahul.movieappitunes.network.paginationChecker

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class CheckerBundle<T : Any>(
    val boundary: LiveData<PagedList<T>>,
    val itemCount: (Int) -> Unit
)