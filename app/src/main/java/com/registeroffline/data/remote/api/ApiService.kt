package com.registeroffline.data.remote.api

import com.registeroffline.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──
    @POST("register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    // ── Members ──
    @Multipart
    @POST("member")
    suspend fun uploadMember(
        @Part("name") name: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("birth_place") birthPlace: RequestBody,
        @Part("birth_date") birthDate: RequestBody,
        @Part("status") status: RequestBody,
        @Part("occupation") occupation: RequestBody,
        @Part("address") address: RequestBody,
        @Part("provinsi") provinsi: RequestBody,
        @Part("kota_kabupaten") kotaKabupaten: RequestBody,
        @Part("kecamatan") kecamatan: RequestBody,
        @Part("kelurahan") kelurahan: RequestBody,
        @Part("kode_pos") kodePos: RequestBody,
        @Part("alamat_domisili") alamatDomisili: RequestBody,
        @Part("provinsi_domisili") provinsiDomisili: RequestBody,
        @Part("kota_kabupaten_domisili") kotaKabupatenDomisili: RequestBody,
        @Part("kecamatan_domisili") kecamatanDomisili: RequestBody,
        @Part("kelurahan_domisili") kelurahanDomisili: RequestBody,
        @Part("kode_pos_domisili") kodePosDomisili: RequestBody,
        @Part ktpFile: MultipartBody.Part?,
        @Part ktpFileSecondary: MultipartBody.Part?,
    ): Response<UploadMemberResponse>

    @GET("member")
    suspend fun getMembers(): Response<List<MemberListItem>>
}
