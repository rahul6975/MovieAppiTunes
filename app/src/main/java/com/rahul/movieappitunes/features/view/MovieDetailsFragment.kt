package com.rahul.movieappitunes.features.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.rahul.movieappitunes.R
import com.rahul.movieappitunes.databinding.FragmentMovieDetailsBinding
import com.rahul.movieappitunes.utils.toCurrency
import com.squareup.picasso.Picasso


/**
 * This is the screen that shows the entire information about the Movie.
 */
class MovieDetailsFragment : Fragment() {

    private lateinit var binder: FragmentMovieDetailsBinding
    private var isFavorite = false
    private var movieId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binder =
            DataBindingUtil.inflate(inflater, R.layout.fragment_movie_details, container, false)

        /**
         * Add a 60% gap in between our price tag and movie description!
         */
        val mHeight = activity!!.windowManager.defaultDisplay.height

        return binder.root
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        val viewModel = activity.homeViewModel
        viewModel.viewedMovie.observe(this) { entity ->
            if (entity != null) {
                isFavorite = entity.favorite == true
                movieId = entity.id
                binder.apply {
                    movieTitle.text = entity.name
                    movieDesc.text = entity.longDesc
                    moviePrice.text = entity.price.toCurrency(entity.currency)
                    movieActor.text = entity.actor
                    movieGenre.text = entity.genre
                    Picasso.get()
                        .load(entity.image)
                        .centerCrop()
                        .fit()
                        .into(movieImage)
                    favorite.setImageResource(if (entity.favorite == true) R.drawable.favorite else R.drawable.not_favorite)

                    playVideo.setOnClickListener {
                        if (entity.preview != null) {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(entity.preview, "video/*")
                            startActivity(intent)
                        }
                    }

                    movieTitle.paint
                }
            }
        }

        binder.favorite.setOnClickListener {
            if (isFavorite) {
                viewModel.removeMovieAsFavorite(movieId)
                isFavorite = false
                binder.favorite.setImageResource(R.drawable.not_favorite)
            } else {
                viewModel.addMovieAsFavorite(movieId)
                isFavorite = true
                binder.favorite.setImageResource(R.drawable.favorite)

            }
        }
    }

}