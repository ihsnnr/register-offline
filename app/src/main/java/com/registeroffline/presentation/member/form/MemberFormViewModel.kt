package com.registeroffline.presentation.member.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.registeroffline.domain.model.Member
import com.registeroffline.domain.usecase.GetMemberByIdUseCase
import com.registeroffline.domain.usecase.SaveDraftUseCase
import com.registeroffline.domain.usecase.UpdateDraftUseCase
import com.registeroffline.domain.usecase.UploadMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberFormState(
    // Required
    val phone: String = "",
    val nik: String = "",
    val name: String = "",
    // KTP photos
    val ktpFilePath: String? = null,
    val ktpFileSecondaryPath: String? = null,
    // Identity info
    val birthPlace: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val maritalStatus: String = "",
    val occupation: String = "",
    // KTP address
    val address: String = "",
    val provinsi: String = "",
    val kotaKabupaten: String = "",
    val kecamatan: String = "",
    val kelurahan: String = "",
    val kodePos: String = "",
    // Domicile
    val sameAsKtp: Boolean = true,
    val alamatDomisili: String = "",
    val provinsiDomisili: String = "",
    val kotaKabupatenDomisili: String = "",
    val kecamatanDomisili: String = "",
    val kelurahanDomisili: String = "",
    val kodePosDomisili: String = "",
    // UI state
    val isEditing: Boolean = false,
    val editMemberId: Long = 0,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val savedSuccess: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class MemberFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val updateDraftUseCase: UpdateDraftUseCase,
    private val getMemberByIdUseCase: GetMemberByIdUseCase,
    private val uploadMemberUseCase: UploadMemberUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MemberFormState())
    val state = _state.asStateFlow()

    init {
        val memberId = savedStateHandle.get<String>("memberId")?.toLongOrNull() ?: -1L
        if (memberId > 0) {
            viewModelScope.launch {
                val member = getMemberByIdUseCase(memberId)
                if (member != null) {
                    _state.update {
                        it.copy(
                            isEditing = true,
                            editMemberId = member.id,
                            phone = member.phone,
                            nik = member.nik,
                            name = member.name,
                            ktpFilePath = member.ktpFilePath,
                            ktpFileSecondaryPath = member.ktpFileSecondaryPath,
                            birthPlace = member.birthPlace,
                            birthDate = member.birthDate,
                            gender = member.gender,
                            maritalStatus = member.maritalStatus,
                            occupation = member.occupation,
                            address = member.address,
                            provinsi = member.provinsi,
                            kotaKabupaten = member.kotaKabupaten,
                            kecamatan = member.kecamatan,
                            kelurahan = member.kelurahan,
                            kodePos = member.kodePos,
                            sameAsKtp = member.alamatDomisili.isBlank(),
                            alamatDomisili = member.alamatDomisili,
                            provinsiDomisili = member.provinsiDomisili,
                            kotaKabupatenDomisili = member.kotaKabupatenDomisili,
                            kecamatanDomisili = member.kecamatanDomisili,
                            kelurahanDomisili = member.kelurahanDomisili,
                            kodePosDomisili = member.kodePosDomisili,
                        )
                    }
                }
            }
        }
    }

    // ── Field updaters ──
    fun updatePhone(v: String) { _state.update { it.copy(phone = v) } }
    fun updateNik(v: String) { if (v.length <= 16) _state.update { it.copy(nik = v) } }
    fun updateName(v: String) { _state.update { it.copy(name = v) } }
    fun updateBirthPlace(v: String) { _state.update { it.copy(birthPlace = v) } }
    fun updateBirthDate(v: String) { _state.update { it.copy(birthDate = v) } }
    fun updateGender(v: String) { _state.update { it.copy(gender = v) } }
    fun updateMaritalStatus(v: String) { _state.update { it.copy(maritalStatus = v) } }
    fun updateOccupation(v: String) { _state.update { it.copy(occupation = v) } }
    fun updateAddress(v: String) { _state.update { it.copy(address = v) } }
    fun updateProvinsi(v: String) { _state.update { it.copy(provinsi = v) } }
    fun updateKotaKabupaten(v: String) { _state.update { it.copy(kotaKabupaten = v) } }
    fun updateKecamatan(v: String) { _state.update { it.copy(kecamatan = v) } }
    fun updateKelurahan(v: String) { _state.update { it.copy(kelurahan = v) } }
    fun updateKodePos(v: String) { _state.update { it.copy(kodePos = v) } }
    fun updateSameAsKtp(v: Boolean) { _state.update { it.copy(sameAsKtp = v) } }
    fun updateAlamatDomisili(v: String) { _state.update { it.copy(alamatDomisili = v) } }
    fun updateProvinsiDomisili(v: String) { _state.update { it.copy(provinsiDomisili = v) } }
    fun updateKotaKabupatenDomisili(v: String) { _state.update { it.copy(kotaKabupatenDomisili = v) } }
    fun updateKecamatanDomisili(v: String) { _state.update { it.copy(kecamatanDomisili = v) } }
    fun updateKelurahanDomisili(v: String) { _state.update { it.copy(kelurahanDomisili = v) } }
    fun updateKodePosDomisili(v: String) { _state.update { it.copy(kodePosDomisili = v) } }

    fun setKtpFile(path: String) { _state.update { it.copy(ktpFilePath = path) } }
    fun setKtpFileSecondary(path: String) { _state.update { it.copy(ktpFileSecondaryPath = path) } }

    private fun buildMember(): Member {
        val s = _state.value
        return Member(
            id = if (s.isEditing) s.editMemberId else 0,
            name = s.name,
            nik = s.nik,
            phone = s.phone,
            birthPlace = s.birthPlace,
            birthDate = s.birthDate,
            gender = s.gender,
            maritalStatus = s.maritalStatus,
            occupation = s.occupation,
            address = s.address,
            provinsi = s.provinsi,
            kotaKabupaten = s.kotaKabupaten,
            kecamatan = s.kecamatan,
            kelurahan = s.kelurahan,
            kodePos = s.kodePos,
            alamatDomisili = if (s.sameAsKtp) s.address else s.alamatDomisili,
            provinsiDomisili = if (s.sameAsKtp) s.provinsi else s.provinsiDomisili,
            kotaKabupatenDomisili = if (s.sameAsKtp) s.kotaKabupaten else s.kotaKabupatenDomisili,
            kecamatanDomisili = if (s.sameAsKtp) s.kecamatan else s.kecamatanDomisili,
            kelurahanDomisili = if (s.sameAsKtp) s.kelurahan else s.kelurahanDomisili,
            kodePosDomisili = if (s.sameAsKtp) s.kodePos else s.kodePosDomisili,
            ktpFilePath = s.ktpFilePath,
            ktpFileSecondaryPath = s.ktpFileSecondaryPath,
            syncStatus = "Draft",
        )
    }

    fun validate(): String? {
        val s = _state.value
        if (s.phone.isBlank()) return "Nomor Handphone wajib diisi"
        if (s.nik.length != 16) return "NIK harus 16 digit"
        return null
    }

    fun saveDraft() {
        val err = validate()
        if (err != null) { _state.update { it.copy(error = err) }; return }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val member = buildMember()
                if (_state.value.isEditing) {
                    updateDraftUseCase(member)
                } else {
                    saveDraftUseCase(member)
                }
                _state.update { it.copy(isSaving = false, savedSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun upload() {
        val err = validate()
        if (err != null) { _state.update { it.copy(error = err) }; return }

        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            // Save first to ensure in DB
            val member = buildMember()
            val id = if (_state.value.isEditing) {
                updateDraftUseCase(member)
                member.id
            } else {
                saveDraftUseCase(member)
            }
            val savedMember = member.copy(id = if (_state.value.isEditing) member.id else id)

            val result = uploadMemberUseCase(savedMember)
            _state.update {
                if (result.isSuccess) it.copy(isUploading = false, uploadSuccess = true)
                else it.copy(isUploading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
}
