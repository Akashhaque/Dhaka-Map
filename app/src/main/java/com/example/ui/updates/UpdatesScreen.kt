package com.example.ui.updates

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.datasource.DhakaBoundaryData
import com.example.data.model.AppLanguage
import com.example.data.model.CommunityReport
import com.example.data.model.IncidentType
import com.example.data.model.ReportVerificationStatus
import com.example.data.repository.CommunityReportsRepository
import com.example.ui.map.openGoogleMapsNavigation
import com.google.android.gms.maps.model.LatLng

@Composable
fun UpdatesScreen(
    reportsRepository: CommunityReportsRepository,
    language: AppLanguage,
    onViewOnMap: (CommunityReport) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    val reports by reportsRepository.reports.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedTypeFilter by remember { mutableStateOf<IncidentType?>(null) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    val filteredReports = remember(reports, selectedTypeFilter) {
        reports.filter { report ->
            selectedTypeFilter == null || report.incidentType == selectedTypeFilter
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("updates_screen_root")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = if (isBn) "নাগরিক লাইভ আপডেট" else "Dhaka Community Live Updates",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isBn) "যানজট, জলাবদ্ধতা ও সড়ক পরিস্থিতির তাৎক্ষণিক খবর" else "Real-time traffic bottlenecks, waterlogging & transit alerts",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text(if (isBn) "সকল রিপোর্ট" else "All Reports") }
                )

                IncidentType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == type) null else type
                        },
                        label = { Text(if (isBn) type.titleBn else type.titleEn) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reports Feed
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize().testTag("updates_feed_list")
            ) {
                items(filteredReports, key = { it.id }) { report ->
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = when (report.incidentType) {
                                        IncidentType.TRAFFIC_CONGESTION -> Color(0xFFFFF3E0)
                                        IncidentType.WATERLOGGING -> Color(0xFFE1F5FE)
                                        IncidentType.ACCIDENT -> Color(0xFFFFEBEE)
                                        IncidentType.ROAD_CLOSURE -> Color(0xFFFBE9E7)
                                        else -> Color(0xFFEDE7F6)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isBn) report.incidentType.titleBn else report.incidentType.titleEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (report.incidentType) {
                                            IncidentType.TRAFFIC_CONGESTION -> Color(0xFFE65100)
                                            IncidentType.WATERLOGGING -> Color(0xFF0277BD)
                                            IncidentType.ACCIDENT -> Color(0xFFC62828)
                                            IncidentType.ROAD_CLOSURE -> Color(0xFFD84315)
                                            else -> Color(0xFF4527A0)
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBn) "যাচাইকৃত" else "Citizen Verified",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold
                                    )
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

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "📍 ${report.locationLabel} • ${report.area}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = {
                                            reportsRepository.confirmReport(report.id)
                                        },
                                        enabled = !report.hasConfirmedByMe
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ThumbUp,
                                            contentDescription = "Confirm",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${report.confirmationsCount} ${if (isBn) "নিশ্চিত" else "Confirm"}",
                                            fontSize = 12.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            reportsRepository.flagReport(report.id)
                                            Toast.makeText(
                                                context,
                                                if (isBn) "রিপোর্টটি পর্যবেক্ষণে পাঠানো হয়েছে" else "Report flagged for review",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Flag,
                                            contentDescription = "Flag spam",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onViewOnMap(report) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBn) "ম্যাপে দেখুন" else "View on Map", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to Submit Report
        ExtendedFloatingActionButton(
            onClick = { showSubmitDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(if (isBn) "রিপোর্ট দিন" else "Post Update") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 16.dp)
                .testTag("submit_report_fab")
        )

        // Submit Incident Dialog
        if (showSubmitDialog) {
            SubmitReportDialog(
                isBn = isBn,
                onDismiss = { showSubmitDialog = false },
                onSubmit = { type, title, desc, area, lat, lng ->
                    val result = reportsRepository.submitReport(
                        incidentType = type,
                        titleEn = title,
                        titleBn = title,
                        description = desc,
                        location = LatLng(lat, lng),
                        locationLabel = "$area Main Rd",
                        area = area,
                        userId = "citizen_user",
                        userDisplayName = "Dhaka Citizen"
                    )
                    if (result.isSuccess) {
                        Toast.makeText(
                            context,
                            if (isBn) "রিপোর্ট সফলভাবে জমা হয়েছে!" else "Report published to live feed!",
                            Toast.LENGTH_SHORT
                        ).show()
                        showSubmitDialog = false
                    } else {
                        Toast.makeText(
                            context,
                            result.exceptionOrNull()?.message ?: "Submission error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
    }
}

@Composable
fun SubmitReportDialog(
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (IncidentType, String, String, String, Double, Double) -> Unit
) {
    var selectedType by remember { mutableStateOf(IncidentType.TRAFFIC_CONGESTION) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf("Mirpur") }

    val dhakaAreas = listOf(
        "Mirpur" to Pair(23.8062, 90.3639),
        "Farmgate" to Pair(23.7570, 90.3905),
        "Gulshan" to Pair(23.7925, 90.4078),
        "Dhanmondi" to Pair(23.7461, 90.3742),
        "Uttara" to Pair(23.8728, 90.3970),
        "Motijheel" to Pair(23.7314, 90.4184),
        "Badda" to Pair(23.7800, 90.4260),
        "Old Dhaka" to Pair(23.7120, 90.4000)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isBn) "নতুন নাগরিক রিপোর্ট" else "Submit Community Report")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = if (isBn) "⚠️ কেবলমাত্র ঢাকা মেট্রোপলিটন এলাকার জন্য প্রযোজ্য" else "📍 Dhaka Metropolitan Area Coverage Only",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Text(if (isBn) "সমস্যার ধরন:" else "Incident Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IncidentType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(if (isBn) type.titleBn else type.titleEn, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBn) "শিরোনাম (যেমন: রাস্তায় তীব্র যানজট)" else "Short Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isBn) "বিবরণ (বিকল্প রাস্তা ইত্যাদি)" else "Description & Advice") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(if (isBn) "এলাকা নির্বাচন করুন:" else "Select Dhaka Area:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dhakaAreas.forEach { (area, _) ->
                        FilterChip(
                            selected = selectedArea == area,
                            onClick = { selectedArea = area },
                            label = { Text(area, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val coords = dhakaAreas.firstOrNull { it.first == selectedArea }?.second
                        ?: Pair(23.78088, 90.4152)
                    onSubmit(selectedType, title, description, selectedArea, coords.first, coords.second)
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (isBn) "জমা দিন" else "Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
