package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.data.model.CommunityReport
import com.example.data.model.DhakaPlace
import com.example.data.repository.CommunityReportsRepository
import com.example.data.repository.DhakaPlacesRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.explore.ExploreScreen
import com.example.ui.map.MapScreen
import com.example.ui.map.MapViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.saved.SavedScreen
import com.example.ui.updates.UpdatesScreen

enum class AppDestination(
    val titleEn: String,
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    MAP("Map", "ম্যাপ", Icons.Filled.Map, Icons.Outlined.Map, "nav_map"),
    EXPLORE("Explore", "এক্সপ্লোর", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_explore"),
    UPDATES("Updates", "আপডেট", Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_updates"),
    SAVED("Saved", "সংরক্ষিত", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "nav_saved"),
    PROFILE("Profile", "প্রোফাইল", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
}

@Composable
fun MainAppNavigation(
    placesRepository: DhakaPlacesRepository = remember { DhakaPlacesRepository() },
    reportsRepository: CommunityReportsRepository = remember { CommunityReportsRepository() },
    preferencesRepository: UserPreferencesRepository = remember { UserPreferencesRepository() },
    mapViewModel: MapViewModel = remember {
        MapViewModel(placesRepository, reportsRepository, preferencesRepository)
    }
) {
    var currentDestination by remember { mutableStateOf(AppDestination.MAP) }
    val language by preferencesRepository.language.collectAsStateWithLifecycle(initialValue = AppLanguage.ENGLISH)
    val isBn = language == AppLanguage.BANGLA

    // Handle system back navigation to return to MAP if currently on another tab
    BackHandler(enabled = currentDestination != AppDestination.MAP) {
        currentDestination = AppDestination.MAP
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = if (isBn) destination.titleBn else destination.titleEn
                            )
                        },
                        label = {
                            Text(
                                text = if (isBn) destination.titleBn else destination.titleEn,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { paddingValues ->
        when (currentDestination) {
            AppDestination.MAP -> {
                MapScreen(
                    viewModel = mapViewModel,
                    modifier = Modifier.padding(paddingValues),
                    onNavigateToReport = {
                        currentDestination = AppDestination.UPDATES
                    }
                )
            }
            AppDestination.EXPLORE -> {
                ExploreScreen(
                    placesRepository = placesRepository,
                    language = language,
                    modifier = Modifier.padding(paddingValues),
                    onSelectPlaceOnMap = { place ->
                        mapViewModel.selectPlace(place)
                        currentDestination = AppDestination.MAP
                    }
                )
            }
            AppDestination.UPDATES -> {
                UpdatesScreen(
                    reportsRepository = reportsRepository,
                    language = language,
                    modifier = Modifier.padding(paddingValues),
                    onViewOnMap = { report ->
                        mapViewModel.selectReport(report)
                        currentDestination = AppDestination.MAP
                    }
                )
            }
            AppDestination.SAVED -> {
                SavedScreen(
                    placesRepository = placesRepository,
                    language = language,
                    modifier = Modifier.padding(paddingValues),
                    onSelectPlaceOnMap = { place ->
                        mapViewModel.selectPlace(place)
                        currentDestination = AppDestination.MAP
                    }
                )
            }
            AppDestination.PROFILE -> {
                ProfileScreen(
                    preferencesRepository = preferencesRepository,
                    language = language,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
