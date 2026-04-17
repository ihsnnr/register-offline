package com.registeroffline.domain.usecase

import com.registeroffline.domain.model.Member
import com.registeroffline.domain.model.UserProfile
import com.registeroffline.domain.repository.AuthRepository
import com.registeroffline.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ── Auth ──
class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<String> =
        repo.login(email, password)
}

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, fullName: String): Result<String> =
        repo.register(email, password, fullName)
}

class GetProfileUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(): Result<UserProfile> = repo.getProfile()
}

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.logout()
}

class IsLoggedInUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke(): Flow<Boolean> = repo.isLoggedIn()
}

// ── Member ──
class GetDraftMembersUseCase @Inject constructor(private val repo: MemberRepository) {
    operator fun invoke(): Flow<List<Member>> = repo.getDraftMembers()
}

class GetSyncedMembersUseCase @Inject constructor(private val repo: MemberRepository) {
    operator fun invoke(): Flow<List<Member>> = repo.getSyncedMembers()
}

class GetDraftCountUseCase @Inject constructor(private val repo: MemberRepository) {
    operator fun invoke(): Flow<Int> = repo.getDraftCount()
}

class SaveDraftUseCase @Inject constructor(private val repo: MemberRepository) {
    suspend operator fun invoke(member: Member): Long = repo.saveDraft(member)
}

class UpdateDraftUseCase @Inject constructor(private val repo: MemberRepository) {
    suspend operator fun invoke(member: Member) = repo.updateDraft(member)
}

class GetMemberByIdUseCase @Inject constructor(private val repo: MemberRepository) {
    suspend operator fun invoke(id: Long): Member? = repo.getMemberById(id)
}

class UploadMemberUseCase @Inject constructor(private val repo: MemberRepository) {
    suspend operator fun invoke(member: Member): Result<Unit> = repo.uploadMember(member)
}

class BulkSyncUseCase @Inject constructor(private val repo: MemberRepository) {
    /**
     * Upload all draft members one by one.
     * Returns pair of (successCount, failedCount).
     */
    suspend operator fun invoke(
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
    ): Pair<Int, Int> {
        val drafts = repo.getDraftMembers() // we need the snapshot, not flow
        // Get current drafts via getMemberById workaround — use a direct query
        val draftList = (repo as? com.registeroffline.data.repository.MemberRepositoryImpl)
            ?.getDraftMembersOnce() ?: return 0 to 0

        var success = 0
        var failed = 0
        draftList.forEachIndexed { index, member ->
            onProgress(index + 1, draftList.size)
            val result = repo.uploadMember(member)
            if (result.isSuccess) success++ else failed++
        }
        return success to failed
    }
}

class FetchSyncedMembersUseCase @Inject constructor(private val repo: MemberRepository) {
    suspend operator fun invoke(): Result<List<Member>> = repo.fetchSyncedMembers()
}
