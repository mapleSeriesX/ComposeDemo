package com.example.core.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConfig
    @Inject
    constructor() {
        val baseUrl: String = BuildConfig.BASE_URL
        val connectTimeoutSeconds: Long = 30
        val readTimeoutSeconds: Long = 30
    }
