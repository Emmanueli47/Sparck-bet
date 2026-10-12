package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.BetEntity
import com.example.data.db.FriendlyWagerEntity
import com.example.data.db.UserAccountEntity
import com.example.data.db.getDashboardStatusCategory
import com.example.data.firebase.AuthManager
import com.example.data.firebase.FirestoreBet
import com.example.data.firebase.FirestoreBettingRepository
import com.example.data.firebase.FirestoreFriendlyWager
import com.example.data.firebase.FirestoreUserAccount
import com.example.data.model.BetOption
import com.example.data.model.DashboardStatusCategory
import com.example.data.model.Match
import com.example.data.model.SlipSelection
import com.example.data.model.SportType
import com.example.data.repository.BettingRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BettingViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)
    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser
    val isAuthenticating: StateFlow<Boolean> = authManager.isAuthenticating
    val authError: StateFlow<String?> = authManager.errorMessage

    private val firestoreRepo = FirestoreBettingRepository(application)

    private val repository = BettingRepository(
        database = AppDatabase.getDatabase(application),
        scope = viewModelScope
    )

    init {
        // Sync user state with Firestore whenever auth state updates
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    syncUserDataWithFirestore(user)
                }
            }
        }
    }

    private fun syncUserDataWithFirestore(user: FirebaseUser) {
        viewModelScope.launch {
            firestoreRepo.observeUserAccount(user.uid).collect { cloudAcc ->
                if (cloudAcc == null) {
                    val local = repository.getUserAccountSync() ?: UserAccountEntity()
                    firestoreRepo.saveUserAccount(
                        FirestoreUserAccount(
                            id = user.uid,
                            displayName = user.displayName ?: "Bettor",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString() ?: "",
                            balance = local.balance,
                            totalWagered = local.totalWagered,
                            totalWon = local.totalWon,
                            betsPlaced = local.betsPlaced,
                            betsWon = local.betsWon,
                            winStreak = local.winStreak,
                            bestStreak = local.bestStreak,
                            lastBonusClaim = local.lastBonusClaim
                        )
                    )
                } else {
                    repository.updateUserAccountFromCloud(
                        UserAccountEntity(
                            id = 1,
                            balance = cloudAcc.balance,
                            totalWagered = cloudAcc.totalWagered,
                            totalWon = cloudAcc.totalWon,
                            betsPlaced = cloudAcc.betsPlaced,
                            betsWon = cloudAcc.betsWon,
                            winStreak = cloudAcc.winStreak,
                            bestStreak = cloudAcc.bestStreak,
                            lastBonusClaim = cloudAcc.lastBonusClaim
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            firestoreRepo.observeBets(user.uid).collect { cloudBets ->
                if (cloudBets.isNotEmpty()) {
                    val entities = cloudBets.map { fb ->
                        BetEntity(
                            id = fb.id.toLongOrNull() ?: System.currentTimeMillis(),
                            betType = fb.betType,
                            selectionsSummary = fb.selectionsSummary,
                            selectionsDetailJson = fb.selectionsDetailJson,
                            legsCount = fb.legsCount,
                            stake = fb.stake,
                            totalOdds = fb.totalOdds,
                            potentialPayout = fb.potentialPayout,
                            boostPercent = fb.boostPercent,
                            status = fb.status,
                            cashOutOffer = fb.cashOutOffer,
                            placedAt = fb.placedAtTimestamp,
                            settledAt = fb.settledAt,
                            payoutReceived = fb.payoutReceived
                        )
                    }
                    repository.syncBetsFromCloud(entities)
                }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authManager.signInWithGoogle()
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    fun clearAuthError() {
        authManager.clearError()
    }

    val userAccount: StateFlow<UserAccountEntity?> = repository.userAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeBets: StateFlow<List<BetEntity>> = repository.activeBets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingBets: StateFlow<List<BetEntity>> = repository.pendingBets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settledBets: StateFlow<List<BetEntity>> = repository.settledBets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBets: StateFlow<List<BetEntity>> = repository.allBets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friendlyWagers: StateFlow<List<FriendlyWagerEntity>> = repository.friendlyWagers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dashboardFilter = MutableStateFlow(DashboardStatusCategory.ALL)
    val dashboardFilter: StateFlow<DashboardStatusCategory> = _dashboardFilter.asStateFlow()

    private val _dashboardSearch = MutableStateFlow("")
    val dashboardSearch: StateFlow<String> = _dashboardSearch.asStateFlow()

    val filteredDashboardBets: StateFlow<List<BetEntity>> = combine(
        allBets,
        _dashboardFilter,
        _dashboardSearch
    ) { bets, filter, query ->
        bets.filter { bet ->
            val matchesFilter = when (filter) {
                DashboardStatusCategory.ALL -> true
                DashboardStatusCategory.PENDING -> bet.getDashboardStatusCategory() == DashboardStatusCategory.PENDING
                DashboardStatusCategory.ACTIVE -> bet.getDashboardStatusCategory() == DashboardStatusCategory.ACTIVE
                DashboardStatusCategory.RESOLVED -> bet.getDashboardStatusCategory() == DashboardStatusCategory.RESOLVED
            }
            val matchesQuery = query.isBlank() ||
                    bet.selectionsSummary.contains(query, ignoreCase = true) ||
                    bet.betType.contains(query, ignoreCase = true) ||
                    bet.status.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSport = MutableStateFlow<SportType?>(null)
    val selectedSport: StateFlow<SportType?> = _selectedSport.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredMatches: StateFlow<List<Match>> = combine(
        repository.matches,
        _selectedSport,
        _searchQuery
    ) { allMatches, sport, query ->
        allMatches.filter { match ->
            val matchesSport = sport == null || match.sport == sport
            val matchesQuery = query.isBlank() ||
                    match.homeTeam.contains(query, ignoreCase = true) ||
                    match.awayTeam.contains(query, ignoreCase = true) ||
                    match.league.contains(query, ignoreCase = true)
            matchesSport && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _betSlipSelections = MutableStateFlow<List<SlipSelection>>(emptyList())
    val betSlipSelections: StateFlow<List<SlipSelection>> = _betSlipSelections.asStateFlow()

    private val _currentStake = MutableStateFlow(25.0)
    val currentStake: StateFlow<Double> = _currentStake.asStateFlow()

    private val _isParlayMode = MutableStateFlow(true)
    val isParlayMode: StateFlow<Boolean> = _isParlayMode.asStateFlow()

    private val _isSlipExpanded = MutableStateFlow(false)
    val isSlipExpanded: StateFlow<Boolean> = _isSlipExpanded.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _selectedMatchDetail = MutableStateFlow<Match?>(null)
    val selectedMatchDetail: StateFlow<Match?> = _selectedMatchDetail.asStateFlow()

    fun setSelectedSport(sport: SportType?) {
        _selectedSport.value = sport
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openMatchDetail(match: Match) {
        _selectedMatchDetail.value = match
    }

    fun closeMatchDetail() {
        _selectedMatchDetail.value = null
    }

    fun toggleSelection(match: Match, option: BetOption) {
        val current = _betSlipSelections.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.selectionId == option.id }

        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            // Remove any other pick from the same match and market to prevent contradiction
            current.removeAll { it.matchId == match.id && it.marketName == option.marketName }
            current.add(
                SlipSelection(
                    selectionId = option.id,
                    matchId = match.id,
                    matchTitle = "${match.homeTeam} vs ${match.awayTeam}",
                    sport = match.sport,
                    marketName = option.marketName,
                    pickName = option.name,
                    odds = option.odds
                )
            )
        }
        _betSlipSelections.value = current
    }

    fun isSelected(optionId: String): Boolean {
        return _betSlipSelections.value.any { it.selectionId == optionId }
    }

    fun removeSelection(selectionId: String) {
        _betSlipSelections.value = _betSlipSelections.value.filterNot { it.selectionId == selectionId }
    }

    fun clearSlip() {
        _betSlipSelections.value = emptyList()
        _isSlipExpanded.value = false
    }

    fun setSlipExpanded(expanded: Boolean) {
        _isSlipExpanded.value = expanded
    }

    fun setParlayMode(isParlay: Boolean) {
        _isParlayMode.value = isParlay
    }

    fun setStake(stake: Double) {
        _currentStake.value = (stake * 100.0).roundToInt() / 100.0
    }

    fun addStake(increment: Double) {
        _currentStake.value = ((_currentStake.value + increment) * 100.0).roundToInt() / 100.0
    }

    fun setMaxStake() {
        val balance = userAccount.value?.balance ?: 0.0
        _currentStake.value = (balance * 100.0).roundToInt() / 100.0
    }

    fun calculateTotalOdds(): Double {
        val selections = _betSlipSelections.value
        if (selections.isEmpty()) return 1.0
        return if (_isParlayMode.value && selections.size > 1) {
            val total = selections.fold(1.0) { acc, s -> acc * s.odds }
            (total * 100.0).roundToInt() / 100.0
        } else {
            selections.first().odds
        }
    }

    fun calculatePotentialPayout(): Double {
        val odds = calculateTotalOdds()
        val stake = _currentStake.value
        val boost = getParlayBoostPercent()
        val boostMultiplier = 1.0 + (boost / 100.0)
        return ((stake * odds * boostMultiplier) * 100.0).roundToInt() / 100.0
    }

    fun getParlayBoostPercent(): Int {
        val count = _betSlipSelections.value.size
        if (!_isParlayMode.value || count < 2) return 0
        return when (count) {
            2 -> 5
            3 -> 10
            4 -> 18
            else -> 25
        }
    }

    fun placeBet() {
        val selections = _betSlipSelections.value
        if (selections.isEmpty()) {
            _uiMessage.value = "Select at least one match outcome to place a bet!"
            return
        }
        val stake = _currentStake.value
        if (stake <= 0) {
            _uiMessage.value = "Please enter a valid stake!"
            return
        }

        val balance = userAccount.value?.balance ?: 0.0
        if (stake > balance) {
            _uiMessage.value = "Insufficient virtual bankroll! Claim daily reload or lower stake."
            return
        }

        viewModelScope.launch {
            val result = repository.placeBet(
                selections = selections,
                stake = stake,
                isParlay = _isParlayMode.value && selections.size > 1
            )
            result.onSuccess { betId ->
                _uiMessage.value = "🎯 Bet placed successfully! Potential win: $${calculatePotentialPayout()}"
                _betSlipSelections.value = emptyList()
                _isSlipExpanded.value = false

                val uid = currentUser.value?.uid
                if (uid != null) {
                    val bet = repository.getBetById(betId)
                    if (bet != null) {
                        firestoreRepo.saveBet(
                            uid,
                            FirestoreBet(
                                id = bet.id.toString(),
                                userId = uid,
                                betType = bet.betType,
                                selectionsSummary = bet.selectionsSummary,
                                selectionsDetailJson = bet.selectionsDetailJson,
                                legsCount = bet.legsCount,
                                stake = bet.stake,
                                totalOdds = bet.totalOdds,
                                potentialPayout = bet.potentialPayout,
                                boostPercent = bet.boostPercent,
                                status = bet.status,
                                cashOutOffer = bet.cashOutOffer,
                                placedAtTimestamp = bet.placedAt,
                                settledAt = bet.settledAt,
                                payoutReceived = bet.payoutReceived
                            )
                        )
                    }
                    val acc = repository.getUserAccountSync()
                    if (acc != null) {
                        firestoreRepo.saveUserAccount(
                            FirestoreUserAccount(
                                id = uid,
                                displayName = currentUser.value?.displayName ?: "Bettor",
                                email = currentUser.value?.email ?: "",
                                photoUrl = currentUser.value?.photoUrl?.toString() ?: "",
                                balance = acc.balance,
                                totalWagered = acc.totalWagered,
                                totalWon = acc.totalWon,
                                betsPlaced = acc.betsPlaced,
                                betsWon = acc.betsWon,
                                winStreak = acc.winStreak,
                                bestStreak = acc.bestStreak,
                                lastBonusClaim = acc.lastBonusClaim
                            )
                        )
                    }
                }
            }.onFailure { err ->
                _uiMessage.value = err.message ?: "Failed to place bet"
            }
        }
    }

    fun cashOut(betId: Long) {
        viewModelScope.launch {
            val success = repository.cashOutBet(betId)
            if (success) {
                _uiMessage.value = "💰 Bet successfully cashed out! Funds added to your bankroll."
                val uid = currentUser.value?.uid
                if (uid != null) {
                    val bet = repository.getBetById(betId)
                    if (bet != null) {
                        firestoreRepo.updateBetStatus(uid, betId.toString(), "CASHED_OUT", bet.payoutReceived, bet.settledAt)
                    }
                    val acc = repository.getUserAccountSync()
                    if (acc != null) {
                        firestoreRepo.saveUserAccount(
                            FirestoreUserAccount(
                                id = uid,
                                displayName = currentUser.value?.displayName ?: "Bettor",
                                email = currentUser.value?.email ?: "",
                                photoUrl = currentUser.value?.photoUrl?.toString() ?: "",
                                balance = acc.balance,
                                totalWagered = acc.totalWagered,
                                totalWon = acc.totalWon,
                                betsPlaced = acc.betsPlaced,
                                betsWon = acc.betsWon,
                                winStreak = acc.winStreak,
                                bestStreak = acc.bestStreak,
                                lastBonusClaim = acc.lastBonusClaim
                            )
                        )
                    }
                }
            } else {
                _uiMessage.value = "Could not cash out this bet."
            }
        }
    }

    private fun syncUserAccountToFirestore(uid: String) {
        viewModelScope.launch {
            val acc = repository.getUserAccountSync() ?: return@launch
            firestoreRepo.saveUserAccount(
                FirestoreUserAccount(
                    id = uid,
                    displayName = currentUser.value?.displayName ?: "Bettor",
                    email = currentUser.value?.email ?: "",
                    photoUrl = currentUser.value?.photoUrl?.toString() ?: "",
                    balance = acc.balance,
                    totalWagered = acc.totalWagered,
                    totalWon = acc.totalWon,
                    betsPlaced = acc.betsPlaced,
                    betsWon = acc.betsWon,
                    winStreak = acc.winStreak,
                    bestStreak = acc.bestStreak,
                    lastBonusClaim = acc.lastBonusClaim
                )
            )
        }
    }

    fun simulateSettleAll() {
        viewModelScope.launch {
            val count = repository.simulateAndSettleAllMatches()
            if (count > 0) {
                _uiMessage.value = "🏁 Simulated $count active match result(s)! Check settled bets."
                val uid = currentUser.value?.uid
                if (uid != null) {
                    val settled = repository.settledBets.firstOrNull() ?: emptyList()
                    for (b in settled) {
                        firestoreRepo.updateBetStatus(uid, b.id.toString(), b.status, b.payoutReceived, b.settledAt)
                    }
                    syncUserAccountToFirestore(uid)
                }
            } else {
                _uiMessage.value = "No active bets to settle right now."
            }
        }
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            repository.claimFreeDailyBonus()
            _uiMessage.value = "🎁 Claimed $500.00 free bonus bankroll reload!"
            val uid = currentUser.value?.uid
            if (uid != null) {
                syncUserAccountToFirestore(uid)
            }
        }
    }

    fun createFriendlyWager(
        title: String,
        opponent: String,
        category: String,
        userPick: String,
        opponentPick: String,
        stake: Double
    ) {
        val balance = userAccount.value?.balance ?: 0.0
        if (stake > balance) {
            _uiMessage.value = "Insufficient balance for this friendly wager!"
            return
        }
        viewModelScope.launch {
            val wagerId = repository.addFriendlyWager(title, opponent, category, userPick, opponentPick, stake)
            _uiMessage.value = "🤝 Friendly wager with $opponent created!"
            val uid = currentUser.value?.uid
            if (uid != null) {
                syncUserAccountToFirestore(uid)
                firestoreRepo.saveFriendlyWager(
                    FirestoreFriendlyWager(
                        id = wagerId.toString(),
                        creatorId = uid,
                        title = title,
                        opponent = opponent,
                        category = category,
                        userPick = userPick,
                        opponentPick = opponentPick,
                        stake = stake,
                        potentialReward = stake * 2.0,
                        status = "PENDING",
                        createdAtTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun resolveFriendlyWager(wager: FriendlyWagerEntity, userWon: Boolean) {
        viewModelScope.launch {
            repository.resolveFriendlyWager(wager, userWon)
            val outcome = if (userWon) "WON! +$${wager.potentialReward}" else "LOST"
            _uiMessage.value = "Wager resolved as $outcome"
            val uid = currentUser.value?.uid
            if (uid != null) {
                syncUserAccountToFirestore(uid)
                firestoreRepo.resolveFriendlyWager(
                    wagerId = wager.id.toString(),
                    newStatus = if (userWon) "WON" else "LOST",
                    settledAt = System.currentTimeMillis()
                )
            }
        }
    }

    fun setDashboardFilter(category: DashboardStatusCategory) {
        _dashboardFilter.value = category
    }

    fun setDashboardSearch(query: String) {
        _dashboardSearch.value = query
    }

    fun kickoffPendingMatches() {
        viewModelScope.launch {
            val count = repository.kickoffPendingMatchesAndBets()
            if (count > 0) {
                _uiMessage.value = "🚀 $count pending bet(s) are now LIVE in-play!"
                val uid = currentUser.value?.uid
                if (uid != null) {
                    val active = repository.activeBets.firstOrNull() ?: emptyList()
                    for (b in active) {
                        firestoreRepo.updateBetStatus(uid, b.id.toString(), b.status, b.payoutReceived, b.settledAt)
                    }
                }
            } else {
                _uiMessage.value = "All matches already in-play or no pending bets."
            }
        }
    }

    fun settleSingleBet(betId: Long, won: Boolean) {
        viewModelScope.launch {
            val success = repository.settleSingleBet(betId, won)
            if (success) {
                val outcome = if (won) "WON! Bankroll updated 🎯" else "Settled as Lost."
                _uiMessage.value = "Bet #$betId resolved as $outcome"
                val uid = currentUser.value?.uid
                if (uid != null) {
                    val bet = repository.getBetById(betId)
                    if (bet != null) {
                        firestoreRepo.updateBetStatus(uid, betId.toString(), bet.status, bet.payoutReceived, bet.settledAt)
                    }
                    syncUserAccountToFirestore(uid)
                }
            }
        }
    }

    fun dismissMessage() {
        _uiMessage.value = null
    }
}
