package com.example.data.model

import com.google.android.gms.maps.model.LatLng

/**
 * Supported categories in Dhaka Metropolitan Area
 */
enum class PlaceCategory(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val iconName: String
) {
    METRO_RAIL("metro_rail", "Metro Rail (MRT 6)", "মেট্রোরেল (এমআরটি ৬)", "train"),
    HOSPITALS("hospitals", "Hospitals & Clinics", "হাসপাতাল ও ক্লিনিক", "local_hospital"),
    PHARMACIES("pharmacies", "Pharmacies", "ফার্মেসি", "medication"),
    POLICE("police", "Police Stations", "থানা ও পুলিশ বক্স", "local_police"),
    FIRE_SERVICE("fire_service", "Fire Service", "ফায়ার সার্ভিস", "fire_truck"),
    BUS_TERMINALS("bus_terminals", "Bus & Train Stations", "বাস ও ট্রেন টার্মিনাল", "directions_bus"),
    UNIVERSITIES("universities", "Universities & Colleges", "বিশ্ববিদ্যালয় ও কলেজ", "school"),
    SHOPPING("shopping", "Shopping Malls", "শপিং মল ও বাজার", "shopping_bag"),
    RESTAURANTS("restaurants", "Restaurants & Cafes", "রেস্তোরাঁ ও ক্যাফে", "restaurant"),
    BANKS("banks", "Banks & ATMs", "ব্যাংক ও এটিএম", "account_balance"),
    GOVERNMENT("government", "Government Offices", "সরকারি দপ্তর", "account_balance"),
    PARKS("parks", "Parks & Recreation", "পার্ক ও লেক", "park"),
    LANDMARKS("landmarks", "Historical & Landmarks", "ঐতিহাসিক ও দর্শনীয় স্থান", "account_balance"),
    MOSQUES("mosques", "Mosques & Places of Worship", "মসজিদ ও উপাসনালয়", "mosque");

    companion object {
        fun fromId(id: String): PlaceCategory = entries.firstOrNull { it.id == id } ?: LANDMARKS
    }
}

/**
 * Represents a point of interest or verified municipal place in Dhaka
 */
data class DhakaPlace(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val category: PlaceCategory,
    val location: LatLng,
    val addressEn: String,
    val addressBn: String,
    val areaName: String,
    val rating: Double = 4.5,
    val contactPhone: String = "",
    val openingHoursEn: String = "Open Daily",
    val openingHoursBn: String = "প্রতিদিন খোলা",
    val isVerified: Boolean = true,
    val tags: List<String> = emptyList(),
    val isEmergency: Boolean = false
)

/**
 * Category for Community Live Reports
 */
enum class IncidentType(
    val id: String,
    val titleEn: String,
    val titleBn: String
) {
    TRAFFIC_CONGESTION("traffic_jam", "Severe Traffic Congestion", "তীব্র যানজট"),
    WATERLOGGING("waterlogging", "Waterlogging / Rain Flooding", "জলাবদ্ধতা"),
    ROAD_CLOSURE("road_closure", "Road Closed / VIP Movement", "রাস্তা বন্ধ / ভিআইপি মুভমেন্ট"),
    ACCIDENT("accident", "Road Accident", "সড়ক দুর্ঘটনা"),
    ROAD_DAMAGE("road_damage", "Pothole / Road Damage / Digging", "ভাঙা রাস্তা / খোঁড়াখুঁড়ি"),
    TRANSIT_ALERT("transit_alert", "Metro / Bus Disruption", "মেট্রো বা গণপরিবহন বিঘ্ন");

    companion object {
        fun fromId(id: String): IncidentType = entries.firstOrNull { it.id == id } ?: TRAFFIC_CONGESTION
    }
}

/**
 * Verification state workflow for community submissions:
 * Submitted -> Pending Review -> Approved -> Published -> Expired
 */
enum class ReportVerificationStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    EXPIRED
}

/**
 * Community Live Report Model
 */
data class CommunityReport(
    val id: String,
    val incidentType: IncidentType,
    val titleEn: String,
    val titleBn: String,
    val description: String,
    val location: LatLng,
    val locationLabel: String,
    val area: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val expiryTimestampMillis: Long = System.currentTimeMillis() + (4 * 60 * 60 * 1000), // Default 4 hours
    val userId: String,
    val userDisplayName: String = "Dhaka Citizen",
    val verificationStatus: ReportVerificationStatus = ReportVerificationStatus.APPROVED,
    val confirmationsCount: Int = 1,
    val flagsCount: Int = 0,
    val hasConfirmedByMe: Boolean = false
)

/**
 * App Language Preference
 */
enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা")
}
