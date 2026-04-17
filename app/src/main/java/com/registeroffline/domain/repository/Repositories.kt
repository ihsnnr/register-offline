package com.registeroffline.domain.repository

import com.registeroffline.domain.model.Member
import com.registeroffline.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun register(email: String, password: String, fullName: String): Result<String>
    suspend fun getProfile(): Result<UserProfile>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
}

interface MemberRepository {
    // ── Local (offline) ──
    fun getDraftMembers(): Flow<List<Member>>
    fun getSyncedMembers(): Flow<List<Member>>
    fun getDraftCount(): Flow<Int>
    suspend fun saveDraft(member: Member): Long
    suspend fun updateDraft(member: Member)
    suspend fun getMemberById(id: Long): Member?
    suspend fun deleteDraft(member: Member)

    // ── Remote (sync) ──
    suspend fun uploadMember(member: Member): Result<Unit>
    suspend fun fetchSyncedMembers(): Result<List<Member>>
}
