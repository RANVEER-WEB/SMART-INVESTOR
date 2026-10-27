package com.smartinvestor.app.domain.model

import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════
// Smart Investor — User Domain Model
// ══════════════════════════════════════════════════════════

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,
    val isKycVerified: Boolean = false,
    val kycStatus: KycStatus = KycStatus.NOT_STARTED,
    val role: UserRole = UserRole.USER,
    val riskProfile: RiskProfile = RiskProfile.MODERATE,
    val createdAt: String = "",
)

@Serializable
enum class RiskProfile {
    CONSERVATIVE,
    MODERATE,
    AGGRESSIVE
}

@Serializable
enum class KycStatus {
    NOT_STARTED,
    PAN_VERIFIED,
    AADHAAR_VERIFIED,
    BANK_LINKED,
    DEMAT_LINKED,
    FULLY_VERIFIED
}

@Serializable
enum class UserRole {
    USER,
    ADMIN
}

@Serializable
data class KycData(
    val panNumber: String = "",
    val panVerified: Boolean = false,
    val aadhaarNumber: String = "",
    val aadhaarVerified: Boolean = false,
    val bankAccountNumber: String = "",
    val bankIfsc: String = "",
    val bankLinked: Boolean = false,
    val dematId: String = "",
    val dematLinked: Boolean = false,
)
