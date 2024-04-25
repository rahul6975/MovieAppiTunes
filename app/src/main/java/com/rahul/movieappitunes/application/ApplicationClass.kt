package com.rahul.movieappitunes.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * This [Application] subclass will initialize our app context.
 */
class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()
        globalContext = this
    }

    companion object {
        /**
         * App level context
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var globalContext: Context
    }
}