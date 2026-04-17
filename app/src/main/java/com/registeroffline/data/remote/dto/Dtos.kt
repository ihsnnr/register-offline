package com.registeroffline.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Auth ──
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
)

data class RegisterResponse(val message: String?)

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String?)

data class ProfileResponse(
    val id: String?,
    @SerializedName("full_name") val fullName: String?,
    val email: String?,
)

// ── Members ──
data class UploadMemberResponse(
    val message: String?,
    val member: UploadedMember?,
)

data class UploadedMember(
    val id: Int?,
    val name: String?,
    val nik: String?,
    val phone: String?,
    @SerializedName("ktp_url") val ktpUrl: String?,
    @SerializedName("ktp_url_secondary") val ktpUrlSecondary: String?,
)

data class MemberListItem(
    val name: String?,
    val nik: String?,
    val phone: String?,
    @SerializedName("ktp_url") val ktpUrl: String?,
    @SerializedName("ktp_url_secondary") val ktpUrlSecondary: String?,
)
