package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DashboardStatusCategory

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 1000.00,
    val totalWagered: Double = 0.00,
    val totalWon: Double = 0.00,
    val betsPlaced: Int = 0,
    val betsWon: Int = 0,
    val winStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastBonusClaim: Long = 0L
)

@Entity(tableName = "bets")
data class BetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val betType: String, // "SINGLE", "PARLAY"
    val selectionsSummary: String, // e.g. "Real Madrid (2.10), Arsenal Over 2.5 (1.80)"
    val selectionsDetailJson: String, // Pipe or delimited formatted details
    val legsCount: Int,
    val stake: Double,
    val totalOdds: Double,
    val potentialPayout: Double,
    val boostPercent: Int = 0, // e.g. 10 for +10% parlay bonus
    val status: String, // "ACTIVE", "WON", "LOST", "CASHED_OUT"
    val cashOutOffer: Double,
    val placedAt: Long = System.currentTimeMillis(),
    val settledAt: Long = 0L,
    val payoutReceived: Double = 0.0
)

@Entity(tableName = "friendly_wagers")
data class FriendlyWagerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val opponent: String,
    val category: String,
    val userPick: String,
    val opponentPick: String,
    val stake: Double,
    val potentialReward: Double,
    val status: String = "PENDING", // "PENDING", "WON", "LOST"
    val createdAt: Long = System.currentTimeMillis(),
    val settledAt: Long = 0L
)

fun BetEntity.getDashboardStatusCategory(): DashboardStatusCategory {
    return when {
        status.equals("PENDING", ignoreCase = true) -> DashboardStatusCategory.PENDING
        status.equals("ACTIVE", ignoreCase = true) -> DashboardStatusCategory.ACTIVE
        else -> DashboardStatusCategory.RESOLVED
    }
}

fun FriendlyWagerEntity.getDashboardStatusCategory(): DashboardStatusCategory {
    return when {
        status.equals("PENDING", ignoreCase = true) -> DashboardStatusCategory.PENDING
        status.equals("ACTIVE", ignoreCase = true) -> DashboardStatusCategory.ACTIVE
        else -> DashboardStatusCategory.RESOLVED
    }
}
