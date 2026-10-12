package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreBettingRepo"

class FirestoreBettingRepository(
    private val db: FirebaseFirestore
) {
    // Secondary constructor resolving named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // Observe user profile/bankroll
    fun observeUserAccount(userId: String): Flow<FirestoreUserAccount?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing user account: ${error.message}", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val account = snapshot.toObject(FirestoreUserAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(account)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserAccount(account: FirestoreUserAccount) {
        require(account.id.isNotBlank()) { "User ID must not be blank" }
        val docRef = db.collection("users").document(account.id)
        val data = mapOf(
            "id" to account.id,
            "displayName" to account.displayName,
            "email" to account.email,
            "photoUrl" to account.photoUrl,
            "balance" to account.balance,
            "totalWagered" to account.totalWagered,
            "totalWon" to account.totalWon,
            "betsPlaced" to account.betsPlaced,
            "betsWon" to account.betsWon,
            "winStreak" to account.winStreak,
            "bestStreak" to account.bestStreak,
            "lastBonusClaim" to account.lastBonusClaim,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    // Observe user's bets
    fun observeBets(userId: String): Flow<List<FirestoreBet>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val collection = db.collection("users").document(userId).collection("bets")
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing bets: ${error.message}", error)
                return@addSnapshotListener
            }
            val bets = snapshot?.toObjects(FirestoreBet::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
            trySend(bets.sortedByDescending { it.placedAtTimestamp })
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveBet(userId: String, bet: FirestoreBet) {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        require(bet.id.isNotBlank()) { "Bet ID must not be blank" }
        val docRef = db.collection("users").document(userId).collection("bets").document(bet.id)
        val data = mapOf(
            "id" to bet.id,
            "userId" to userId,
            "betType" to bet.betType,
            "selectionsSummary" to bet.selectionsSummary,
            "selectionsDetailJson" to bet.selectionsDetailJson,
            "legsCount" to bet.legsCount,
            "stake" to bet.stake,
            "totalOdds" to bet.totalOdds,
            "potentialPayout" to bet.potentialPayout,
            "boostPercent" to bet.boostPercent,
            "status" to bet.status,
            "cashOutOffer" to bet.cashOutOffer,
            "placedAtTimestamp" to bet.placedAtTimestamp,
            "settledAt" to bet.settledAt,
            "payoutReceived" to bet.payoutReceived,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
    }

    suspend fun updateBetStatus(userId: String, betId: String, newStatus: String, payout: Double, settledAt: Long) {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        require(betId.isNotBlank()) { "Bet ID must not be blank" }
        val docRef = db.collection("users").document(userId).collection("bets").document(betId)
        docRef.update(
            mapOf(
                "status" to newStatus,
                "payoutReceived" to payout,
                "settledAt" to settledAt
            )
        ).await()
    }

    // Observe friendly wagers
    fun observeFriendlyWagers(): Flow<List<FirestoreFriendlyWager>> = callbackFlow {
        val collection = db.collection("friendly_wagers")
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing friendly wagers: ${error.message}", error)
                return@addSnapshotListener
            }
            val wagers = snapshot?.toObjects(FirestoreFriendlyWager::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
            trySend(wagers.sortedByDescending { it.createdAtTimestamp })
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveFriendlyWager(wager: FirestoreFriendlyWager) {
        require(wager.id.isNotBlank()) { "Wager ID must not be blank" }
        val docRef = db.collection("friendly_wagers").document(wager.id)
        val data = mapOf(
            "id" to wager.id,
            "creatorId" to wager.creatorId,
            "title" to wager.title,
            "opponent" to wager.opponent,
            "category" to wager.category,
            "userPick" to wager.userPick,
            "opponentPick" to wager.opponentPick,
            "stake" to wager.stake,
            "potentialReward" to wager.potentialReward,
            "status" to wager.status,
            "createdAtTimestamp" to wager.createdAtTimestamp,
            "settledAt" to wager.settledAt,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
    }

    suspend fun resolveFriendlyWager(wagerId: String, newStatus: String, settledAt: Long) {
        require(wagerId.isNotBlank()) { "Wager ID must not be blank" }
        val docRef = db.collection("friendly_wagers").document(wagerId)
        docRef.update(
            mapOf(
                "status" to newStatus,
                "settledAt" to settledAt
            )
        ).await()
    }
}
