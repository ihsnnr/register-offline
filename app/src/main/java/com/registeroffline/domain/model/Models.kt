package com.registeroffline.domain.model

data class Member(
    val id: Long = 0,
    val name: String = "",
    val nik: String = "",
    val phone: String = "",
    val birthPlace: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val maritalStatus: String = "",
    val occupation: String = "",
    // KTP Address
    val address: String = "",
    val provinsi: String = "",
    val kotaKabupaten: String = "",
    val kecamatan: String = "",
    val kelurahan: String = "",
    val kodePos: String = "",
    // Domicile Address
    val alamatDomisili: String = "",
    val provinsiDomisili: String = "",
    val kotaKabupatenDomisili: String = "",
    val kecamatanDomisili: String = "",
    val kelurahanDomisili: String = "",
    val kodePosDomisili: String = "",
    // KTP photos
    val ktpFilePath: String? = null,
    val ktpFileSecondaryPath: String? = null,
    // Sync
    val syncStatus: String = "Draft",
    // Server data (for synced members)
    val ktpUrl: String? = null,
    val ktpUrlSecondary: String? = null,
)

data class UserProfile(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
)
