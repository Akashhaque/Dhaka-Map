package com.example.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.DhakaBoundaryData
import com.example.data.datasource.DhakaPlacesData
import com.example.data.model.AppLanguage
import com.example.data.model.DhakaPlace
import com.example.data.model.PlaceCategory
import com.example.data.repository.DhakaPlacesRepository
import com.example.ui.map.openGoogleMapsNavigation

@Composable
fun ExploreScreen(
    placesRepository: DhakaPlacesRepository,
    language: AppLanguage,
    onSelectPlaceOnMap: (DhakaPlace) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PlaceCategory?>(null) }
    var emergencyOnly by remember { mutableStateOf(false) }

    val allPlaces = remember { placesRepository.getAllPlaces() }
    val filteredPlaces = remember(searchQuery, selectedCategory, emergencyOnly) {
        allPlaces.filter { place ->
            val matchesCategory = selectedCategory == null || place.category == selectedCategory
            val matchesEmergency = !emergencyOnly || place.isEmergency
            val matchesQuery = searchQuery.isBlank() ||
                    place.nameEn.contains(searchQuery, ignoreCase = true) ||
                    place.nameBn.contains(searchQuery, ignoreCase = true) ||
                    place.addressEn.contains(searchQuery, ignoreCase = true) ||
                    place.areaName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesEmergency && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("explore_screen_root")
    ) {
        Text(
            text = if (isBn) "ঢাকা শহর অন্বেষণ" else "Explore Dhaka City",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isBn) "মেট্রোরেল, হাসপাতাল, থানা ও দর্শনীয় স্থানসমূহ" else "Metro stations, emergency services & major landmarks",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isBn) "স্থান খুঁজুন (যেমন: শাহবাগ, মিরপুর)..." else "Search places (e.g. Shahbagh, Mirpur)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("explore_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null && !emergencyOnly,
                onClick = {
                    selectedCategory = null
                    emergencyOnly = false
                },
                label = { Text(if (isBn) "সব" else "All") }
            )

            FilterChip(
                selected = emergencyOnly,
                onClick = {
                    emergencyOnly = !emergencyOnly
                    if (emergencyOnly) selectedCategory = null
                },
                label = { Text(if (isBn) "জরুরি সেবা (হাসপাতাল/থানা)" else "Emergency 🚨") }
            )

            PlaceCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat && !emergencyOnly,
                    onClick = {
                        selectedCategory = if (selectedCategory == cat) null else cat
                        emergencyOnly = false
                    },
                    label = { Text(if (isBn) cat.nameBn else cat.nameEn) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Places List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize().testTag("explore_places_list")
        ) {
            items(filteredPlaces, key = { it.id }) { place ->
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPlaceOnMap(place) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isBn) place.category.nameBn else place.category.nameEn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = place.rating.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) place.nameBn else place.nameEn,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) place.addressBn else place.addressEn,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val distance = placesRepository.calculateDistanceMeters(
                                DhakaBoundaryData.CENTRAL_DHAKA, place.location
                            )
                            val km = distance / 1000.0
                            Text(
                                text = "📍 ${place.areaName} • ~${String.format("%.1f", km)} km",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )

                            Row {
                                if (place.contactPhone.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            openDialer(context, place.contactPhone)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Call",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        openGoogleMapsNavigation(context, place.location, place.nameEn)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Navigate",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun openDialer(context: android.content.Context, phone: String) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$phone")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handled
    }
}
