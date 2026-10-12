package com.example.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserAccount(
    @DocumentId val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val balance: Double = 1000.0,
    val totalWagered: Double = 0.0,
    val totalWon: Double = 0.0,
    val betsPlaced: Int = 0,
    val betsWon: Int = 0,
    val winStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastBonusClaim: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreBet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val betType: String = "SINGLE", // "SINGLE", "PARLAY"
    val selectionsSummary: String = "",
    val selectionsDetailJson: String = "",
    val legsCount: Int = 1,
    val stake: Double = 0.0,
    val totalOdds: Double = 1.0,
    val potentialPayout: Double = 0.0,
    val boostPercent: Int = 0,
    val status: String = "ACTIVE", // "PENDING", "ACTIVE", "WON", "LOST", "CASHED_OUT", "RESOLVED"
    val cashOutOffer: Double = 0.0,
    val placedAtTimestamp: Long = 0L,
    val settledAt: Long = 0L,
    val payoutReceived: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreFriendlyWager(
    @DocumentId val id: String = "",
    val creatorId: String = "",
    val title: String = "",
    val opponent: String = "",
    val category: String = "",
    val userPick: String = "",
    val opponentPick: String = "",
    val stake: Double = 0.0,
    val potentialReward: Double = 0.0,
    val status: String = "PENDING", // "PENDING", "WON", "LOST"
    val createdAtTimestamp: Long = 0L,
    val settledAt: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)
