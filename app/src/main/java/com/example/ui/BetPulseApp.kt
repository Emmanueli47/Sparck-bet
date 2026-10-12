package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BetSlipDrawer
import com.example.ui.components.MatchDetailDialog
import com.example.ui.components.TopBarHeader
import com.example.ui.components.UserProfileDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CreateWagerDialog
import com.example.ui.screens.MyBetsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.SportsbookScreen
import com.example.ui.theme.BorderStroke
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BettingViewModel
import kotlinx.coroutines.launch

@Composable
fun BetPulseApp(
    viewModel: BettingViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthenticating by viewModel.isAuthenticating.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val matches by viewModel.filteredMatches.collectAsStateWithLifecycle()
    val selectedSport by viewModel.selectedSport.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeBets by viewModel.activeBets.collectAsStateWithLifecycle()
    val pendingBets by viewModel.pendingBets.collectAsStateWithLifecycle()
    val settledBets by viewModel.settledBets.collectAsStateWithLifecycle()
    val allBets by viewModel.allBets.collectAsStateWithLifecycle()
    val friendlyWagers by viewModel.friendlyWagers.collectAsStateWithLifecycle()
    val dashboardFilter by viewModel.dashboardFilter.collectAsStateWithLifecycle()
    val dashboardSearch by viewModel.dashboardSearch.collectAsStateWithLifecycle()
    val betSlipSelections by viewModel.betSlipSelections.collectAsStateWithLifecycle()
    val isSlipExpanded by viewModel.isSlipExpanded.collectAsStateWithLifecycle()
    val isParlayMode by viewModel.isParlayMode.collectAsStateWithLifecycle()
    val currentStake by viewModel.currentStake.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val selectedMatchDetail by viewModel.selectedMatchDetail.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) }
    var showCreateWagerDialog by remember { mutableStateOf(false) }
    var showUserProfileDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissMessage()
        }
    }

    // Auth Gate: unauthenticated users see the Google Sign-In Screen
    if (currentUser == null) {
        SignInScreen(
            isLoading = isAuthenticating,
            errorMessage = authError,
            onSignInClick = { viewModel.signInWithGoogle() },
            modifier = modifier.statusBarsPadding()
        )
        return
    }

    BackHandler(enabled = isSlipExpanded || currentTab != 0) {
        if (isSlipExpanded) {
            viewModel.setSlipExpanded(false)
        } else if (currentTab != 0) {
            currentTab = 0
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBarHeader(
                balance = userAccount?.balance ?: 1000.00,
                onClaimBonus = { viewModel.claimDailyBonus() },
                userDisplayName = currentUser?.displayName,
                userPhotoUrl = currentUser?.photoUrl?.toString(),
                onSignOut = { viewModel.signOut() },
                onOpenProfile = { showUserProfileDialog = true }
            )
        },
        bottomBar = {
            Column {
                // Persistent Bet Slip above bottom navigation if selections exist
                if (betSlipSelections.isNotEmpty()) {
                    BetSlipDrawer(
                        selections = betSlipSelections,
                        isExpanded = isSlipExpanded,
                        onExpandChange = { viewModel.setSlipExpanded(it) },
                        onRemoveSelection = { viewModel.removeSelection(it) },
                        onClearAll = { viewModel.clearSlip() },
                        isParlay = isParlayMode,
                        onParlayChange = { viewModel.setParlayMode(it) },
                        currentStake = currentStake,
                        onStakeChange = { viewModel.setStake(it) },
                        onAddStake = { viewModel.addStake(it) },
                        onMaxStake = { viewModel.setMaxStake() },
                        totalOdds = viewModel.calculateTotalOdds(),
                        potentialPayout = viewModel.calculatePotentialPayout(),
                        parlayBoost = viewModel.getParlayBoostPercent(),
                        userBalance = userAccount?.balance ?: 0.0,
                        onPlaceBet = { viewModel.placeBet() }
                    )
                }

                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(width = 1.dp, color = BorderStroke)
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = {
                            currentTab = 0
                            if (isSlipExpanded) viewModel.setSlipExpanded(false)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.SportsScore,
                                contentDescription = "Sportsbook"
                            )
                        },
                        label = { Text("Sportsbook", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PulseEmerald,
                            indicatorColor = PulseEmerald,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_sportsbook")
                    )

                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = {
                            currentTab = 1
                            if (isSlipExpanded) viewModel.setSlipExpanded(false)
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (activeBets.isNotEmpty()) {
                                        Badge(containerColor = PulseEmerald, contentColor = Color.Black) {
                                            Text("${activeBets.size}", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Bets Dashboard"
                                )
                            }
                        },
                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PulseEmerald,
                            indicatorColor = PulseEmerald,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_my_bets")
                    )

                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = {
                            currentTab = 2
                            if (isSlipExpanded) viewModel.setSlipExpanded(false)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = "Bankroll"
                            )
                        },
                        label = { Text("Bankroll", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PulseEmerald,
                            indicatorColor = PulseEmerald,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_bankroll")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> SportsbookScreen(
                    matches = matches,
                    selectedSport = selectedSport,
                    onSportSelected = { viewModel.setSelectedSport(it) },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    isOptionSelected = {},
                    isOptionSelectedPredicate = { viewModel.isSelected(it) },
                    onOptionClick = { match, opt -> viewModel.toggleSelection(match, opt) },
                    onOpenDetails = { viewModel.openMatchDetail(it) },
                    onSimulateFastForward = { viewModel.simulateSettleAll() }
                )
                1 -> MyBetsScreen(
                    allBets = allBets,
                    activeBets = activeBets,
                    pendingBets = pendingBets,
                    settledBets = settledBets,
                    friendlyWagers = friendlyWagers,
                    dashboardFilter = dashboardFilter,
                    onFilterChange = { viewModel.setDashboardFilter(it) },
                    dashboardSearch = dashboardSearch,
                    onSearchChange = { viewModel.setDashboardSearch(it) },
                    onCashOut = { viewModel.cashOut(it) },
                    onSimulateFastForward = { viewModel.simulateSettleAll() },
                    onKickoffPending = { viewModel.kickoffPendingMatches() },
                    onSettleSingleBet = { id, won -> viewModel.settleSingleBet(id, won) },
                    onOpenCreateWager = { showCreateWagerDialog = true },
                    onResolveWager = { wager, won -> viewModel.resolveFriendlyWager(wager, won) }
                )
                2 -> AnalyticsScreen(
                    account = userAccount,
                    onClaimDailyBonus = { viewModel.claimDailyBonus() },
                    user = currentUser,
                    onOpenProfile = { showUserProfileDialog = true }
                )
            }

            // Match Detail Deep Markets Dialog
            selectedMatchDetail?.let { match ->
                MatchDetailDialog(
                    match = match,
                    isOptionSelected = { viewModel.isSelected(it) },
                    onOptionClick = { m, opt -> viewModel.toggleSelection(m, opt) },
                    onDismiss = { viewModel.closeMatchDetail() }
                )
            }

            // Friendly Wager Creation Dialog
            if (showCreateWagerDialog) {
                CreateWagerDialog(
                    userBalance = userAccount?.balance ?: 0.0,
                    onDismiss = { showCreateWagerDialog = false },
                    onCreate = { title, opponent, category, userPick, opponentPick, stake ->
                        viewModel.createFriendlyWager(title, opponent, category, userPick, opponentPick, stake)
                        showCreateWagerDialog = false
                    }
                )
            }

            // User Profile & Bet Management Dialog
            if (showUserProfileDialog && currentUser != null) {
                UserProfileDialog(
                    user = currentUser!!,
                    account = userAccount,
                    activeBetsCount = activeBets.size,
                    settledBetsCount = settledBets.size,
                    onDismiss = { showUserProfileDialog = false },
                    onNavigateToDashboard = {
                        currentTab = 1
                        showUserProfileDialog = false
                    },
                    onSignOut = {
                        showUserProfileDialog = false
                        viewModel.signOut()
                    }
                )
            }
        }
    }
}
