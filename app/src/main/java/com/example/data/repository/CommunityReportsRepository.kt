package com.example.data.repository

import com.example.data.datasource.DhakaBoundaryData
import com.example.data.datasource.DhakaPlacesData
import com.example.data.model.CommunityReport
import com.example.data.model.IncidentType
import com.example.data.model.ReportVerificationStatus
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CommunityReportsRepository {

    private val _reports = MutableStateFlow<List<CommunityReport>>(
        DhakaPlacesData.INITIAL_COMMUNITY_REPORTS
    )
    val reports: Flow<List<CommunityReport>> = _reports.asStateFlow()

    /**
     * Returns approved, non-expired reports for public map display
     */
    fun getApprovedPublicReports(): List<CommunityReport> {
        val now = System.currentTimeMillis()
        return _reports.value.filter {
            it.verificationStatus == ReportVerificationStatus.APPROVED &&
                    it.expiryTimestampMillis > now
        }
    }

    /**
     * Submits a new community report.
     * Validates that the report location is strictly within the Dhaka Metropolitan Area.
     * Returns Result.success or Result.failure with descriptive error.
     */
    fun submitReport(
        incidentType: IncidentType,
        titleEn: String,
        titleBn: String,
        description: String,
        location: LatLng,
        locationLabel: String,
        area: String,
        userId: String,
        userDisplayName: String,
        autoApproveForDemo: Boolean = true
    ): Result<CommunityReport> {
        // Enforce Dhaka Boundary check
        if (!DhakaBoundaryData.isInsideDhaka(location)) {
            return Result.failure(
                IllegalArgumentException("Report location is outside Dhaka Metropolitan Area boundary.")
            )
        }

        if (titleEn.isBlank() && titleBn.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Report title cannot be empty.")
            )
        }

        val effectiveTitleEn = if (titleEn.isNotBlank()) titleEn else incidentType.titleEn
        val effectiveTitleBn = if (titleBn.isNotBlank()) titleBn else incidentType.titleBn

        val newReport = CommunityReport(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            incidentType = incidentType,
            titleEn = effectiveTitleEn,
            titleBn = effectiveTitleBn,
            description = description.ifBlank { "Citizen report from $area." },
            location = location,
            locationLabel = locationLabel.ifBlank { area },
            area = area,
            userId = userId,
            userDisplayName = userDisplayName,
            verificationStatus = if (autoApproveForDemo) ReportVerificationStatus.APPROVED else ReportVerificationStatus.PENDING_REVIEW,
            confirmationsCount = 1,
            hasConfirmedByMe = true
        )

        _reports.value = listOf(newReport) + _reports.value
        return Result.success(newReport)
    }

    /**
     * Confirm a report: upvotes/verifies incident accuracy
     */
    fun confirmReport(reportId: String): Boolean {
        var updated = false
        _reports.value = _reports.value.map { report ->
            if (report.id == reportId && !report.hasConfirmedByMe) {
                updated = true
                report.copy(
                    confirmationsCount = report.confirmationsCount + 1,
                    hasConfirmedByMe = true
                )
            } else {
                report
            }
        }
        return updated
    }

    /**
     * Flag report for moderation
     */
    fun flagReport(reportId: String) {
        _reports.value = _reports.value.map { report ->
            if (report.id == reportId) {
                report.copy(flagsCount = report.flagsCount + 1)
            } else {
                report
            }
        }
    }

    /**
     * Admin workflow: change verification status
     */
    fun updateReportStatus(reportId: String, newStatus: ReportVerificationStatus) {
        _reports.value = _reports.value.map { report ->
            if (report.id == reportId) {
                report.copy(verificationStatus = newStatus)
            } else {
                report
            }
        }
    }
}
