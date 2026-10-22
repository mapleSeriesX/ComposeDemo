package com.example.core.common.startup

import android.content.Context
import androidx.startup.Initializer
import com.example.core.common.util.AppLogger

class LoggerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AppLogger.i("LoggerInitializer", "AppLogger initialized via App Startup")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
