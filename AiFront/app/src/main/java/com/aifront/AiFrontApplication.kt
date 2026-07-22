package com.aifront

import android.app.Application
import com.aifront.data.local.AppDatabase

class AiFrontApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
    }
}