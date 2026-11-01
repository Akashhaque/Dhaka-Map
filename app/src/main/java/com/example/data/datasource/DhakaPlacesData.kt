package com.example.data.datasource

import com.example.data.model.CommunityReport
import com.example.data.model.DhakaPlace
import com.example.data.model.IncidentType
import com.example.data.model.PlaceCategory
import com.example.data.model.ReportVerificationStatus
import com.google.android.gms.maps.model.LatLng

object DhakaPlacesData {

    /**
     * Complete MRT Line 6 (Metro Rail) Stations from Uttara North to Motijheel
     */
    val METRO_STATIONS = listOf(
        DhakaPlace("mrt_01", "Uttara North Station", "উত্তরা উত্তর স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.8732, 90.3804), "Diabari, Uttara", "দিয়াবাড়ী, উত্তরা", "Uttara", tags = listOf("mrt6", "diabari")),
        DhakaPlace("mrt_02", "Uttara Center Station", "উত্তরা সেন্টার স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.8643, 90.3789), "Sector 16, Uttara", "সেক্টর ১৬, উত্তরা", "Uttara", tags = listOf("mrt6")),
        DhakaPlace("mrt_03", "Uttara South Station", "উত্তরা দক্ষিণ স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.8541, 90.3775), "Sector 18, Uttara", "সেক্টর ১৮, উত্তরা", "Uttara", tags = listOf("mrt6")),
        DhakaPlace("mrt_04", "Pallabi Station", "পল্লবী স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.8340, 90.3644), "Mirpur 12 / Pallabi", "মিরপুর ১২ / পল্লবী", "Mirpur", tags = listOf("mrt6")),
        DhakaPlace("mrt_05", "Mirpur 11 Station", "মিরপুর ১১ স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.8188, 90.3662), "Mirpur 11 Main Road", "মিরপুর ১১ মেইন রোড", "Mirpur", tags = listOf("mrt6")),
        DhakaPlace("mrt_06", "Mirpur 10 Station", "মিরপুর ১০ স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.8071, 90.3686), "Mirpur 10 Golchokkor", "মিরপুর ১০ গোলচত্বর", "Mirpur", tags = listOf("mrt6", "hub")),
        DhakaPlace("mrt_07", "Kazipara Station", "কাজীপাড়া স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7992, 90.3725), "Begum Rokeya Sarani", "বেগম রোকেয়া সরণি", "Kazipara", tags = listOf("mrt6")),
        DhakaPlace("mrt_08", "Shewrapara Station", "শেওড়াপাড়া স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7905, 90.3752), "Begum Rokeya Sarani", "বেগম রোকেয়া সরণি", "Shewrapara", tags = listOf("mrt6")),
        DhakaPlace("mrt_09", "Agargaon Station", "আগারগাঁও স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7786, 90.3798), "Sher-e-Bangla Nagar", "শেরেবাংলা নগর", "Agargaon", tags = listOf("mrt6", "hub")),
        DhakaPlace("mrt_10", "Bijoy Sarani Station", "বিজয় সরণি স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7661, 90.3871), "Bijoy Sarani Intersection", "বিজয় সরণি মোড়", "Tejgaon", tags = listOf("mrt6")),
        DhakaPlace("mrt_11", "Farmgate Station", "ফার্মগেট স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7570, 90.3905), "Farmgate Hub, Kazi Nazrul Islam Ave", "ফার্মগেট, কাজী নজরুল ইসলাম এভিনিউ", "Farmgate", tags = listOf("mrt6", "hub")),
        DhakaPlace("mrt_12", "Karwan Bazar Station", "কাওরান বাজার স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7501, 90.3934), "Karwan Bazar Commercial Area", "কাওরান বাজার বাণিজ্যিক এলাকা", "Karwan Bazar", tags = listOf("mrt6")),
        DhakaPlace("mrt_13", "Shahbagh Station", "শাহবাগ স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7389, 90.3958), "Shahbagh Intersection, BSMMU", "শাহবাগ মোড়, বিএসএমএমইউ", "Shahbagh", tags = listOf("mrt6", "bsmmu")),
        DhakaPlace("mrt_14", "Dhaka University Station", "ঢাকা বিশ্ববিদ্যালয় স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7314, 90.3995), "TSC / Curzon Hall Area", "টিএসসি / কার্জন হল এলাকা", "Dhaka University", tags = listOf("mrt6", "tsc")),
        DhakaPlace("mrt_15", "Bangladesh Secretariat Station", "বাংলাদেশ সচিবালয় স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7279, 90.4072), "Press Club / Topkhana Rd", "প্রেস ক্লাব / তোপখানা রোড", "Secretariat", tags = listOf("mrt6")),
        DhakaPlace("mrt_16", "Motijheel Station", "মতিঝিল স্টেশন", PlaceCategory.METRO_RAIL, LatLng(23.7247, 90.4190), "Motijheel C/A & Shapla Chattar", "মতিঝিল বা/এ ও শাপলা চত্বর", "Motijheel", tags = listOf("mrt6", "terminal"))
    )

    /**
     * MRT Line 6 Polyline Route Coordinates for Map Rendering
     */
    val METRO_ROUTE_LINE: List<LatLng> = METRO_STATIONS.map { it.location }

    /**
     * Essential Emergency Hospitals & Medical Centers in Dhaka
     */
    val HOSPITALS = listOf(
        DhakaPlace("hosp_01", "Dhaka Medical College Hospital (DMCH)", "ঢাকা মেডিকেল কলেজ হাসপাতাল", PlaceCategory.HOSPITALS, LatLng(23.7258, 90.3976), "Secretariat Rd, Dhaka 1000", "সচিবালয় রোড, ঢাকা ১০০০", "Bakshibazar", 4.4, "02-55165000", isEmergency = true),
        DhakaPlace("hosp_02", "Bangabandhu Sheikh Mujib Medical University (BSMMU)", "বিএসএমএমইউ (পিজি হাসপাতাল)", PlaceCategory.HOSPITALS, LatLng(23.7394, 90.3952), "Shahbagh, Dhaka", "শাহবাগ, ঢাকা", "Shahbagh", 4.5, "02-55165600", isEmergency = true),
        DhakaPlace("hosp_03", "Square Hospital", "স্কয়ার হাসপাতাল", PlaceCategory.HOSPITALS, LatLng(23.7533, 90.3814), "18/F Bir Uttam Qazi Nuruzzaman Sarak, West Panthapath", "১৮/এফ কাজী নুরুজ্জামান সড়ক, পশ্চিম পান্থপথ", "Panthapath", 4.7, "10616", isEmergency = true),
        DhakaPlace("hosp_04", "United Hospital", "ইউনাইটেড হাসপাতাল", PlaceCategory.HOSPITALS, LatLng(23.8052, 90.4162), "Plot 15, Road 71, Gulshan 2", "প্লট ১৫, রোড ৭১, গুলশান ২", "Gulshan", 4.6, "10666", isEmergency = true),
        DhakaPlace("hosp_05", "Evercare Hospital Dhaka", "এভারকেয়ার হাসপাতাল ঢাকা", PlaceCategory.HOSPITALS, LatLng(23.8103, 90.4312), "Plot 81, Block E, Bashundhara R/A", "প্লট ৮১, ব্লক ই, বসুন্ধরা আ/এ", "Bashundhara", 4.7, "10678", isEmergency = true),
        DhakaPlace("hosp_06", "Kurmitola General Hospital", "কুর্মিটোলা জেনারেল হাসপাতাল", PlaceCategory.HOSPITALS, LatLng(23.8182, 90.4045), "Dhaka - Mymensingh Hwy, Cantonment", "ঢাকা-ময়মনসিংহ হাইওয়ে, ক্যান্টনমেন্ট", "Kurmitola", 4.3, "02-55067200", isEmergency = true),
        DhakaPlace("hosp_07", "National Institute of Cardiovascular Diseases (NICVD)", "জাতীয় হৃদরোগ ইনস্টিটিউট", PlaceCategory.HOSPITALS, LatLng(23.7719, 90.3703), "Sher-e-Bangla Nagar", "শেরেবাংলা নগর", "Agargaon", 4.5, "02-9122560", isEmergency = true)
    )

    /**
     * Primary Police Stations (Thana)
     */
    val POLICE_STATIONS = listOf(
        DhakaPlace("pol_01", "Ramna Police Station", "রমনা মডেল থানা", PlaceCategory.POLICE, LatLng(23.7431, 90.4034), "Minto Road, Ramna", "মিন্টো রোড, রমনা", "Ramna", 4.2, "01713-373125", isEmergency = true),
        DhakaPlace("pol_02", "Gulshan Police Station", "গুলশান থানা", PlaceCategory.POLICE, LatLng(23.7788, 90.4158), "Gulshan 1 Circle", "গুলশান ১ চত্বর", "Gulshan", 4.3, "01713-373155", isEmergency = true),
        DhakaPlace("pol_03", "Dhanmondi Police Station", "ধানমন্ডি থানা", PlaceCategory.POLICE, LatLng(23.7456, 90.3732), "Road 8, Dhanmondi", "রোড ৮, ধানমন্ডি", "Dhanmondi", 4.2, "01713-373130", isEmergency = true),
        DhakaPlace("pol_04", "Mirpur Model Police Station", "মিরপুর মডেল থানা", PlaceCategory.POLICE, LatLng(23.8062, 90.3639), "Mirpur 2, Dhaka", "মিরপুর ২, ঢাকা", "Mirpur", 4.0, "01713-373160", isEmergency = true),
        DhakaPlace("pol_05", "Uttara East Police Station", "উত্তরা পূর্ব থানা", PlaceCategory.POLICE, LatLng(23.8690, 90.4011), "Sector 4, Uttara", "সেক্টর ৪, উত্তরা", "Uttara", 4.1, "01713-373170", isEmergency = true),
        DhakaPlace("pol_06", "Kotwali Police Station (Old Dhaka)", "কোতোয়ালী থানা (পুরান ঢাকা)", PlaceCategory.POLICE, LatLng(23.7125, 90.4060), "Court House St, Old Dhaka", "কোর্ট হাউস স্ট্রিট, পুরান ঢাকা", "Kotwali", 4.0, "01713-373140", isEmergency = true)
    )

    /**
     * Fire Service Stations
     */
    val FIRE_SERVICE = listOf(
        DhakaPlace("fire_01", "Fire Service & Civil Defence Headquarters", "ফায়ার সার্ভিস ও সিভিল ডিফেন্স হেডকোয়ার্টার", PlaceCategory.FIRE_SERVICE, LatLng(23.7176, 90.4048), "Kazi Alauddin Rd, Bangshal", "কাজী আলাউদ্দিন রোড, বংশাল", "Bangshal", 4.7, "02-9555555", isEmergency = true),
        DhakaPlace("fire_02", "Tejgaon Fire Station", "তেজগাঁও ফায়ার স্টেশন", PlaceCategory.FIRE_SERVICE, LatLng(23.7612, 90.3958), "Tejgaon Industrial Area", "তেজগাঁও শিল্প এলাকা", "Tejgaon", 4.6, "02-9111111", isEmergency = true),
        DhakaPlace("fire_03", "Kurmitola Fire Station", "কুর্মিটোলা ফায়ার স্টেশন", PlaceCategory.FIRE_SERVICE, LatLng(23.8242, 90.4089), "Airport Road, Kurmitola", "বিমানবন্দর সড়ক, কুর্মিটোলা", "Airport", 4.6, "02-8901111", isEmergency = true)
    )

    /**
     * Major Bus & Railway Terminals
     */
    val TRANSIT_TERMINALS = listOf(
        DhakaPlace("term_01", "Kamalapur Railway Station", "কমলাপুর রেলওয়ে স্টেশন", PlaceCategory.BUS_TERMINALS, LatLng(23.7319, 90.4255), "Kamalapur, Motijheel", "কমলাপুর, মতিঝিল", "Kamalapur", 4.4, "02-9358634"),
        DhakaPlace("term_02", "Mohakhali Inter-District Bus Terminal", "মহাখালী আন্তঃজেলা বাস টার্মিনাল", PlaceCategory.BUS_TERMINALS, LatLng(23.7780, 90.4011), "Bir Uttam AK Khandakar Rd, Mohakhali", "মহাখালী, ঢাকা", "Mohakhali", 4.0),
        DhakaPlace("term_03", "Sayedabad Bus Terminal", "সায়েদাবাদ বাস টার্মিনাল", PlaceCategory.BUS_TERMINALS, LatLng(23.7102, 90.4310), "Sayedabad, Jatrabari", "সায়েদাবাদ, যাত্রাবাড়ী", "Sayedabad", 3.9),
        DhakaPlace("term_04", "Gabtoli Bus Terminal", "গাবতলী বাস টার্মিনাল", PlaceCategory.BUS_TERMINALS, LatLng(23.7845, 90.3478), "Mirpur Road, Gabtoli", "মিরপুর রোড, গাবতলী", "Gabtoli", 3.9),
        DhakaPlace("term_05", "Hazrat Shahjalal International Airport", "হযরত শাহজালাল আন্তর্জাতিক বিমানবন্দর", PlaceCategory.BUS_TERMINALS, LatLng(23.8433, 90.4039), "Airport Rd, Kurmitola", "বিমানবন্দর সড়ক, কুর্মিটোলা", "Airport", 4.3, "02-8901449")
    )

    /**
     * Leading Universities
     */
    val UNIVERSITIES = listOf(
        DhakaPlace("uni_01", "University of Dhaka (DU)", "ঢাকা বিশ্ববিদ্যালয়", PlaceCategory.UNIVERSITIES, LatLng(23.7337, 90.3929), "Nilkhet Rd, Dhaka 1000", "নীলক্ষেত রোড, ঢাকা ১০০০", "Dhaka University", 4.8),
        DhakaPlace("uni_02", "BUET (Bangladesh Univ. of Eng. & Tech.)", "বুয়েট", PlaceCategory.UNIVERSITIES, LatLng(23.7265, 90.3924), "Palashi, Dhaka 1000", "পলাশী, ঢাকা ১০০০", "Palashi", 4.9),
        DhakaPlace("uni_03", "North South University (NSU)", "নর্থ সাউথ ইউনিভার্সিটি", PlaceCategory.UNIVERSITIES, LatLng(23.8151, 90.4255), "Plot 15, Block B, Bashundhara R/A", "প্লট ১৫, ব্লক বি, বসুন্ধরা", "Bashundhara", 4.6),
        DhakaPlace("uni_04", "BRAC University", "ব্র্যাক ইউনিভার্সিটি", PlaceCategory.UNIVERSITIES, LatLng(23.7745, 90.4282), "Merul Badda, Progoti Sarani", "মেরুল বাড্ডা, প্রগতি সরণি", "Badda", 4.7)
    )

    /**
     * Famous Landmarks, Parks & Mosques
     */
    val LANDMARKS_AND_PARKS = listOf(
        DhakaPlace("land_01", "National Parliament House (Jatiya Sangsad)", "জাতীয় সংসদ ভবন", PlaceCategory.LANDMARKS, LatLng(23.7629, 90.3787), "Manik Mia Ave, Sher-e-Bangla Nagar", "মানিক মিয়া এভিনিউ", "Sher-e-Bangla Nagar", 4.8),
        DhakaPlace("land_02", "Hatirjheel Integrated Lake & Waterfront", "হাতিরঝিল লেক", PlaceCategory.PARKS, LatLng(23.7698, 90.4124), "Hatirjheel Circular Road", "হাতিরঝিল বৃত্তাকার সড়ক", "Tejgaon / Badda", 4.7),
        DhakaPlace("land_03", "Lalbagh Fort", "লালবাগ কেল্লা", PlaceCategory.LANDMARKS, LatLng(23.7196, 90.3881), "Lalbagh, Old Dhaka", "লালবাগ, পুরান ঢাকা", "Lalbagh", 4.6),
        DhakaPlace("land_04", "Ahsan Manzil (Pink Palace)", "আহসান মঞ্জিল", PlaceCategory.LANDMARKS, LatLng(23.7086, 90.4060), "Kumartoli, Sadarghat", "কুমারটুলী, সদরঘাট", "Sadarghat", 4.6),
        DhakaPlace("land_05", "Ramna Park & Suhrawardy Udyan", "রমনা পার্ক ও সোহরাওয়ার্দী উদ্যান", PlaceCategory.PARKS, LatLng(23.7383, 90.4003), "Ramna, Dhaka", "রমনা, ঢাকা", "Ramna", 4.6),
        DhakaPlace("land_06", "Baitul Mukarram National Mosque", "বায়তুল মোকাররম জাতীয় মসজিদ", PlaceCategory.MOSQUES, LatLng(23.7289, 90.4128), "Topkhana Rd, Motijheel", "তোপখানা রোড, মতিঝিল", "Paltan", 4.8),
        DhakaPlace("land_07", "Star Mosque (Tara Masjid)", "তারা মসজিদ", PlaceCategory.MOSQUES, LatLng(23.7153, 90.4009), "Armanitola, Old Dhaka", "আরমানিটোলা, পুরান ঢাকা", "Armanitola", 4.7),
        DhakaPlace("land_08", "Jamuna Future Park", "যমুনা ফিউচার পার্ক", PlaceCategory.SHOPPING, LatLng(23.8135, 90.4242), "KA-244, Kuril, Progoti Sarani", "কুড়িল, প্রগতি সরণি", "Kuril", 4.7),
        DhakaPlace("land_09", "Bashundhara City Shopping Mall", "বসুন্ধরা সিটি শপিং মল", PlaceCategory.SHOPPING, LatLng(23.7508, 90.3905), "Panthapath, Dhaka", "পান্থপথ, ঢাকা", "Panthapath", 4.6)
    )

    /**
     * Aggregate list of all verified Dhaka Places
     */
    val ALL_DHAKA_PLACES: List<DhakaPlace> =
        METRO_STATIONS + HOSPITALS + POLICE_STATIONS + FIRE_SERVICE +
        TRANSIT_TERMINALS + UNIVERSITIES + LANDMARKS_AND_PARKS

    /**
     * Initial verified live community reports for traffic, waterlogging & transit alerts in Dhaka
     */
    val INITIAL_COMMUNITY_REPORTS: List<CommunityReport> = listOf(
        CommunityReport(
            id = "rep_01",
            incidentType = IncidentType.TRAFFIC_CONGESTION,
            titleEn = "Heavy bottleneck at Mohakhali Flyover entry",
            titleBn = "মহাখালী ফ্লাইওভার প্রবেশমুখে তীব্র যানজট",
            description = "Traffic is crawling from Jahangir Gate towards Mohakhali. Plan 25+ min delay.",
            location = LatLng(23.7790, 90.3980),
            locationLabel = "Mohakhali Flyover South Entry",
            area = "Mohakhali",
            userId = "comm_user_1",
            userDisplayName = "TrafficVolunteer_Mirpur",
            verificationStatus = ReportVerificationStatus.APPROVED,
            confirmationsCount = 28
        ),
        CommunityReport(
            id = "rep_02",
            incidentType = IncidentType.WATERLOGGING,
            titleEn = "Waterlogging on Mirpur 10 to 14 link road",
            titleBn = "মিরপুর ১০ থেকে ১৪ লিংক রোডে জলাবদ্ধতা",
            description = "Knee-deep water after sudden downpour. Small cars and CNGs should avoid lane 2.",
            location = LatLng(23.8120, 90.3740),
            locationLabel = "Mirpur Section 10 Connector",
            area = "Mirpur",
            userId = "comm_user_2",
            userDisplayName = "GreenDhakaCitizen",
            verificationStatus = ReportVerificationStatus.APPROVED,
            confirmationsCount = 19
        ),
        CommunityReport(
            id = "rep_03",
            incidentType = IncidentType.TRANSIT_ALERT,
            titleEn = "MRT Line 6 normal frequency 6-8 mins",
            titleBn = "মেট্রোরেল এমআরটি ৬ স্বাভাবিক সূচিতে চালু",
            description = "Stations operating smoothly. Rapid pass recharging active at all counters.",
            location = LatLng(23.7570, 90.3905),
            locationLabel = "Farmgate Metro Station",
            area = "Farmgate",
            userId = "comm_user_3",
            userDisplayName = "MetroRider_DU",
            verificationStatus = ReportVerificationStatus.APPROVED,
            confirmationsCount = 42
        ),
        CommunityReport(
            id = "rep_04",
            incidentType = IncidentType.ROAD_DAMAGE,
            titleEn = "Utility digging ongoing at Merul Badda",
            titleBn = "মেরুল বাড্ডায় পাইপলাইন খোঁড়াখুঁড়ি",
            description = "One lane blocked near BRAC University campus. Slow vehicle movement.",
            location = LatLng(23.7745, 90.4282),
            locationLabel = "Merul Badda Progoti Sarani",
            area = "Badda",
            userId = "comm_user_4",
            userDisplayName = "CitizenReporter_Badda",
            verificationStatus = ReportVerificationStatus.APPROVED,
            confirmationsCount = 14
        )
    )
}
