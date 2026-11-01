package com.example.data.repository

import com.example.data.datasource.DhakaPlacesData
import com.example.data.model.DhakaPlace
import com.example.data.model.PlaceCategory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DhakaPlacesRepository {

    private val _places = MutableStateFlow<List<DhakaPlace>>(DhakaPlacesData.ALL_DHAKA_PLACES)
    val places: Flow<List<DhakaPlace>> = _places.asStateFlow()

    private val _savedPlaceIds = MutableStateFlow<Set<String>>(
        setOf("mrt_11", "hosp_01", "land_01")
    )
    val savedPlaceIds: Flow<Set<String>> = _savedPlaceIds.asStateFlow()

    fun getAllPlaces(): List<DhakaPlace> = _places.value

    fun getPlaceById(id: String): DhakaPlace? {
        return _places.value.firstOrNull { it.id == id }
    }

    fun searchPlaces(query: String, category: PlaceCategory? = null): List<DhakaPlace> {
        val cleanQuery = query.trim().lowercase()
        return _places.value.filter { place ->
            val matchesCategory = category == null || place.category == category
            val matchesQuery = cleanQuery.isEmpty() ||
                    place.nameEn.lowercase().contains(cleanQuery) ||
                    place.nameBn.lowercase().contains(cleanQuery) ||
                    place.addressEn.lowercase().contains(cleanQuery) ||
                    place.addressBn.lowercase().contains(cleanQuery) ||
                    place.areaName.lowercase().contains(cleanQuery) ||
                    place.tags.any { it.contains(cleanQuery) }
            matchesCategory && matchesQuery
        }
    }

    fun getPlacesByCategory(category: PlaceCategory): List<DhakaPlace> {
        return _places.value.filter { it.category == category }
    }

    fun toggleSavedPlace(placeId: String) {
        val current = _savedPlaceIds.value.toMutableSet()
        if (current.contains(placeId)) {
            current.remove(placeId)
        } else {
            current.add(placeId)
        }
        _savedPlaceIds.value = current
    }

    fun isPlaceSaved(placeId: String): Boolean {
        return _savedPlaceIds.value.contains(placeId)
    }

    /**
     * Calculates distance in meters between two coordinates using Haversine formula
     */
    fun calculateDistanceMeters(start: LatLng, end: LatLng): Double {
        val r = 6371000.0 // Earth radius in meters
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLng = Math.toRadians(end.longitude - start.longitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Estimated drive/travel time in minutes based on typical Dhaka traffic speed (15 km/h)
     */
    fun estimateTravelTimeMinutes(distanceMeters: Double, isMetro: Boolean = false): Int {
        val speedKmh = if (isMetro) 35.0 else 16.0
        val speedMps = speedKmh * 1000 / 3600
        val seconds = distanceMeters / speedMps
        val minutes = (seconds / 60).toInt()
        return minutes.coerceAtLeast(1)
    }
}
