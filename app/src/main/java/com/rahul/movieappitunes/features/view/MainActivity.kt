package com.rahul.movieappitunes.features.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.transaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rahul.movieappitunes.R
import com.rahul.movieappitunes.databinding.ActivityMainBinding
import com.rahul.movieappitunes.features.viewModel.MovieViewModel

class MainActivity : AppCompatActivity() {
    val homeViewModel by lazy {
        ViewModelProvider(this)[MovieViewModel::class.java]
    }

    lateinit var binder: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = DataBindingUtil.setContentView(this, R.layout.activity_main)

        homeViewModel.openFragment.observe(this, Observer {
            supportFragmentManager.transaction {
                add(R.id.container, MovieDetailsFragment())
                addToBackStack(null)
            }
        })
    }
}