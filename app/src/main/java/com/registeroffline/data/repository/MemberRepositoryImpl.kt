package com.registeroffline.data.repository

import android.content.Context
import com.registeroffline.core.local.dao.MemberDao
import com.registeroffline.core.util.ImageCompressor
import com.registeroffline.data.remote.api.ApiService
import com.registeroffline.data.remote.dto.MemberListItem
import com.registeroffline.domain.model.Member
import com.registeroffline.domain.repository.MemberRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Locale
import javax.inject.Inject

class MemberRepositoryImpl @Inject constructor(
    private val memberDao: MemberDao,
    private val api: ApiService,
    @ApplicationContext private val context: Context,
) : MemberRepository {

    // ── Local ──
    override fun getDraftMembers(): Flow<List<Member>> =
        memberDao.getDraftMembers().map { list -> list.map { it.toDomain() } }

    override fun getSyncedMembers(): Flow<List<Member>> =
        memberDao.getSyncedMembers().map { list -> list.map { it.toDomain() } }

    override fun getDraftCount(): Flow<Int> = memberDao.getDraftCount()

    override suspend fun saveDraft(member: Member): Long =
        memberDao.insert(member.toEntity())

    override suspend fun updateDraft(member: Member) =
        memberDao.update(member.toEntity())

    override suspend fun getMemberById(id: Long): Member? =
        memberDao.getMemberById(id)?.toDomain()

    override suspend fun deleteDraft(member: Member) =
        memberDao.delete(member.toEntity())

    /** Exposed for BulkSyncUseCase to get a snapshot (not flow) */
    suspend fun getDraftMembersOnce(): List<Member> =
        memberDao.getDraftMembersOnce().map { it.toDomain() }

    // ── Remote ──
    override suspend fun uploadMember(member: Member): Result<Unit> {
        return try {
            val textType = "text/plain".toMediaType()
            fun String.toBody() = this.toRequestBody(textType)

            // Compress + prepare KTP files
            val ktpPart = member.ktpFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val compressed = ImageCompressor.compress(context, file)
                    buildImagePart("ktp_file", compressed)
                } else null
            }

            val ktpSecondaryPart = member.ktpFileSecondaryPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val compressed = ImageCompressor.compress(context, file)
                    buildImagePart("ktp_file_secondary", compressed)
                } else null
            }

            val response = api.uploadMember(
                name = member.name.toBody(),
                nik = member.nik.toBody(),
                phone = member.phone.toBody(),
                birthPlace = member.birthPlace.toBody(),
                birthDate = member.birthDate.toBody(),
                status = member.maritalStatus.toBody(),
                occupation = member.occupation.toBody(),
                address = member.address.toBody(),
                provinsi = member.provinsi.toBody(),
                kotaKabupaten = member.kotaKabupaten.toBody(),
                kecamatan = member.kecamatan.toBody(),
                kelurahan = member.kelurahan.toBody(),
                kodePos = member.kodePos.toBody(),
                alamatDomisili = member.alamatDomisili.toBody(),
                provinsiDomisili = member.provinsiDomisili.toBody(),
                kotaKabupatenDomisili = member.kotaKabupatenDomisili.toBody(),
                kecamatanDomisili = member.kecamatanDomisili.toBody(),
                kelurahanDomisili = member.kelurahanDomisili.toBody(),
                kodePosDomisili = member.kodePosDomisili.toBody(),
                ktpFile = ktpPart,
                ktpFileSecondary = ktpSecondaryPart,
            )

            if (response.isSuccessful) {
                memberDao.markAsSynced(member.id)
                Result.success(Unit)
            } else {
                val err = response.errorBody()?.string()
                Result.failure(Exception(err ?: "Upload gagal (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Upload gagal: ${e.message}"))
        }
    }

    private fun buildImagePart(fieldName: String, file: File): MultipartBody.Part {
    val mimeType = detectImageMimeType(file.name)
    // Pastikan filename punya extension yg valid
    val safeFilename = ensureImageExtension(file.name, mimeType)
    val body = file.asRequestBody(mimeType.toMediaType())
    return MultipartBody.Part.createFormData(fieldName, safeFilename, body)
    }

    private fun detectImageMimeType(filename: String): String {
    val lower = filename.lowercase(Locale.ROOT)
    return when {
    lower.endsWith(".png") -> "image/png"
    lower.endsWith(".jpeg") -> "image/jpeg"
    lower.endsWith(".jpg") -> "image/jpeg"
    lower.endsWith(".pdf") -> "application/pdf"
    else -> "image/jpeg" // default fallback (kamera Android selalu JPEG)
    }
    }

    private fun ensureImageExtension(filename: String, mimeType: String): String {
    val lower = filename.lowercase(Locale.ROOT)
    val hasValidExt = lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
    lower.endsWith(".png") || lower.endsWith(".pdf")
    if (hasValidExt) return filename

    val ext = when (mimeType) {
    "image/png" -> ".png"
    "application/pdf" -> ".pdf"
    else -> ".jpg"
    }
    return "$filename$ext"
    }

    override suspend fun fetchSyncedMembers(): Result<List<Member>> {
    return try {
    val response = api.getMembers()
    if (response.isSuccessful) {
    val list = response.body()?.map { it.toDomain() } ?: emptyList()
    Result.success(list)
    } else {
    Result.failure(Exception("Gagal memuat data member"))
    }
    } catch (e: Exception) {
    Result.failure(Exception("Tidak dapat terhubung ke server"))
    }
    }

    private fun MemberListItem.toDomain() = Member(
    name = name ?: "",
    nik = nik ?: "",
    phone = phone ?: "",
    syncStatus = "Synced",
    ktpUrl = ktpUrl,
    ktpUrlSecondary = ktpUrlSecondary,
    )
    }