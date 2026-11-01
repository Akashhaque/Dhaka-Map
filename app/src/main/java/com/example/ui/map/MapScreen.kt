package com.example.ui.map

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.datasource.DhakaBoundaryData
import com.example.data.datasource.DhakaPlacesData
import com.example.data.model.AppLanguage
import com.example.data.model.CommunityReport
import com.example.data.model.DhakaPlace
import com.example.data.model.IncidentType
import com.example.data.model.PlaceCategory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline

/**
 * Main MapScreen Composable for Dhaka City Smart Map
 *
 * Implements:
 * 1. Google Maps SDK for Android Compose integration
 * 2. Fine & Coarse location permission requesting with ActivityResultContracts
 * 3. Camera positioning initialized and bounded to Dhaka Metropolitan Area
 * 4. Custom layer toggles (Traffic, Transit, Satellite/Terrain styles)
 * 5. Dhaka administrative boundary polygon & MRT Line 6 overlay
 * 6. Interactive place and incident markers with turn-by-turn navigation handoff
 */
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier,
    onNavigateToReport: () -> Unit = {}
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isBn = language == AppLanguage.BANGLA

    val mapEngineMode by viewModel.mapEngineMode.collectAsStateWithLifecycle()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsStateWithLifecycle()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

    val places by viewModel.placesList.collectAsStateWithLifecycle()
    val reports by viewModel.communityReports.collectAsStateWithLifecycle()
    val selectedPlace by viewModel.selectedPlace.collectAsStateWithLifecycle()
    val selectedReport by viewModel.selectedReport.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val routePreview by viewModel.routePreview.collectAsStateWithLifecycle()
    val isTrafficEnabled by viewModel.isTrafficEnabled.collectAsStateWithLifecycle()
    val isTransitEnabled by viewModel.isTransitEnabled.collectAsStateWithLifecycle()
    val savedIds by viewModel.savedPlaceIds.collectAsStateWithLifecycle()

    var showPermissionRationale by remember { mutableStateOf(false) }

    // Logic to Request Fine & Coarse Location Permissions
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.setLocationPermissionGranted(true)
            showPermissionRationale = false
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            val userLatLng = LatLng(loc.latitude, loc.longitude)
                            viewModel.updateUserLocation(userLatLng)
                            viewModel.centerOnUserLocation()
                            if (!DhakaBoundaryData.isInsideDhaka(userLatLng)) {
                                Toast.makeText(
                                    context,
                                    if (isBn) "আপনি ঢাকা মেট্রোপলিটন এলাকার বাইরে আছেন" else "Your location is outside Dhaka boundary",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            viewModel.centerOnUserLocation()
                        }
                    }
            } catch (e: SecurityException) {
                viewModel.centerOnUserLocation()
            }
        } else {
            viewModel.setLocationPermissionGranted(false)
            showPermissionRationale = true
        }
    }

    // Check initial permission status on launch
    LaunchedEffect(Unit) {
        val fineStatus = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseStatus = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineStatus || coarseStatus) {
            viewModel.setLocationPermissionGranted(true)
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        viewModel.updateUserLocation(LatLng(loc.latitude, loc.longitude))
                    }
                }
            } catch (e: SecurityException) {
                // Handled gracefully
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("map_screen_root")) {

        // Main Map View (Google Maps Compose)
        if (mapEngineMode == MapEngineMode.GOOGLE_MAPS) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_map_view"),
                cameraPositionState = viewModel.cameraPositionState,
                properties = viewModel.getMapProperties(),
                uiSettings = viewModel.getMapUiSettings(),
                onMapClick = {
                    viewModel.clearSelection()
                }
            ) {
                // Dhaka Metropolitan Administrative Boundary Outline Polygon
                Polygon(
                    points = DhakaBoundaryData.DHAKA_METRO_POLYGON,
                    strokeColor = Color(0xFF006B4D),
                    strokeWidth = 5f,
                    fillColor = Color(0x0C006B4D)
                )

                // MRT Line 6 Polyline (Transit Spine from Uttara to Motijheel)
                if (isTransitEnabled) {
                    Polyline(
                        points = DhakaPlacesData.METRO_ROUTE_LINE,
                        color = Color(0xFF00BCD4),
                        width = 11f
                    )
                }

                // Route Preview Polyline (when navigation preview is requested)
                routePreview?.let { preview ->
                    Polyline(
                        points = preview.points,
                        color = Color(0xFFFF9800),
                        width = 13f
                    )
                }

                // Categorized Dhaka Place Markers
                places.forEach { place ->
                    val hue = when (place.category) {
                        PlaceCategory.METRO_RAIL -> BitmapDescriptorFactory.HUE_CYAN
                        PlaceCategory.HOSPITALS -> BitmapDescriptorFactory.HUE_RED
                        PlaceCategory.POLICE -> BitmapDescriptorFactory.HUE_BLUE
                        PlaceCategory.FIRE_SERVICE -> BitmapDescriptorFactory.HUE_ORANGE
                        PlaceCategory.PARKS -> BitmapDescriptorFactory.HUE_GREEN
                        PlaceCategory.SHOPPING -> BitmapDescriptorFactory.HUE_MAGENTA
                        PlaceCategory.BUS_TERMINALS -> BitmapDescriptorFactory.HUE_VIOLET
                        else -> BitmapDescriptorFactory.HUE_AZURE
                    }
                    Marker(
                        state = MarkerState(position = place.location),
                        title = if (isBn) place.nameBn else place.nameEn,
                        snippet = if (isBn) place.addressBn else place.addressEn,
                        icon = BitmapDescriptorFactory.defaultMarker(hue),
                        onClick = {
                            viewModel.selectPlace(place)
                            true
                        }
                    )
                }

                // Citizen Community Live Incident Alert Markers
                reports.forEach { report ->
                    val hue = when (report.incidentType) {
                        IncidentType.TRAFFIC_CONGESTION -> BitmapDescriptorFactory.HUE_YELLOW
                        IncidentType.WATERLOGGING -> BitmapDescriptorFactory.HUE_AZURE
                        IncidentType.ACCIDENT -> BitmapDescriptorFactory.HUE_RED
                        IncidentType.ROAD_CLOSURE -> BitmapDescriptorFactory.HUE_ROSE
                        else -> BitmapDescriptorFactory.HUE_ORANGE
                    }
                    Marker(
                        state = MarkerState(position = report.location),
                        title = "⚠️ " + (if (isBn) report.titleBn else report.titleEn),
                        snippet = report.locationLabel,
                        icon = BitmapDescriptorFactory.defaultMarker(hue),
                        onClick = {
                            viewModel.selectReport(report)
                            true
                        }
                    )
                }
            }
        } else {
            // Standalone Interactive Dhaka Vector Map Engine (Zero API Key Fallback)
            DhakaVectorMapView(
                places = places,
                reports = reports,
                selectedPlace = selectedPlace,
                selectedReport = selectedReport,
                routePreview = routePreview,
                isTransitEnabled = isTransitEnabled,
                language = language,
                onSelectPlace = { viewModel.selectPlace(it) },
                onSelectReport = { viewModel.selectReport(it) },
                onClearSelection = { viewModel.clearSelection() }
            )
        }

        // Top Controls: Floating Search Bar, Engine Toggle, Category Rail
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Search Input Surface Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp)),
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = {
                            Text(
                                if (isBn) "ঢাকায় স্থান, রাস্তা খুঁজুন..." else "Search Dhaka places, roads...",
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input")
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Map Engine Switcher & Key Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Toggle Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isGoogle = mapEngineMode == MapEngineMode.GOOGLE_MAPS
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isGoogle) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.clickable { viewModel.setMapEngine(MapEngineMode.GOOGLE_MAPS) }
                        ) {
                            Text(
                                text = if (isBn) "🗺️ গুগল ম্যাপস" else "🗺️ Google Maps",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoogle) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }

                        val isVector = mapEngineMode == MapEngineMode.SMART_VECTOR
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isVector) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.clickable { viewModel.setMapEngine(MapEngineMode.SMART_VECTOR) }
                        ) {
                            Text(
                                text = if (isBn) "⚡ স্মার্ট ভেক্টর" else "⚡ Smart Vector",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isVector) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // API Key Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (viewModel.isApiKeyConfigured) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable { viewModel.setShowApiKeyDialog(true) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Maps API Key",
                            tint = if (viewModel.isApiKeyConfigured) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (viewModel.isApiKeyConfigured) "Key Active" else "API Key Setup",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.isApiKeyConfigured) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Rail Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.onCategorySelected(null) },
                    label = { Text(if (isBn) "সকল স্থান" else "All Places") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_all")
                )

                PlaceCategory.entries.take(7).forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(if (isBn) category.nameBn else category.nameEn) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Layer Controls (Traffic Layer, Transit Layer, Map Style) - Top Right
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 168.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Traffic Toggle
            SmallFloatingActionButton(
                onClick = { viewModel.toggleTraffic() },
                containerColor = if (isTrafficEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isTrafficEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("traffic_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Traffic,
                    contentDescription = "Traffic Layer"
                )
            }

            // Transit (MRT 6) Overlay Toggle
            SmallFloatingActionButton(
                onClick = { viewModel.toggleTransit() },
                containerColor = if (isTransitEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface,
                contentColor = if (isTransitEnabled) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("transit_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Train,
                    contentDescription = "Metro Transit Layer"
                )
            }

            // Standard / Satellite / Terrain Style Cycle
            if (mapEngineMode == MapEngineMode.GOOGLE_MAPS) {
                SmallFloatingActionButton(
                    onClick = { viewModel.cycleMapType() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("map_type_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Map Style"
                    )
                }
            }
        }

        // Location & Center Camera Actions - Bottom Right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = if (selectedPlace != null || selectedReport != null || routePreview != null) 210.dp else 24.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Central Dhaka Focus Button
            FloatingActionButton(
                onClick = { viewModel.focusCentralDhaka() },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("recenter_dhaka_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Focus Dhaka"
                )
            }

            // My Location Button with Permission Trigger
            FloatingActionButton(
                onClick = {
                    if (!hasLocationPermission) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                        try {
                            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener { loc ->
                                    if (loc != null) {
                                        viewModel.updateUserLocation(LatLng(loc.latitude, loc.longitude))
                                    }
                                    viewModel.centerOnUserLocation()
                                }
                        } catch (e: SecurityException) {
                            viewModel.centerOnUserLocation()
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("my_location_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location"
                )
            }
        }

        // Location Permission Rationale Notice Card
        AnimatedVisibility(
            visible = showPermissionRationale,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 180.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "লোকেশন অনুমতি প্রয়োজন" else "Location Permission Needed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isBn)
                                "ঢাকা শহরের ম্যাপে আপনার সঠিক অবস্থান দেখাতে এবং মেট্রো স্টেশন বা হাসপাতালের দূরত্ব নির্ণয়ে লোকেশন অনুমতি দিন।"
                            else
                                "Grant location permission to view your position on the Dhaka map and calculate journey times.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(if (isBn) "অনুমতি দিন" else "Grant", fontSize = 11.sp)
                    }
                    IconButton(onClick = { showPermissionRationale = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Route Preview Card (when directions are calculated)
        AnimatedVisibility(
            visible = routePreview != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            routePreview?.let { preview ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("route_preview_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "যাত্রার পথ ও আনুমানিক সময়" else "Route & Travel Estimate",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            IconButton(onClick = { viewModel.clearRoutePreview() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Close preview")
                            }
                        }

                        Text(
                            text = if (isBn) preview.destination.nameBn else preview.destination.nameEn,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isBn) "দূরত্ব" else "Distance",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val km = preview.distanceMeters / 1000.0
                                Text(
                                    text = String.format("%.1f km", km),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isBn) "আনুমানিক সময়" else "Estimated Time",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${preview.estimatedMinutes} min",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                openGoogleMapsNavigation(context, preview.destination.location, preview.destination.nameEn)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_maps_nav_btn")
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBn) "গুগল ম্যাপসে নেভিগেশন শুরু করুন" else "Navigate in Google Maps")
                        }
                    }
                }
            }
        }

        // Place Detail Bottom Card
        AnimatedVisibility(
            visible = selectedPlace != null && routePreview == null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            selectedPlace?.let { place ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("place_detail_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isBn) place.category.nameBn else place.category.nameEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isBn) place.nameBn else place.nameEn,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isBn) place.addressBn else place.addressEn,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Close")
                            }
                        }

                        if (place.contactPhone.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.contactPhone}"))
                                    context.startActivity(dialIntent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = place.contactPhone,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.buildRoutePreview(place) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preview_route_btn")
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "পথ দেখুন" else "Preview Route")
                            }

                            val isSaved = savedIds.contains(place.id)
                            OutlinedButton(
                                onClick = { viewModel.toggleSavePlace(place.id) },
                                modifier = Modifier.testTag("save_place_btn")
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save place",
                                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Community Incident Report Card
        AnimatedVisibility(
            visible = selectedReport != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            selectedReport?.let { report ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("report_detail_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isBn) report.incidentType.titleBn else report.incidentType.titleEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) report.titleBn else report.titleEn,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = report.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 ${report.locationLabel} (${report.area})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "👍 ${report.confirmationsCount} ${if (isBn) "নিশ্চিত করেছেন" else "confirmed"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    openGoogleMapsNavigation(context, report.location, report.titleEn)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "ঘটনাস্থলে যান" else "Navigate")
                            }
                        }
                    }
                }
            }
        }

        // Google Maps Platform API Key Setup Dialog
        if (showApiKeyDialog) {
            ApiKeySetupDialog(
                isConfigured = viewModel.isApiKeyConfigured,
                isBn = isBn,
                onDismiss = { viewModel.setShowApiKeyDialog(false) },
                onSwitchToVector = {
                    viewModel.setMapEngine(MapEngineMode.SMART_VECTOR)
                    viewModel.setShowApiKeyDialog(false)
                }
            )
        }
    }
}

