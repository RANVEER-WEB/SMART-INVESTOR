package com.smartinvestor.app.data.repository

import com.smartinvestor.app.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import com.smartinvestor.app.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {

    /**
     * Signs up a new user using Email and Password
     */
    suspend fun signUp(email: String, password: String, fullName: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(User(email = email, fullName = fullName))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * Logs in an existing user
     */
    suspend fun signIn(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(User(email = email))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * Sends password reset email via Supabase Auth
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.resetPasswordForEmail(email)
                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * Logs out the current user
     */
    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Checks if a user is currently logged in.
     * Wrapped in try-catch to prevent crashes if the Supabase client
     * fails to initialize (e.g. no network, bad URL).
     */
    fun isAuthenticated(): Boolean {
        return try {
            SupabaseClient.client.auth.currentUserOrNull() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
