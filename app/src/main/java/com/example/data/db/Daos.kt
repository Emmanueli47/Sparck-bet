package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BetDao {
    @Query("SELECT * FROM bets ORDER BY placedAt DESC")
    fun getAllBets(): Flow<List<BetEntity>>

    @Query("SELECT * FROM bets ORDER BY placedAt DESC")
    suspend fun getAllBetsSync(): List<BetEntity>

    @Query("SELECT * FROM bets WHERE status = 'ACTIVE' ORDER BY placedAt DESC")
    fun getActiveBets(): Flow<List<BetEntity>>

    @Query("SELECT * FROM bets WHERE status = 'PENDING' ORDER BY placedAt DESC")
    fun getPendingBets(): Flow<List<BetEntity>>

    @Query("SELECT * FROM bets WHERE status IN ('ACTIVE', 'PENDING') ORDER BY placedAt DESC")
    fun getActiveAndPendingBets(): Flow<List<BetEntity>>

    @Query("SELECT * FROM bets WHERE status NOT IN ('ACTIVE', 'PENDING') ORDER BY settledAt DESC, placedAt DESC")
    fun getSettledBets(): Flow<List<BetEntity>>

    @Query("SELECT * FROM bets WHERE id = :id LIMIT 1")
    suspend fun getBetById(id: Long): BetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: BetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBets(bets: List<BetEntity>)

    @Update
    suspend fun updateBet(bet: BetEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    fun getUserAccount(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
    suspend fun getUserAccountSync(): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: UserAccountEntity)
}

@Dao
interface FriendlyWagerDao {
    @Query("SELECT * FROM friendly_wagers ORDER BY createdAt DESC")
    fun getAllWagers(): Flow<List<FriendlyWagerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWager(wager: FriendlyWagerEntity): Long

    @Update
    suspend fun updateWager(wager: FriendlyWagerEntity)

    @Query("DELETE FROM friendly_wagers WHERE id = :id")
    suspend fun deleteWager(id: Long)
}
