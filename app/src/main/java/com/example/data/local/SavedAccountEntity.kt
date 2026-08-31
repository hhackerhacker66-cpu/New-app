package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_accounts")
data class SavedAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerId: String,
    val nickname: String,
    val serverRegion: String,
    val rankBadge: String = "Heroic",
    val level: Int = 50,
    val isFavorite: Boolean = false,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
