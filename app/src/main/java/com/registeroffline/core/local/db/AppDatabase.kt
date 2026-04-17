package com.registeroffline.core.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.registeroffline.core.local.dao.MemberDao
import com.registeroffline.core.local.entity.MemberEntity

@Database(entities = [MemberEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
}
