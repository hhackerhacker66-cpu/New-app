package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TopUpDao {
    // Orders
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderByOrderId(orderId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)

    @Query("DELETE FROM orders")
    suspend fun clearAllOrders()

    // Saved Accounts
    @Query("SELECT * FROM saved_accounts ORDER BY isFavorite DESC, lastUsedTimestamp DESC")
    fun getAllSavedAccounts(): Flow<List<SavedAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAccount(account: SavedAccountEntity): Long

    @Update
    suspend fun updateSavedAccount(account: SavedAccountEntity)

    @Delete
    suspend fun deleteSavedAccount(account: SavedAccountEntity)

    @Query("SELECT * FROM saved_accounts WHERE playerId = :playerId LIMIT 1")
    suspend fun getAccountByPlayerId(playerId: String): SavedAccountEntity?
}
