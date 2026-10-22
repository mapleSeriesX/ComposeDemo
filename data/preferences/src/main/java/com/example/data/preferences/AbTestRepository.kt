package com.example.data.preferences

import com.example.core.model.AbTestExperiment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface AbTestRepository {
    fun observeExperiment(key: String): Flow<AbTestExperiment?>

    fun isFeatureEnabled(key: String): Boolean

    fun evaluateVariant(
        key: String,
        defaultVariant: String,
    ): String

    fun updateExperiment(experiment: AbTestExperiment)
}

@Singleton
class AbTestRepositoryImpl
    @Inject
    constructor() : AbTestRepository {
        private val experiments = MutableStateFlow<Map<String, AbTestExperiment>>(emptyMap())

        override fun observeExperiment(key: String): Flow<AbTestExperiment?> {
            return experiments.map { map -> map[key] }
        }

        override fun isFeatureEnabled(key: String): Boolean {
            return experiments.value[key]?.isEnabled ?: false
        }

        override fun evaluateVariant(
            key: String,
            defaultVariant: String,
        ): String {
            return experiments.value[key]?.variantName ?: defaultVariant
        }

        override fun updateExperiment(experiment: AbTestExperiment) {
            val updatedMap = experiments.value.toMutableMap()
            updatedMap[experiment.key] = experiment
            experiments.value = updatedMap
        }
    }
