package com.registeroffline.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.registeroffline.core.util.NetworkMonitor
import com.registeroffline.data.repository.MemberRepositoryImpl
import com.registeroffline.domain.model.Member
import com.registeroffline.domain.repository.MemberRepository
import com.registeroffline.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val draftMembers: List<Member> = emptyList(),
    val syncedMembers: List<Member> = emptyList(),
    val draftCount: Int = 0,
    val isUploading: Boolean = false,
    val uploadProgress: String = "",
    val uploadResult: String? = null,
    val error: String? = null,
    val isFetchingSynced: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getDraftMembersUseCase: GetDraftMembersUseCase,
    getSyncedMembersUseCase: GetSyncedMembersUseCase,
    getDraftCountUseCase: GetDraftCountUseCase,
    private val uploadMemberUseCase: UploadMemberUseCase,
    private val fetchSyncedMembersUseCase: FetchSyncedMembersUseCase,
    private val memberRepository: MemberRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    val isOnline = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // Observe draft members from Room
        viewModelScope.launch {
            getDraftMembersUseCase().collect { drafts ->
                _state.update { it.copy(draftMembers = drafts, draftCount = drafts.size) }
            }
        }
        // Observe synced members from Room
        viewModelScope.launch {
            getSyncedMembersUseCase().collect { synced ->
                _state.update { it.copy(syncedMembers = synced) }
            }
        }
    }

    fun uploadSingleMember(member: Member) {
        if (!networkMonitor.isCurrentlyOnline()) {
            _state.update { it.copy(error = "Tidak ada koneksi internet") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            val result = uploadMemberUseCase(member)
            _state.update {
                if (result.isSuccess) it.copy(isUploading = false, uploadResult = "Berhasil diupload")
                else it.copy(isUploading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun bulkSync() {
        if (!networkMonitor.isCurrentlyOnline()) {
            _state.update { it.copy(error = "Tidak ada koneksi internet") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null, uploadProgress = "") }

            val repo = memberRepository as? MemberRepositoryImpl ?: return@launch
            val drafts = repo.getDraftMembersOnce()

            if (drafts.isEmpty()) {
                _state.update { it.copy(isUploading = false, error = "Tidak ada data draft") }
                return@launch
            }

            var success = 0
            var failed = 0
            drafts.forEachIndexed { index, member ->
                _state.update { it.copy(uploadProgress = "Uploading ${index + 1}/${drafts.size}...") }
                val result = uploadMemberUseCase(member)
                if (result.isSuccess) success++ else failed++
            }

            _state.update {
                it.copy(
                    isUploading = false,
                    uploadProgress = "",
                    uploadResult = "Berhasil: $success, Gagal: $failed",
                )
            }
        }
    }

    fun fetchSyncedFromServer() {
        viewModelScope.launch {
            _state.update { it.copy(isFetchingSynced = true) }
            fetchSyncedMembersUseCase()
            _state.update { it.copy(isFetchingSynced = false) }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, uploadResult = null) }
    }
}
