package com.example.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.datasource.DhakaBoundaryData
import com.example.data.datasource.DhakaPlacesData
import com.example.data.model.AppLanguage
import com.example.data.model.CommunityReport
import com.example.data.model.DhakaPlace
import com.example.data.model.IncidentType
import com.example.data.model.PlaceCategory
import com.google.android.gms.maps.model.LatLng
import kotlin.math.hypot

@Composable
fun DhakaVectorMapView(
    places: List<DhakaPlace>,
    reports: List<CommunityReport>,
    selectedPlace: DhakaPlace?,
    selectedReport: CommunityReport?,
    routePreview: MapRoutePreview?,
    isTransitEnabled: Boolean,
    language: AppLanguage,
    onSelectPlace: (DhakaPlace) -> Unit,
    onSelectReport: (CommunityReport) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isBn = language == AppLanguage.BANGLA

    // Pan and zoom transformation states
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Bounding coordinates of Dhaka
    val minLat = 23.6800
    val maxLat = 23.9000
    val minLng = 90.3200
    val maxLng = 90.5200

    fun latLngToScreen(latLng: LatLng, canvasWidth: Float, canvasHeight: Float): Offset {
        val normX = ((latLng.longitude - minLng) / (maxLng - minLng)).toFloat()
        // Invert Y because latitude increases northward (up)
        val normY = (1f - ((latLng.latitude - minLat) / (maxLat - minLat))).toFloat()

        val basePadding = 60f
        val mapW = canvasWidth - (basePadding * 2)
        val mapH = canvasHeight - (basePadding * 2)

        val unscaledX = basePadding + normX * mapW
        val unscaledY = basePadding + normY * mapH

        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        val finalX = centerX + (unscaledX - centerX + panOffset.x) * zoomScale
        val finalY = centerY + (unscaledY - centerY + panOffset.y) * zoomScale

        return Offset(finalX, finalY)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.7f, 4.5f)
                    panOffset += pan / zoomScale
                }
            }
            .pointerInput(places, reports, zoomScale, panOffset) {
                detectTapGestures { tapOffset ->
                    val hitRadius = 45f * zoomScale.coerceAtLeast(1f)
                    var hitPlace: DhakaPlace? = null
                    for (place in places) {
                        val pt = latLngToScreen(place.location, size.width.toFloat(), size.height.toFloat())
                        val dist = hypot(tapOffset.x - pt.x, tapOffset.y - pt.y)
                        if (dist <= hitRadius) {
                            hitPlace = place
                            break
                        }
                    }

                    if (hitPlace != null) {
                        onSelectPlace(hitPlace)
                        return@detectTapGestures
                    }

                    var hitReport: CommunityReport? = null
                    for (rep in reports) {
                        val pt = latLngToScreen(rep.location, size.width.toFloat(), size.height.toFloat())
                        val dist = hypot(tapOffset.x - pt.x, tapOffset.y - pt.y)
                        if (dist <= hitRadius) {
                            hitReport = rep
                            break
                        }
                    }

                    if (hitReport != null) {
                        onSelectReport(hitReport)
                        return@detectTapGestures
                    }

                    onClearSelection()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Background surface
            drawRect(
                color = if (isDark) Color(0xFF131715) else Color(0xFFF7FAF7)
            )

            // 1. Draw Dhaka Administrative Boundary Polygon
            val boundaryPath = Path()
            val polyPoints = DhakaBoundaryData.DHAKA_METRO_POLYGON
            if (polyPoints.isNotEmpty()) {
                val start = latLngToScreen(polyPoints.first(), canvasW, canvasH)
                boundaryPath.moveTo(start.x, start.y)
                for (i in 1 until polyPoints.size) {
                    val p = latLngToScreen(polyPoints[i], canvasW, canvasH)
                    boundaryPath.lineTo(p.x, p.y)
                }
                boundaryPath.close()

                drawPath(
                    path = boundaryPath,
                    color = if (isDark) Color(0xFF1B3329) else Color(0xFFE2F4EC),
                    style = Fill
                )
                drawPath(
                    path = boundaryPath,
                    color = if (isDark) Color(0xFF388E3C) else Color(0xFF00796B),
                    style = Stroke(width = 3f * zoomScale)
                )
            }

            // 2. Draw Water Bodies (Hatirjheel, Buriganga, Turag Rivers)
            val waterColor = if (isDark) Color(0xFF1E3A4C) else Color(0xFFB3E5FC)

            // Buriganga River (South)
            val buriganga = Path().apply {
                val p1 = latLngToScreen(LatLng(23.6820, 90.3500), canvasW, canvasH)
                val p2 = latLngToScreen(LatLng(23.7020, 90.3900), canvasW, canvasH)
                val p3 = latLngToScreen(LatLng(23.6950, 90.4150), canvasW, canvasH)
                val p4 = latLngToScreen(LatLng(23.6780, 90.4400), canvasW, canvasH)
                moveTo(p1.x, p1.y)
                quadraticTo(p2.x, p2.y, p3.x, p3.y)
                lineTo(p4.x, p4.y)
            }
            drawPath(buriganga, waterColor, style = Stroke(width = 16f * zoomScale))

            // Hatirjheel Lake (Central)
            val hatirjheel = Path().apply {
                val p1 = latLngToScreen(LatLng(23.7710, 90.3980), canvasW, canvasH)
                val p2 = latLngToScreen(LatLng(23.7690, 90.4150), canvasW, canvasH)
                val p3 = latLngToScreen(LatLng(23.7730, 90.4280), canvasW, canvasH)
                moveTo(p1.x, p1.y)
                quadraticTo(p2.x, p2.y, p3.x, p3.y)
            }
            drawPath(hatirjheel, waterColor, style = Stroke(width = 10f * zoomScale))

            // 3. Draw Major Road Grid Lines
            val roadColor = if (isDark) Color(0xFF2C3530) else Color(0xFFE0E0E0)
            val highwayColor = if (isDark) Color(0xFF424F47) else Color(0xFFFFE0B2)

            // Mirpur Road
            drawLine(
                color = highwayColor,
                start = latLngToScreen(LatLng(23.8200, 90.3600), canvasW, canvasH),
                end = latLngToScreen(LatLng(23.7350, 90.3850), canvasW, canvasH),
                strokeWidth = 6f * zoomScale
            )

            // Airport Road / VIP Road
            drawLine(
                color = highwayColor,
                start = latLngToScreen(LatLng(23.8800, 90.4050), canvasW, canvasH),
                end = latLngToScreen(LatLng(23.7400, 90.3950), canvasW, canvasH),
                strokeWidth = 7f * zoomScale
            )

            // Progoti Sarani (East)
            drawLine(
                color = roadColor,
                start = latLngToScreen(LatLng(23.8250, 90.4250), canvasW, canvasH),
                end = latLngToScreen(LatLng(23.7600, 90.4350), canvasW, canvasH),
                strokeWidth = 5f * zoomScale
            )

            // 4. Draw MRT Line 6 Track & Viaduct (Cyan Spine)
            if (isTransitEnabled) {
                val metroLine = Path()
                val metroStations = DhakaPlacesData.METRO_STATIONS
                if (metroStations.isNotEmpty()) {
                    val firstStation = latLngToScreen(metroStations.first().location, canvasW, canvasH)
                    metroLine.moveTo(firstStation.x, firstStation.y)
                    for (i in 1 until metroStations.size) {
                        val pt = latLngToScreen(metroStations[i].location, canvasW, canvasH)
                        metroLine.lineTo(pt.x, pt.y)
                    }

                    // Shadow/Viaduct glow
                    drawPath(
                        path = metroLine,
                        color = Color(0x6600E5FF),
                        style = Stroke(width = 10f * zoomScale)
                    )
                    // Core track line
                    drawPath(
                        path = metroLine,
                        color = Color(0xFF00B8D4),
                        style = Stroke(width = 4f * zoomScale)
                    )
                }
            }

            // 5. Draw Route Preview (if active)
            routePreview?.let { preview ->
                if (preview.points.size >= 2) {
                    val routePath = Path()
                    val pStart = latLngToScreen(preview.points.first(), canvasW, canvasH)
                    routePath.moveTo(pStart.x, pStart.y)
                    for (i in 1 until preview.points.size) {
                        val p = latLngToScreen(preview.points[i], canvasW, canvasH)
                        routePath.lineTo(p.x, p.y)
                    }
                    drawPath(
                        path = routePath,
                        color = Color(0xFFFF9800),
                        style = Stroke(width = 7f * zoomScale)
                    )
                }
            }

            // 6. Draw Community Incidents (Warning/Alert indicators)
            reports.forEach { report ->
                val pt = latLngToScreen(report.location, canvasW, canvasH)
                val alertColor = when (report.incidentType) {
                    IncidentType.TRAFFIC_CONGESTION -> Color(0xFFFFA000)
                    IncidentType.WATERLOGGING -> Color(0xFF0288D1)
                    IncidentType.ACCIDENT -> Color(0xFFD32F2F)
                    else -> Color(0xFFE64A19)
                }

                // Outer alert ripple
                drawCircle(
                    color = alertColor.copy(alpha = 0.25f),
                    radius = 16f * zoomScale,
                    center = pt
                )
                // Center pin
                drawCircle(
                    color = alertColor,
                    radius = 7f * zoomScale,
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f * zoomScale,
                    center = pt
                )
            }

            // 7. Draw Places & MRT Stations
            places.forEach { place ->
                val pt = latLngToScreen(place.location, canvasW, canvasH)
                val isSelected = selectedPlace?.id == place.id

                val placeColor = when (place.category) {
                    PlaceCategory.METRO_RAIL -> Color(0xFF00ACC1)
                    PlaceCategory.HOSPITALS -> Color(0xFFE53935)
                    PlaceCategory.POLICE -> Color(0xFF1E88E5)
                    PlaceCategory.FIRE_SERVICE -> Color(0xFFFB8C00)
                    PlaceCategory.PARKS -> Color(0xFF43A047)
                    PlaceCategory.SHOPPING -> Color(0xFFD81B60)
                    PlaceCategory.BUS_TERMINALS -> Color(0xFF8E24AA)
                    else -> Color(0xFF00897B)
                }

                if (isSelected) {
                    // Pulsing selection halo
                    drawCircle(
                        color = placeColor.copy(alpha = 0.35f),
                        radius = 24f * zoomScale,
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 11f * zoomScale,
                        center = pt
                    )
                }

                // Place pin body
                drawCircle(
                    color = placeColor,
                    radius = if (isSelected) 9f * zoomScale else 6f * zoomScale,
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 4f * zoomScale else 2f * zoomScale,
                    center = pt
                )
            }
        }
    }
}
