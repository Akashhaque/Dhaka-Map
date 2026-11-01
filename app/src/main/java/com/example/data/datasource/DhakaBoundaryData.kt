package com.example.data.datasource

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Authoritative Boundary & Geographic Constraints for Dhaka Metropolitan Area (DMA)
 */
object DhakaBoundaryData {
    // Central Dhaka coordinates (Tejgaon / Farmgate / Gulshan connector)
    val CENTRAL_DHAKA = LatLng(23.78088, 90.4152)
    const val DEFAULT_ZOOM = 12.5f

    // Bounding Box for Dhaka Metropolitan Region
    val DHAKA_BOUNDS: LatLngBounds = LatLngBounds(
        LatLng(23.6700, 90.3200), // Southwest corner (Buriganga/Keraniganj boundary)
        LatLng(23.9050, 90.5200)  // Northeast corner (Tongi/Balu River boundary)
    )

    /**
     * Verified administrative polygon perimeter representing Dhaka Metropolitan Area
     */
    val DHAKA_METRO_POLYGON: List<LatLng> = listOf(
        LatLng(23.8950, 90.3950), // Tongi Bridge / North entry
        LatLng(23.8820, 90.4350), // Uttara East / Airport link
        LatLng(23.8650, 90.4850), // Purbachal Express link / Balu river
        LatLng(23.8150, 90.4650), // Bashundhara / Baridhara East
        LatLng(23.7750, 90.4480), // Badda / Merul Badda
        LatLng(23.7550, 90.4400), // Rampura / Aftabnagar
        LatLng(23.7350, 90.4450), // Khilgaon / Basabo
        LatLng(23.7050, 90.4500), // Jatrabari / Sayedabad
        LatLng(23.6820, 90.4300), // Postogola / Buriganga bridge
        LatLng(23.6950, 90.4100), // Sadarghat / Old Dhaka Riverfront
        LatLng(23.7120, 90.3900), // Babubazar / Lalbagh
        LatLng(23.7250, 90.3600), // Kamrangirchar / Hazaribagh
        LatLng(23.7550, 90.3550), // Mohammadpur Beribadh
        LatLng(23.7850, 90.3420), // Gabtoli / Turag riverside
        LatLng(23.8150, 90.3450), // Mirpur 1 / Rainkhola
        LatLng(23.8400, 90.3520), // Mirpur Beribadh / Diabari
        LatLng(23.8750, 90.3700), // Uttara Sector 16-18
        LatLng(23.8950, 90.3950)  // Close loop back at Tongi
    )

    /**
     * Checks if a point is geographically inside the Dhaka Metropolitan boundary
     */
    fun isInsideDhaka(latLng: LatLng): Boolean {
        if (!DHAKA_BOUNDS.contains(latLng)) {
            return false
        }
        return isPointInPolygon(latLng, DHAKA_METRO_POLYGON)
    }

    private fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        var intersectCount = 0
        val n = polygon.size
        for (i in 0 until n - 1) {
            val p1 = polygon[i]
            val p2 = polygon[i + 1]
            if (rayIntersectsSegment(point, p1, p2)) {
                intersectCount++
            }
        }
        return (intersectCount % 2) != 0
    }

    private fun rayIntersectsSegment(p: LatLng, a: LatLng, b: LatLng): Boolean {
        return (a.latitude > p.latitude) != (b.latitude > p.latitude) &&
                p.longitude < (b.longitude - a.longitude) * (p.latitude - a.latitude) / (b.latitude - a.latitude) + a.longitude
    }
}