@Composable
fun ApiKeySetupDialog(
    isConfigured: Boolean,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSwitchToVector: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = null,
                tint = if (isConfigured) Color(0xFF2E7D32) else Color(0xFFE65100),
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(if (isBn) "গুগল ম্যাপস এপিআই কনফিগারেশন" else "Google Maps Platform Setup")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = if (isConfigured) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isConfigured)
                            (if (isBn) "✅ আপনার গুগল ম্যাপস এপিআই কী সক্রিয় আছে।" else "✅ Google Maps API Key is active.")
                        else
                            (if (isBn) "⚠️ বর্তমানে ডেভেলপার মোডে চলছে।" else "⚠️ Currently running in Development / Smart Vector mode."),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isConfigured) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = if (isBn)
                        "গুগল ম্যাপস এপিআই কী যুক্ত করার নিয়ম:"
                    else
                        "How to inject your own Google Maps API Key securely:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Secret Name: MAPS_API_KEY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("secret_key", "MAPS_API_KEY"))
                                Toast.makeText(context, "Copied MAPS_API_KEY to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Text(
                    text = if (isBn)
                        "১. এআই স্টুডিও-এর বাম পাশের 'Secrets' প্যানেলে যান।\n২. 'MAPS_API_KEY' নামে আপনার নিজস্ব এপিআই কী পেস্ট করুন।\n৩. অ্যাপটি স্বয়ংক্রিয়ভাবে লাইভ গুগল টাইলস ও ট্রাফিক চালু করবে।"
                    else
                        "1. Open the Secrets panel in AI Studio.\n2. Add variable MAPS_API_KEY with your Google Maps key.\n3. The app automatically compiles and connects to live Google Maps tiles.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (isBn)
                        "💡 কোনো এপিআই কী ছাড়া সম্পূর্ণ বিনামূল্যে চালাতে চাইলে 'স্মার্ট ভেক্টর ম্যাপ' ইঞ্জিন ব্যবহার করুন।"
                    else
                        "💡 Alternatively, use the built-in 'Smart Vector' engine for instant zero-configuration Dhaka mapping with MRT 6, water bodies, and community reports.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (isBn) "ঠিক আছে" else "Got It")
            }
        },
        dismissButton = {
            TextButton(onClick = onSwitchToVector) {
                Text(if (isBn) "স্মার্ট ভেক্টরে যান" else "Use Smart Vector")
            }
        }
    )
}

/**
 * Launches official Google Maps app or browser navigation
 */
fun openGoogleMapsNavigation(context: Context, destination: LatLng, label: String) {
    try {
        val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${destination.latitude},${destination.longitude}")
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    } catch (e: Exception) {
        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${destination.latitude},${destination.longitude}")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}
