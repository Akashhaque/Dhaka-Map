package com.example.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datasource.DhakaBoundaryData
import com.example.data.datasource.DhakaPlacesData
import com.example.data.model.AppLanguage
import com.example.data.model.CommunityReport
import com.example.data.model.DhakaPlace
import com.example.data.model.PlaceCategory
import com.example.data.repository.CommunityReportsRepository
import com.example.data.repository.DhakaPlacesRepository
import com.example.data.repository.UserPreferencesRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MapEngineMode(val labelEn: String, val labelBn: String) {
    GOOGLE_MAPS("Google Maps", "গুগল ম্যাপস"),
    SMART_VECTOR("Smart Vector", "স্মার্ট ভেক্টর")
}

data class MapRoutePreview(
    val destination: DhakaPlace,
    val points: List<LatLng>,
    val distanceMeters: Double,
    val estimatedMinutes: Int
)

class MapViewModel(
    private val placesRepository: DhakaPlacesRepository = DhakaPlacesRepository(),
    private val reportsRepository: CommunityReportsRepository = CommunityReportsRepository(),
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository()
) : ViewModel() {

    val isApiKeyConfigured: Boolean =
        com.example.BuildConfig.MAPS_API_KEY.isNotBlank() &&
        com.example.BuildConfig.MAPS_API_KEY != "DEFAULT_KEY"

    private val _mapEngineMode = MutableStateFlow(MapEngineMode.GOOGLE_MAPS)
    val mapEngineMode: StateFlow<MapEngineMode> = _mapEngineMode.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(false)
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    val language: StateFlow<AppLanguage> = preferencesRepository.language.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    val selectedCategory: StateFlow<PlaceCategory?> = _selectedCategory.asStateFlow()

    private val _selectedPlace = MutableStateFlow<DhakaPlace?>(null)
    val selectedPlace: StateFlow<DhakaPlace?> = _selectedPlace.asStateFlow()

    private val _selectedReport = MutableStateFlow<CommunityReport?>(null)
    val selectedReport: StateFlow<CommunityReport?> = _selectedReport.asStateFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _routePreview = MutableStateFlow<MapRoutePreview?>(null)
    val routePreview: StateFlow<MapRoutePreview?> = _routePreview.asStateFlow()

    // Map UI Toggles
    val isTrafficEnabled: StateFlow<Boolean> = preferencesRepository.trafficEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val isTransitEnabled: StateFlow<Boolean> = preferencesRepository.transitEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val mapTypeIndex: StateFlow<Int> = preferencesRepository.mapTypeIndex.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val cameraPositionState: CameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(DhakaBoundaryData.CENTRAL_DHAKA, DhakaBoundaryData.DEFAULT_ZOOM)
    )

    // Filtered Places
    val placesList: StateFlow<List<DhakaPlace>> = combine(
        _searchQuery, _selectedCategory, placesRepository.places
    ) { query, category, allPlaces ->
        if (query.isBlank() && category == null) {
            allPlaces
        } else {
            placesRepository.searchPlaces(query, category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DhakaPlacesData.ALL_DHAKA_PLACES)

    // Community Reports
    val communityReports: StateFlow<List<CommunityReport>> = reportsRepository.reports.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), DhakaPlacesData.INITIAL_COMMUNITY_REPORTS
    )

    val savedPlaceIds: StateFlow<Set<String>> = placesRepository.savedPlaceIds.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: PlaceCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun selectPlace(place: DhakaPlace?) {
        _selectedPlace.value = place
        _selectedReport.value = null
        if (place != null) {
            // Animate camera smoothly to place
            viewModelScope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(place.location, 15f)
                )
            }
        }
    }

    fun selectReport(report: CommunityReport?) {
        _selectedReport.value = report
        _selectedPlace.value = null
        if (report != null) {
            viewModelScope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(report.location, 15f)
                )
            }
        }
    }

    fun clearSelection() {
        _selectedPlace.value = null
        _selectedReport.value = null
        _routePreview.value = null
    }

    fun toggleSavePlace(placeId: String) {
        placesRepository.toggleSavedPlace(placeId)
    }

    fun toggleTraffic() {
        preferencesRepository.toggleTraffic()
    }

    fun toggleTransit() {
        preferencesRepository.toggleTransit()
    }

    fun cycleMapType() {
        val next = (mapTypeIndex.value + 1) % 3
        preferencesRepository.setMapTypeIndex(next)
    }

    fun getMapType(): MapType {
        return when (mapTypeIndex.value) {
            1 -> MapType.HYBRID
            2 -> MapType.TERRAIN
            else -> MapType.NORMAL
        }
    }

    fun updateUserLocation(location: LatLng) {
        _userLocation.value = location
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _hasLocationPermission.value = granted
    }

    fun centerOnUserLocation() {
        val loc = _userLocation.value
        if (loc != null) {
            viewModelScope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(loc, 15f)
                )
            }
        } else {
            focusCentralDhaka()
        }
    }

    fun focusCentralDhaka() {
        viewModelScope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    DhakaBoundaryData.CENTRAL_DHAKA, DhakaBoundaryData.DEFAULT_ZOOM
                )
            )
        }
    }

    fun buildRoutePreview(destination: DhakaPlace) {
        val origin = _userLocation.value ?: DhakaBoundaryData.CENTRAL_DHAKA
        val distance = placesRepository.calculateDistanceMeters(origin, destination.location)
        val isMetro = destination.category == PlaceCategory.METRO_RAIL
        val timeEst = placesRepository.estimateTravelTimeMinutes(distance, isMetro)

        // Interpolate an approximate path along city grid between origin and destination
        val midLat = (origin.latitude + destination.location.latitude) / 2
        val midLng = (origin.longitude + destination.location.longitude) / 2
        // Introduce slight realistic dogleg for roads
        val waypoint1 = LatLng(origin.latitude + (midLat - origin.latitude) * 0.8, origin.longitude)
        val waypoint2 = LatLng(midLat, destination.location.longitude)

        val path = listOf(origin, waypoint1, waypoint2, destination.location)

        _routePreview.value = MapRoutePreview(
            destination = destination,
            points = path,
            distanceMeters = distance,
            estimatedMinutes = timeEst
        )
    }

    fun clearRoutePreview() {
        _routePreview.value = null
    }

    fun getMapProperties(): MapProperties {
        return MapProperties(
            isTrafficEnabled = isTrafficEnabled.value,
            mapType = getMapType(),
            isMyLocationEnabled = _hasLocationPermission.value && _userLocation.value != null,
            latLngBoundsForCameraTarget = DhakaBoundaryData.DHAKA_BOUNDS,
            minZoomPreference = 10f,
            maxZoomPreference = 20f
        )
    }

    fun getMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false, // We supply a custom styled M3 FAB
            mapToolbarEnabled = true,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    fun toggleMapEngine() {
        _mapEngineMode.value = if (_mapEngineMode.value == MapEngineMode.GOOGLE_MAPS) {
            MapEngineMode.SMART_VECTOR
        } else {
            MapEngineMode.GOOGLE_MAPS
        }
    }

    fun setMapEngine(mode: MapEngineMode) {
        _mapEngineMode.value = mode
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _showApiKeyDialog.value = show
    }
}
