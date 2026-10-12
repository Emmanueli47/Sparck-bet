package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.BetEntity
import com.example.data.db.FriendlyWagerEntity
import com.example.data.db.UserAccountEntity
import com.example.data.model.BetOption
import com.example.data.model.Market
import com.example.data.model.Match
import com.example.data.model.SlipSelection
import com.example.data.model.SportType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class BettingRepository(
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val betDao = database.betDao()
    private val userDao = database.userDao()
    private val wagerDao = database.friendlyWagerDao()

    val activeBets: Flow<List<BetEntity>> = betDao.getActiveBets()
    val pendingBets: Flow<List<BetEntity>> = betDao.getPendingBets()
    val settledBets: Flow<List<BetEntity>> = betDao.getSettledBets()
    val allBets: Flow<List<BetEntity>> = betDao.getAllBets()
    val userAccount: Flow<UserAccountEntity?> = userDao.getUserAccount()
    val friendlyWagers: Flow<List<FriendlyWagerEntity>> = wagerDao.getAllWagers()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    init {
        // Ensure default user account exists
        scope.launch(Dispatchers.IO) {
            val account = userDao.getUserAccountSync()
            if (account == null) {
                userDao.insertOrUpdate(UserAccountEntity(balance = 1250.00, totalWagered = 180.00, totalWon = 86.60, betsPlaced = 5, betsWon = 2, winStreak = 1, bestStreak = 2))
            }
            initDefaultMatches()
            seedInitialBetsIfEmpty()
            startLiveSimulationTicker()
        }
    }

    private suspend fun seedInitialBetsIfEmpty() {
        val existingBets = betDao.getAllBetsSync()
        if (existingBets.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val seedBets = listOf(
            BetEntity(
                betType = "SINGLE",
                selectionsSummary = "Kansas City Chiefs (1.82) - Moneyline",
                selectionsDetailJson = "m5##Kansas City Chiefs vs Buffalo Bills##KC Chiefs##Moneyline##1.82##FOOTBALL",
                legsCount = 1,
                stake = 50.0,
                totalOdds = 1.82,
                potentialPayout = 91.00,
                boostPercent = 0,
                status = "PENDING",
                cashOutOffer = 46.50,
                placedAt = now - 20 * 60 * 1000L,
                settledAt = 0L,
                payoutReceived = 0.0
            ),
            BetEntity(
                betType = "PARLAY",
                selectionsSummary = "Real Madrid (1.85); Golden State Warriors (1.70)",
                selectionsDetailJson = "m1##Real Madrid vs Manchester City##Real Madrid##Match Winner##1.85##SOCCER||m2##LA Lakers vs Golden State Warriors##GS Warriors##Moneyline##1.70##BASKETBALL",
                legsCount = 2,
                stake = 40.0,
                totalOdds = 3.15,
                potentialPayout = 132.30,
                boostPercent = 5,
                status = "ACTIVE",
                cashOutOffer = 52.80,
                placedAt = now - 45 * 60 * 1000L,
                settledAt = 0L,
                payoutReceived = 0.0
            ),
            BetEntity(
                betType = "SINGLE",
                selectionsSummary = "Arsenal (1.45) - Match Winner",
                selectionsDetailJson = "m3##Arsenal vs Chelsea##Arsenal##Match Winner##1.45##SOCCER",
                legsCount = 1,
                stake = 35.0,
                totalOdds = 1.45,
                potentialPayout = 50.75,
                boostPercent = 0,
                status = "ACTIVE",
                cashOutOffer = 39.10,
                placedAt = now - 30 * 60 * 1000L,
                settledAt = 0L,
                payoutReceived = 0.0
            ),
            BetEntity(
                betType = "SINGLE",
                selectionsSummary = "FaZe Clan (1.62) - CS2 Major Grand Final",
                selectionsDetailJson = "m6##FaZe Clan vs Natus Vincere##FaZe Clan##Match Winner##1.62##ESPORTS",
                legsCount = 1,
                stake = 30.0,
                totalOdds = 1.62,
                potentialPayout = 48.60,
                boostPercent = 0,
                status = "WON",
                cashOutOffer = 0.0,
                placedAt = now - 120 * 60 * 1000L,
                settledAt = now - 25 * 60 * 1000L,
                payoutReceived = 48.60
            ),
            BetEntity(
                betType = "SINGLE",
                selectionsSummary = "Carlos Alcaraz (1.75) - ATP Masters 1000",
                selectionsDetailJson = "m4##Carlos Alcaraz vs Jannik Sinner##Carlos Alcaraz##Match Winner##1.75##TENNIS",
                legsCount = 1,
                stake = 25.0,
                totalOdds = 1.75,
                potentialPayout = 43.75,
                boostPercent = 0,
                status = "CASHED_OUT",
                cashOutOffer = 0.0,
                placedAt = now - 180 * 60 * 1000L,
                settledAt = now - 75 * 60 * 1000L,
                payoutReceived = 38.00
            )
        )
        betDao.insertAllBets(seedBets)

        val wagers = wagerDao.getAllWagers().firstOrNull() ?: emptyList()
        if (wagers.isEmpty()) {
            wagerDao.insertWager(
                FriendlyWagerEntity(
                    title = "Champions League Final Score",
                    opponent = "Marcus Vance",
                    category = "Soccer",
                    userPick = "Real Madrid 3-1",
                    opponentPick = "Man City 2-1",
                    stake = 20.0,
                    potentialReward = 40.0,
                    status = "PENDING",
                    createdAt = now - 40 * 60 * 1000L
                )
            )
        }
    }

    private fun initDefaultMatches() {
        val initialMatches = listOf(
            Match(
                id = "m1",
                sport = SportType.SOCCER,
                league = "UEFA Champions League",
                homeTeam = "Real Madrid",
                awayTeam = "Manchester City",
                homeScore = 2,
                awayScore = 1,
                isLive = true,
                matchStatusText = "71'",
                markets = listOf(
                    Market(
                        name = "Match Winner (1X2)",
                        options = listOf(
                            BetOption("m1_opt_1", "Real Madrid", 1.85, "Match Winner"),
                            BetOption("m1_opt_x", "Draw", 3.40, "Match Winner"),
                            BetOption("m1_opt_2", "Man City", 4.20, "Match Winner")
                        )
                    ),
                    Market(
                        name = "Total Goals",
                        options = listOf(
                            BetOption("m1_opt_ov", "Over 3.5 Goals", 2.05, "Total Goals"),
                            BetOption("m1_opt_un", "Under 3.5 Goals", 1.72, "Total Goals")
                        )
                    ),
                    Market(
                        name = "Both Teams To Score",
                        options = listOf(
                            BetOption("m1_opt_btts_y", "BTTS - Yes", 1.25, "Both Teams To Score"),
                            BetOption("m1_opt_btts_n", "BTTS - No", 3.80, "Both Teams To Score")
                        )
                    )
                ),
                recentEvent = "⚽ GOAL! Vinícius Jr (64') - Powerful shot bottom corner!"
            ),
            Match(
                id = "m2",
                sport = SportType.BASKETBALL,
                league = "NBA Regular Season",
                homeTeam = "LA Lakers",
                awayTeam = "Golden State Warriors",
                homeScore = 88,
                awayScore = 91,
                isLive = true,
                matchStatusText = "Q3 03:44",
                markets = listOf(
                    Market(
                        name = "Moneyline",
                        options = listOf(
                            BetOption("m2_opt_1", "LA Lakers", 2.15, "Moneyline"),
                            BetOption("m2_opt_2", "GS Warriors", 1.70, "Moneyline")
                        )
                    ),
                    Market(
                        name = "Spread (-2.5)",
                        options = listOf(
                            BetOption("m2_opt_sp_1", "Lakers +2.5", 1.90, "Spread"),
                            BetOption("m2_opt_sp_2", "Warriors -2.5", 1.90, "Spread")
                        )
                    ),
                    Market(
                        name = "Total Points",
                        options = listOf(
                            BetOption("m2_opt_ov", "Over 224.5", 1.88, "Total Points"),
                            BetOption("m2_opt_un", "Under 224.5", 1.92, "Total Points")
                        )
                    )
                ),
                recentEvent = "🔥 Curry nails deep 3-pointer from 32ft!"
            ),
            Match(
                id = "m3",
                sport = SportType.SOCCER,
                league = "Premier League",
                homeTeam = "Arsenal",
                awayTeam = "Chelsea",
                homeScore = 1,
                awayScore = 0,
                isLive = true,
                matchStatusText = "54'",
                markets = listOf(
                    Market(
                        name = "Match Winner (1X2)",
                        options = listOf(
                            BetOption("m3_opt_1", "Arsenal", 1.45, "Match Winner"),
                            BetOption("m3_opt_x", "Draw", 4.10, "Match Winner"),
                            BetOption("m3_opt_2", "Chelsea", 6.50, "Match Winner")
                        )
                    ),
                    Market(
                        name = "Total Goals",
                        options = listOf(
                            BetOption("m3_opt_ov", "Over 2.5 Goals", 1.80, "Total Goals"),
                            BetOption("m3_opt_un", "Under 2.5 Goals", 2.00, "Total Goals")
                        )
                    )
                ),
                recentEvent = "🧤 Raya fingertip save on Palmer strike"
            ),
            Match(
                id = "m4",
                sport = SportType.TENNIS,
                league = "ATP Masters 1000",
                homeTeam = "Carlos Alcaraz",
                awayTeam = "Jannik Sinner",
                homeScore = 1,
                awayScore = 1,
                isLive = true,
                matchStatusText = "Set 3 (4-3)",
                markets = listOf(
                    Market(
                        name = "Match Winner",
                        options = listOf(
                            BetOption("m4_opt_1", "Carlos Alcaraz", 1.75, "Match Winner"),
                            BetOption("m4_opt_2", "Jannik Sinner", 2.05, "Match Winner")
                        )
                    ),
                    Market(
                        name = "Total Games",
                        options = listOf(
                            BetOption("m4_opt_ov", "Over 23.5 Games", 1.85, "Total Games"),
                            BetOption("m4_opt_un", "Under 23.5 Games", 1.95, "Total Games")
                        )
                    )
                ),
                recentEvent = "🎾 Break point converted by Alcaraz!"
            ),
            Match(
                id = "m5",
                sport = SportType.FOOTBALL,
                league = "NFL Championship",
                homeTeam = "Kansas City Chiefs",
                awayTeam = "Buffalo Bills",
                homeScore = 0,
                awayScore = 0,
                isLive = false,
                matchStatusText = "Today 20:30",
                markets = listOf(
                    Market(
                        name = "Moneyline",
                        options = listOf(
                            BetOption("m5_opt_1", "KC Chiefs", 1.82, "Moneyline"),
                            BetOption("m5_opt_2", "Buffalo Bills", 2.02, "Moneyline")
                        )
                    ),
                    Market(
                        name = "Spread (-1.5)",
                        options = listOf(
                            BetOption("m5_opt_sp_1", "Chiefs -1.5", 1.91, "Spread"),
                            BetOption("m5_opt_sp_2", "Bills +1.5", 1.89, "Spread")
                        )
                    ),
                    Market(
                        name = "Total Points",
                        options = listOf(
                            BetOption("m5_opt_ov", "Over 48.5", 1.90, "Total Points"),
                            BetOption("m5_opt_un", "Under 48.5", 1.90, "Total Points")
                        )
                    )
                ),
                recentEvent = null
            ),
            Match(
                id = "m6",
                sport = SportType.ESPORTS,
                league = "CS2 Major Grand Final",
                homeTeam = "FaZe Clan",
                awayTeam = "Natus Vincere",
                homeScore = 1,
                awayScore = 0,
                isLive = true,
                matchStatusText = "Map 2 (12-10)",
                markets = listOf(
                    Market(
                        name = "Match Winner",
                        options = listOf(
                            BetOption("m6_opt_1", "FaZe Clan", 1.62, "Match Winner"),
                            BetOption("m6_opt_2", "NaVi", 2.25, "Match Winner")
                        )
                    ),
                    Market(
                        name = "Map 2 Winner",
                        options = listOf(
                            BetOption("m6_opt_m2_1", "FaZe Clan", 1.48, "Map 2"),
                            BetOption("m6_opt_m2_2", "NaVi", 2.60, "Map 2")
                        )
                    )
                ),
                recentEvent = "💥 1v3 Clutch defuse by broky!"
            ),
            Match(
                id = "m7",
                sport = SportType.SOCCER,
                league = "La Liga",
                homeTeam = "Barcelona",
                awayTeam = "Atletico Madrid",
                homeScore = 0,
                awayScore = 0,
                isLive = false,
                matchStatusText = "Tomorrow 21:00",
                markets = listOf(
                    Market(
                        name = "Match Winner (1X2)",
                        options = listOf(
                            BetOption("m7_opt_1", "Barcelona", 1.95, "Match Winner"),
                            BetOption("m7_opt_x", "Draw", 3.50, "Match Winner"),
                            BetOption("m7_opt_2", "Atletico", 3.90, "Match Winner")
                        )
                    )
                ),
                recentEvent = null
            )
        )
        _matches.value = initialMatches
    }

    private fun startLiveSimulationTicker() {
        scope.launch(Dispatchers.IO) {
            while (true) {
                delay(7000) // Ticks every 7 seconds for dynamic feeling
                simulateFluctuations()
            }
        }
    }

    private suspend fun simulateFluctuations() {
        val current = _matches.value
        val updated = current.map { match ->
            if (match.isLive) {
                val advanceScore = Random.nextInt(100) < 18
                var hScore = match.homeScore
                var aScore = match.awayScore
                var event = match.recentEvent

                if (advanceScore) {
                    if (Random.nextBoolean()) {
                        hScore += if (match.sport == SportType.BASKETBALL) Random.nextInt(2, 4) else 1
                        event = "⚡ ${match.homeTeam} scores! ($hScore-$aScore)"
                    } else {
                        aScore += if (match.sport == SportType.BASKETBALL) Random.nextInt(2, 4) else 1
                        event = "⚡ ${match.awayTeam} strikes back! ($hScore-$aScore)"
                    }
                }

                // Micro fluctuations on odds (within 0.03 range)
                val updatedMarkets = match.markets.map { market ->
                    market.copy(
                        options = market.options.map { opt ->
                            val delta = ((Random.nextDouble() - 0.5) * 0.06)
                            val newOdds = ((opt.odds + delta) * 100.0).roundToInt() / 100.0
                            opt.copy(odds = newOdds.coerceIn(1.10, 25.0))
                        }
                    )
                }

                match.copy(
                    homeScore = hScore,
                    awayScore = aScore,
                    markets = updatedMarkets,
                    recentEvent = event ?: match.recentEvent
                )
            } else {
                match
            }
        }
        _matches.value = updated
        updateCashOutOffers()
    }

    private suspend fun updateCashOutOffers() {
        val active = betDao.getActiveBets().firstOrNull() ?: return
        for (bet in active) {
            // Dynamic cash-out fluctuation (60% to 110% of stake or higher if winning)
            val randomFactor = 0.85 + (Random.nextDouble() * 0.35)
            val newCashOut = ((bet.potentialPayout * 0.45 * randomFactor * 100).roundToInt() / 100.0)
                .coerceIn(bet.stake * 0.5, bet.potentialPayout * 0.95)
            betDao.updateBet(bet.copy(cashOutOffer = newCashOut))
        }
    }

    suspend fun placeBet(
        selections: List<SlipSelection>,
        stake: Double,
        isParlay: Boolean
    ): Result<Long> {
        val account = userDao.getUserAccountSync() ?: UserAccountEntity()
        if (account.balance < stake) {
            return Result.failure(Exception("Insufficient virtual bankroll!"))
        }

        val totalOdds = if (isParlay) {
            val raw = selections.fold(1.0) { acc, s -> acc * s.odds }
            (raw * 100.0).roundToInt() / 100.0
        } else {
            selections.firstOrNull()?.odds ?: 1.0
        }

        val boostPercent = if (isParlay && selections.size >= 2) {
            when (selections.size) {
                2 -> 5
                3 -> 10
                4 -> 18
                else -> 25
            }
        } else 0

        val boostMultiplier = 1.0 + (boostPercent / 100.0)
        val rawPayout = stake * totalOdds * boostMultiplier
        val potentialPayout = (rawPayout * 100.0).roundToInt() / 100.0

        val summary = selections.joinToString("; ") { "${it.pickName} (${it.odds}) - ${it.matchTitle}" }
        val detailJson = selections.joinToString("||") {
            "${it.matchId}##${it.matchTitle}##${it.pickName}##${it.marketName}##${it.odds}##${it.sport.name}"
        }

        val initialCashOut = (stake * 0.88 * 100.0).roundToInt() / 100.0

        val hasNonLiveMatch = selections.any { sel ->
            val match = _matches.value.find { it.id == sel.matchId }
            match != null && !match.isLive
        }
        val initialStatus = if (hasNonLiveMatch) "PENDING" else "ACTIVE"

        val newBet = BetEntity(
            betType = if (isParlay) "PARLAY" else "SINGLE",
            selectionsSummary = summary,
            selectionsDetailJson = detailJson,
            legsCount = selections.size,
            stake = stake,
            totalOdds = totalOdds,
            potentialPayout = potentialPayout,
            boostPercent = boostPercent,
            status = initialStatus,
            cashOutOffer = initialCashOut,
            placedAt = System.currentTimeMillis()
        )

        val betId = betDao.insertBet(newBet)

        // Deduct balance and update stats
        val updatedAccount = account.copy(
            balance = (account.balance - stake).coerceAtLeast(0.0),
            totalWagered = account.totalWagered + stake,
            betsPlaced = account.betsPlaced + 1
        )
        userDao.insertOrUpdate(updatedAccount)

        return Result.success(betId)
    }

    suspend fun cashOutBet(betId: Long): Boolean {
        val bet = betDao.getBetById(betId) ?: return false
        if (bet.status != "ACTIVE") return false

        val cashOutAmount = bet.cashOutOffer
        val account = userDao.getUserAccountSync() ?: UserAccountEntity()

        val updatedBet = bet.copy(
            status = "CASHED_OUT",
            settledAt = System.currentTimeMillis(),
            payoutReceived = cashOutAmount
        )
        betDao.updateBet(updatedBet)

        val updatedAccount = account.copy(
            balance = account.balance + cashOutAmount,
            totalWon = account.totalWon + cashOutAmount,
            betsWon = account.betsWon + 1
        )
        userDao.insertOrUpdate(updatedAccount)
        return true
    }

    suspend fun simulateAndSettleAllMatches(): Int {
        val active = betDao.getActiveBets().firstOrNull() ?: emptyList()
        if (active.isEmpty()) return 0

        var settledCount = 0
        val account = userDao.getUserAccountSync() ?: UserAccountEntity()
        var currentBalance = account.balance
        var totalWonAcc = account.totalWon
        var betsWonAcc = account.betsWon
        var currentStreak = account.winStreak
        var bestStreak = account.bestStreak

        for (bet in active) {
            // High thrill: 60% win rate for simulated entertainment
            val isWon = Random.nextInt(100) < 62
            val finalPayout = if (isWon) bet.potentialPayout else 0.0

            val updatedBet = bet.copy(
                status = if (isWon) "WON" else "LOST",
                settledAt = System.currentTimeMillis(),
                payoutReceived = finalPayout
            )
            betDao.updateBet(updatedBet)
            settledCount++

            if (isWon) {
                currentBalance += finalPayout
                totalWonAcc += finalPayout
                betsWonAcc += 1
                currentStreak += 1
                if (currentStreak > bestStreak) {
                    bestStreak = currentStreak
                }
            } else {
                currentStreak = 0
            }
        }

        userDao.insertOrUpdate(
            account.copy(
                balance = currentBalance,
                totalWon = totalWonAcc,
                betsWon = betsWonAcc,
                winStreak = currentStreak,
                bestStreak = bestStreak
            )
        )
        return settledCount
    }

    suspend fun claimFreeDailyBonus(): Boolean {
        val account = userDao.getUserAccountSync() ?: UserAccountEntity()
        val bonus = 500.00
        val updated = account.copy(
            balance = account.balance + bonus,
            lastBonusClaim = System.currentTimeMillis()
        )
        userDao.insertOrUpdate(updated)
        return true
    }

    suspend fun addFriendlyWager(
        title: String,
        opponent: String,
        category: String,
        userPick: String,
        opponentPick: String,
        stake: Double
    ): Long {
        val account = userDao.getUserAccountSync() ?: UserAccountEntity()
        val potentialReward = stake * 2.0 // Even money friendly bet
        val wager = FriendlyWagerEntity(
            title = title,
            opponent = opponent,
            category = category,
            userPick = userPick,
            opponentPick = opponentPick,
            stake = stake,
            potentialReward = potentialReward,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        val id = wagerDao.insertWager(wager)

        userDao.insertOrUpdate(
            account.copy(
                balance = (account.balance - stake).coerceAtLeast(0.0),
                totalWagered = account.totalWagered + stake,
                betsPlaced = account.betsPlaced + 1
            )
        )
        return id
    }

    suspend fun resolveFriendlyWager(wager: FriendlyWagerEntity, userWon: Boolean) {
        val account = userDao.getUserAccountSync() ?: UserAccountEntity()
        val newStatus = if (userWon) "WON" else "LOST"
        wagerDao.updateWager(
            wager.copy(
                status = newStatus,
                settledAt = System.currentTimeMillis()
            )
        )

        if (userWon) {
            userDao.insertOrUpdate(
                account.copy(
                    balance = account.balance + wager.potentialReward,
                    totalWon = account.totalWon + wager.potentialReward,
                    betsWon = account.betsWon + 1,
                    winStreak = account.winStreak + 1
                )
            )
        }
    }

    suspend fun kickoffPendingMatchesAndBets(): Int {
        val current = _matches.value
        val updatedMatches = current.map { match ->
            if (!match.isLive) {
                match.copy(
                    isLive = true,
                    matchStatusText = "1st Half (05:00)",
                    recentEvent = "🔥 Match kicked off and is now live in-play!"
                )
            } else match
        }
        _matches.value = updatedMatches

        val pending = betDao.getPendingBets().firstOrNull() ?: emptyList()
        for (bet in pending) {
            betDao.updateBet(bet.copy(status = "ACTIVE"))
        }
        return pending.size
    }

    suspend fun settleSingleBet(betId: Long, won: Boolean): Boolean {
        val bet = betDao.getBetById(betId) ?: return false
        val payout = if (won) bet.potentialPayout else 0.0
        val updated = bet.copy(
            status = if (won) "WON" else "LOST",
            settledAt = System.currentTimeMillis(),
            payoutReceived = payout
        )
        betDao.updateBet(updated)

        val account = userDao.getUserAccountSync() ?: UserAccountEntity()
        val currentWinStreak = if (won) account.winStreak + 1 else 0
        val newBestStreak = if (currentWinStreak > account.bestStreak) currentWinStreak else account.bestStreak
        val updatedAccount = if (won) {
            account.copy(
                balance = account.balance + payout,
                totalWon = account.totalWon + payout,
                betsWon = account.betsWon + 1,
                winStreak = currentWinStreak,
                bestStreak = newBestStreak
            )
        } else {
            account.copy(winStreak = 0)
        }
        userDao.insertOrUpdate(updatedAccount)
        return true
    }

    suspend fun getUserAccountSync(): UserAccountEntity? = userDao.getUserAccountSync()

    suspend fun updateUserAccountFromCloud(account: UserAccountEntity) {
        userDao.insertOrUpdate(account)
    }

    suspend fun syncBetsFromCloud(bets: List<BetEntity>) {
        betDao.insertAllBets(bets)
    }

    suspend fun getBetById(id: Long): BetEntity? = betDao.getBetById(id)
}
