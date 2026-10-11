package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BetEntity
import com.example.data.db.FriendlyWagerEntity
import com.example.data.model.DashboardStatusCategory
import com.example.ui.theme.BorderStroke
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCrimson
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyBetsScreen(
    activeBets: List<BetEntity>,
    settledBets: List<BetEntity>,
    friendlyWagers: List<FriendlyWagerEntity>,
    onCashOut: (Long) -> Unit,
    onSimulateFastForward: () -> Unit,
    onOpenCreateWager: () -> Unit,
    onResolveWager: (FriendlyWagerEntity, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    allBets: List<BetEntity> = emptyList(),
    pendingBets: List<BetEntity> = emptyList(),
    dashboardFilter: DashboardStatusCategory = DashboardStatusCategory.ALL,
    onFilterChange: (DashboardStatusCategory) -> Unit = {},
    dashboardSearch: String = "",
    onSearchChange: (String) -> Unit = {},
    onKickoffPending: () -> Unit = {},
    onSettleSingleBet: (Long, Boolean) -> Unit = { _, _ -> }
) {
    val effectiveAllBets = if (allBets.isNotEmpty()) allBets else (activeBets + pendingBets + settledBets)
    val effectivePendingBets = if (pendingBets.isNotEmpty()) pendingBets else effectiveAllBets.filter { it.status.equals("PENDING", ignoreCase = true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Dashboard",
        "Active (${activeBets.size})",
        "Pending (${effectivePendingBets.size})",
        "Settled (${settledBets.size})",
        "Friendly (${friendlyWagers.size})"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("my_bets_screen")
    ) {
        // Scrollable Tab Row for smooth navigation across all status categories
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = PulseEmerald,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PulseEmerald,
                    height = 3.dp
                )
            },
            divider = { HorizontalDivider(color = BorderStroke) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTab == index) PulseEmerald else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("my_bets_tab_$index")
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> BetsDashboardScreen(
                    allBets = effectiveAllBets,
                    friendlyWagers = friendlyWagers,
                    selectedFilter = dashboardFilter,
                    onFilterChange = onFilterChange,
                    searchQuery = dashboardSearch,
                    onSearchChange = onSearchChange,
                    onCashOut = onCashOut,
                    onSimulateFastForward = onSimulateFastForward,
                    onKickoffPending = onKickoffPending,
                    onSettleSingleBet = onSettleSingleBet,
                    onOpenCreateWager = onOpenCreateWager,
                    onResolveWager = onResolveWager
                )
                1 -> ActiveBetsTab(
                    activeBets = activeBets,
                    onCashOut = onCashOut,
                    onSimulateFastForward = onSimulateFastForward
                )
                2 -> PendingBetsTab(
                    pendingBets = effectivePendingBets,
                    onKickoffPending = onKickoffPending
                )
                3 -> SettledBetsTab(settledBets = settledBets)
                4 -> FriendlyWagersTab(
                    wagers = friendlyWagers,
                    onOpenCreate = onOpenCreateWager,
                    onResolve = onResolveWager
                )
            }
        }
    }
}

@Composable
private fun PendingBetsTab(
    pendingBets: List<BetEntity>,
    onKickoffPending: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pending_bets_tab"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pendingBets.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevatedDark)
                        .border(1.dp, PulseAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pending Match Kickoff",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Upcoming games waiting to start. Kickoff now to move to in-play!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onKickoffPending,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PulseAmber, contentColor = Color.Black),
                        modifier = Modifier.testTag("kickoff_all_pending_button")
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = "Kickoff", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kickoff All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (pendingBets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "No pending bets",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No pending bets right now",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Bets placed on scheduled upcoming matches will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(pendingBets, key = { it.id }) { bet ->
                DashboardBetCard(
                    bet = bet,
                    onCashOut = {},
                    onKickoffPending = onKickoffPending,
                    onSettleSingle = { _, _ -> }
                )
            }
        }
    }
}

