package com.registeroffline.data.repository

import com.registeroffline.core.util.TokenManager
import com.registeroffline.data.remote.api.ApiService
import com.registeroffline.data.remote.dto.LoginRequest
import com.registeroffline.data.remote.dto.RegisterRequest
import com.registeroffline.domain.model.UserProfile
import com.registeroffline.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val token = response.body()?.token
                if (!token.isNullOrBlank()) {
                    tokenManager.saveToken(token)
                    // Fetch and cache profile after login
                    try {
                        val profileResp = api.getProfile()
                        if (profileResp.isSuccessful) {
                            val profile = profileResp.body()
                            tokenManager.saveProfile(
                                fullName = profile?.fullName ?: "",
                                email = profile?.email ?: email,
                            )
                        }
                    } catch (_: Exception) {
                        // Profile fetch is best-effort
                        tokenManager.saveProfile(fullName = "", email = email)
                    }
                    Result.success(token)
                } else {
                    Result.failure(Exception("Token kosong dari server"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Login gagal (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server: ${e.message}"))
        }
    }

    override suspend fun register(email: String, password: String, fullName: String): Result<String> {
        return try {
            val response = api.register(RegisterRequest(email, password, fullName))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Registrasi berhasil")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Registrasi gagal (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server: ${e.message}"))
        }
    }

    override suspend fun getProfile(): Result<UserProfile> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                val body = response.body()
                val profile = UserProfile(
                    id = body?.id ?: "",
                    fullName = body?.fullName ?: "",
                    email = body?.email ?: "",
                )
                tokenManager.saveProfile(profile.fullName, profile.email)
                Result.success(profile)
            } else {
                Result.failure(Exception("Gagal memuat profil"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }

    override suspend fun logout() {
        tokenManager.clear()
    }

    override fun isLoggedIn(): Flow<Boolean> = tokenManager.tokenFlow.map { !it.isNullOrBlank() }
}
