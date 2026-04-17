package com.registeroffline.core.local.dao

import androidx.room.*
import com.registeroffline.core.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE syncStatus = 'Draft' ORDER BY createdAt DESC")
    fun getDraftMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE syncStatus = 'Draft' ORDER BY createdAt ASC")
    suspend fun getDraftMembersOnce(): List<MemberEntity>

    @Query("SELECT * FROM members WHERE syncStatus = 'Synced' ORDER BY createdAt DESC")
    fun getSyncedMembers(): Flow<List<MemberEntity>>

    @Query("SELECT COUNT(*) FROM members WHERE syncStatus = 'Draft'")
    fun getDraftCount(): Flow<Int>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Long): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Query("UPDATE members SET syncStatus = 'Synced' WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Delete
    suspend fun delete(member: MemberEntity)
}
