package com.registeroffline.core.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Required
    val name: String,
    val nik: String,
    val phone: String,
    // Identity
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
    // KTP photos (local file paths)
    val ktpFilePath: String? = null,
    val ktpFileSecondaryPath: String? = null,
    // Sync
    val syncStatus: String = "Draft", // "Draft" or "Synced"
    val createdAt: Long = System.currentTimeMillis(),
)
