package com.example.data.repository

import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository {

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: Flow<AppLanguage> = _language.asStateFlow()

    private val _trafficEnabled = MutableStateFlow(false)
    val trafficEnabled: Flow<Boolean> = _trafficEnabled.asStateFlow()

    private val _transitEnabled = MutableStateFlow(true)
    val transitEnabled: Flow<Boolean> = _transitEnabled.asStateFlow()

    private val _mapTypeIndex = MutableStateFlow(0) // 0: Normal, 1: Satellite, 2: Terrain
    val mapTypeIndex: Flow<Int> = _mapTypeIndex.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.ENGLISH) AppLanguage.BANGLA else AppLanguage.ENGLISH
    }

    fun setTrafficEnabled(enabled: Boolean) {
        _trafficEnabled.value = enabled
    }

    fun toggleTraffic() {
        _trafficEnabled.value = !_trafficEnabled.value
    }

    fun setTransitEnabled(enabled: Boolean) {
        _transitEnabled.value = enabled
    }

    fun toggleTransit() {
        _transitEnabled.value = !_transitEnabled.value
    }

    fun setMapTypeIndex(index: Int) {
        _mapTypeIndex.value = index % 3
    }
}
