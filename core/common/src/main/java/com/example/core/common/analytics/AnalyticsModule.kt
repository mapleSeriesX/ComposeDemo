package com.example.core.common.analytics

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

class LogAnalyticsHelper @Inject constructor() : AnalyticsHelper {
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        Log.d("APM_Analytics", "Event: $eventName | Params: $params")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsHelper(
        analyticsHelper: LogAnalyticsHelper
    ): AnalyticsHelper
}
