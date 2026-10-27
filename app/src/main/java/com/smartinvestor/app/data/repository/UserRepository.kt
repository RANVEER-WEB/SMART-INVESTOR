package com.smartinvestor.app.data.repository

import com.smartinvestor.app.data.remote.SupabaseClient
import com.smartinvestor.app.domain.model.KycData
import com.smartinvestor.app.domain.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ══════════════════════════════════════════════════════════
// Smart Investor — User Repository
// Manages user profiles and KYC data in Supabase
// All Supabase access is on Dispatchers.IO
// ══════════════════════════════════════════════════════════

@Singleton
class UserRepository @Inject constructor() {

    private val supabase get() = SupabaseClient.client

    /**
     * Get the current user's profile from Supabase.
     */
    suspend fun getUserProfile(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("Not authenticated")
            val user = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<User>()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user profile.
     */
    suspend fun updateProfile(user: User): Result<User> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["profiles"]
                .update(user) { filter { eq("id", user.id) } }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save KYC data for the current user.
     */
    suspend fun saveKycData(kycData: KycData): Result<KycData> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("Not authenticated")
            supabase.postgrest["kyc_data"].upsert(kycData) {
                filter { eq("user_id", userId) }
            }
            Result.success(kycData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get KYC data for the current user.
     */
    suspend fun getKycData(): Result<KycData> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("Not authenticated")
            val kyc = supabase.postgrest["kyc_data"]
                .select { filter { eq("user_id", userId) } }
                .decodeSingle<KycData>()
            Result.success(kyc)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