@Composable
private fun ActiveBetsTab(
    activeBets: List<BetEntity>,
    onCashOut: (Long) -> Unit,
    onSimulateFastForward: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (activeBets.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevatedDark)
                        .border(1.dp, BorderStroke, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Match Fast-Forward",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Simulate all live games and settle active bets instantly!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSimulateFastForward,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PulseCyan, contentColor = Color.Black),
                        modifier = Modifier.testTag("simulate_settle_button")
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = "Simulate", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Settle Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (activeBets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "No active bets",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active bets right now",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Browse the Sportsbook and place a bet to see it live here!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(activeBets, key = { it.id }) { bet ->
                ActiveBetCard(bet = bet, onCashOut = onCashOut)
            }
        }
    }
}

@Composable
private fun ActiveBetCard(
    bet: BetEntity,
    onCashOut: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_bet_card_${bet.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PulseCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (bet.betType == "PARLAY") "PARLAY (${bet.legsCount} LEGS)" else "SINGLE BET",
                            color = PulseCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (bet.boostPercent > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+${bet.boostPercent}% BOOST",
                            color = PulseAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Active",
                        tint = PulseEmerald,
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE IN-PLAY",
                        color = PulseEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selections Summary
            Text(
                text = bet.selectionsSummary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderStroke)
            Spacer(modifier = Modifier.height(10.dp))

            // Financials
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Stake", color = TextSecondary, fontSize = 11.sp)
                    Text(String.format("$%,.2f", bet.stake), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Odds", color = TextSecondary, fontSize = 11.sp)
                    Text(String.format("%.2f", bet.totalOdds), color = PulseCyan, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Potential Payout", color = TextSecondary, fontSize = 11.sp)
                    Text(String.format("$%,.2f", bet.potentialPayout), color = PulseEmerald, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cash Out Button
            Button(
                onClick = { onCashOut(bet.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cashout_button_${bet.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseAmber,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Payments, contentDescription = "Cash out", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CASH OUT NOW  •  ${String.format("$%,.2f", bet.cashOutOffer)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SettledBetsTab(settledBets: List<BetEntity>) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (settledBets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No history",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No settled bets yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Completed bets will show their final outcome and profit here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(settledBets, key = { it.id }) { bet ->
                val isWon = bet.status == "WON"
                val isCashedOut = bet.status == "CASHED_OUT"
                val statusColor = when {
                    isWon -> PulseEmerald
                    isCashedOut -> PulseAmber
                    else -> PulseCrimson
                }
                val profit = bet.payoutReceived - bet.stake

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${bet.betType} (${bet.legsCount} Legs)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusColor.copy(alpha = 0.2f))
                                    .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = bet.status,
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bet.selectionsSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderStroke)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stake: ${String.format("$%,.2f", bet.stake)}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )

                            Text(
                                text = if (profit >= 0) "+${String.format("$%,.2f", profit)}" else String.format("$%,.2f", profit),
                                color = if (profit >= 0) PulseEmerald else PulseCrimson,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendlyWagersTab(
    wagers: List<FriendlyWagerEntity>,
    onOpenCreate: () -> Unit,
    onResolve: (FriendlyWagerEntity, Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevatedDark)
                        .border(1.dp, BorderStroke, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Friendly Wagers",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Create side bets with friends on personal games and challenges!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onOpenCreate,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald, contentColor = Color.Black),
                        modifier = Modifier.testTag("create_friendly_wager_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Wager", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            if (wagers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = "Friendly bets",
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No friendly wagers yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Hit 'New Wager' above to create a side bet with a buddy!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(wagers, key = { it.id }) { wager ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = wager.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = wager.status,
                                    color = if (wager.status == "WON") PulseEmerald else if (wager.status == "LOST") PulseCrimson else PulseAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Opponent: ${wager.opponent}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceVariantDark)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Your Pick", color = PulseEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(wager.userPick, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${wager.opponent}'s Pick", color = PulseCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(wager.opponentPick, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Stake: ${String.format("$%,.2f", wager.stake)}  •  Reward: ${String.format("$%,.2f", wager.potentialReward)}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )

                                if (wager.status == "PENDING") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { onResolve(wager, true) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald, contentColor = Color.Black),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("I Won", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { onResolve(wager, false) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Lost", fontSize = 11.sp, color = PulseCrimson)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
