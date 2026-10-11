package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BetEntity
import com.example.data.db.FriendlyWagerEntity
import com.example.data.db.getDashboardStatusCategory
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

/**
 * Dashboard list using LazyColumn to display current active bets with status indicators
 * for 'Pending', 'Active', and 'Resolved'.
 */
@Composable
fun BetsDashboardScreen(
    allBets: List<BetEntity>,
    friendlyWagers: List<FriendlyWagerEntity> = emptyList(),
    selectedFilter: DashboardStatusCategory = DashboardStatusCategory.ALL,
    onFilterChange: (DashboardStatusCategory) -> Unit = {},
    searchQuery: String = "",
    onSearchChange: (String) -> Unit = {},
    onCashOut: (Long) -> Unit = {},
    onSimulateFastForward: () -> Unit = {},
    onKickoffPending: () -> Unit = {},
    onSettleSingleBet: (Long, Boolean) -> Unit = { _, _ -> },
    onOpenCreateWager: () -> Unit = {},
    onResolveWager: (FriendlyWagerEntity, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var internalFilter by remember { mutableStateOf(selectedFilter) }
    var internalSearch by remember { mutableStateOf(searchQuery) }

    // Counts for stats
    val pendingBets = allBets.filter { it.getDashboardStatusCategory() == DashboardStatusCategory.PENDING }
    val activeBets = allBets.filter { it.getDashboardStatusCategory() == DashboardStatusCategory.ACTIVE }
    val resolvedBets = allBets.filter { it.getDashboardStatusCategory() == DashboardStatusCategory.RESOLVED }

    val activeStake = (activeBets + pendingBets).sumOf { it.stake }
    val potentialReturns = (activeBets + pendingBets).sumOf { it.potentialPayout }

    // Filter bets by status and search
    val displayedBets = allBets.filter { bet ->
        val matchesCategory = when (internalFilter) {
            DashboardStatusCategory.ALL -> true
            DashboardStatusCategory.PENDING -> bet.getDashboardStatusCategory() == DashboardStatusCategory.PENDING
            DashboardStatusCategory.ACTIVE -> bet.getDashboardStatusCategory() == DashboardStatusCategory.ACTIVE
            DashboardStatusCategory.RESOLVED -> bet.getDashboardStatusCategory() == DashboardStatusCategory.RESOLVED
        }
        val matchesQuery = internalSearch.isBlank() ||
                bet.selectionsSummary.contains(internalSearch, ignoreCase = true) ||
                bet.betType.contains(internalSearch, ignoreCase = true) ||
                bet.status.contains(internalSearch, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("bets_dashboard_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Dashboard Metrics Overview Header
        item(key = "dashboard_header_kpis") {
            DashboardHeaderKpiCard(
                totalCount = allBets.size,
                activeCount = activeBets.size,
                pendingCount = pendingBets.size,
                resolvedCount = resolvedBets.size,
                totalAtRisk = activeStake,
                potentialReturns = potentialReturns
            )
        }

        // 2. Action Simulation & Kickoff Quick Control
        item(key = "dashboard_actions_bar") {
            DashboardActionControls(
                hasPending = pendingBets.isNotEmpty(),
                hasActive = activeBets.isNotEmpty(),
                onKickoffPending = {
                    onKickoffPending()
                },
                onSimulateFastForward = onSimulateFastForward,
                onNewWager = onOpenCreateWager
            )
        }

        // 3. Status Filter Chips Row
        item(key = "dashboard_filter_chips") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FILTER BY STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(
                        label = "All Bets (${allBets.size})",
                        selected = internalFilter == DashboardStatusCategory.ALL,
                        selectedColor = PulseCyan,
                        onClick = {
                            internalFilter = DashboardStatusCategory.ALL
                            onFilterChange(DashboardStatusCategory.ALL)
                        },
                        testTag = "filter_chip_all"
                    )

                    StatusChip(
                        label = "Active (${activeBets.size})",
                        selected = internalFilter == DashboardStatusCategory.ACTIVE,
                        selectedColor = PulseEmerald,
                        icon = Icons.Default.FiberManualRecord,
                        onClick = {
                            internalFilter = DashboardStatusCategory.ACTIVE
                            onFilterChange(DashboardStatusCategory.ACTIVE)
                        },
                        testTag = "filter_chip_active"
                    )

                    StatusChip(
                        label = "Pending (${pendingBets.size})",
                        selected = internalFilter == DashboardStatusCategory.PENDING,
                        selectedColor = PulseAmber,
                        icon = Icons.Default.HourglassEmpty,
                        onClick = {
                            internalFilter = DashboardStatusCategory.PENDING
                            onFilterChange(DashboardStatusCategory.PENDING)
                        },
                        testTag = "filter_chip_pending"
                    )

                    StatusChip(
                        label = "Resolved (${resolvedBets.size})",
                        selected = internalFilter == DashboardStatusCategory.RESOLVED,
                        selectedColor = PulseCyan,
                        icon = Icons.Default.CheckCircle,
                        onClick = {
                            internalFilter = DashboardStatusCategory.RESOLVED
                            onFilterChange(DashboardStatusCategory.RESOLVED)
                        },
                        testTag = "filter_chip_resolved"
                    )
                }
            }
        }

        // 4. Search Bar
        item(key = "dashboard_search_bar") {
            OutlinedTextField(
                value = internalSearch,
                onValueChange = {
                    internalSearch = it
                    onSearchChange(it)
                },
                placeholder = { Text("Search by team, pick, or status...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search bets",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (internalSearch.isNotEmpty()) {
                        IconButton(onClick = {
                            internalSearch = ""
                            onSearchChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PulseEmerald,
                    unfocusedBorderColor = BorderStroke,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // 5. Section Header with Active Count
        item(key = "dashboard_section_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (internalFilter) {
                        DashboardStatusCategory.ALL -> "ALL BETS (${displayedBets.size})"
                        DashboardStatusCategory.PENDING -> "PENDING BETS (${displayedBets.size})"
                        DashboardStatusCategory.ACTIVE -> "CURRENT ACTIVE BETS (${displayedBets.size})"
                        DashboardStatusCategory.RESOLVED -> "RESOLVED BETS (${displayedBets.size})"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimary
                )

                Text(
                    text = "${displayedBets.size} wagers displayed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // 6. Empty State if no bets match
        if (displayedBets.isEmpty()) {
            item(key = "dashboard_empty_state") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .testTag("dashboard_empty_state"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (internalFilter) {
                                DashboardStatusCategory.PENDING -> Icons.Default.HourglassEmpty
                                DashboardStatusCategory.ACTIVE -> Icons.Default.FiberManualRecord
                                DashboardStatusCategory.RESOLVED -> Icons.Default.CheckCircle
                                else -> Icons.Default.ReceiptLong
                            },
                            contentDescription = "No bets found",
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No ${internalFilter.displayName.lowercase()} bets found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (internalSearch.isNotBlank()) "No bets match \"$internalSearch\". Try clearing the search."
                            else "No bets currently in this category. Place a bet from the Sportsbook to see it live here!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        if (internalSearch.isNotBlank() || internalFilter != DashboardStatusCategory.ALL) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    internalFilter = DashboardStatusCategory.ALL
                                    internalSearch = ""
                                    onFilterChange(DashboardStatusCategory.ALL)
                                    onSearchChange("")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PulseCyan, contentColor = Color.Black)
                            ) {
                                Text("Reset Filters", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // 7. Bet Cards with distinct Status Indicators
            items(displayedBets, key = { it.id }) { bet ->
                DashboardBetCard(
                    bet = bet,
                    onCashOut = onCashOut,
                    onKickoffPending = onKickoffPending,
                    onSettleSingle = onSettleSingleBet
                )
            }
        }

        // 8. Friendly Wagers section (if friendly wagers exist and filter is ALL or PENDING)
        if (friendlyWagers.isNotEmpty() && (internalFilter == DashboardStatusCategory.ALL || internalFilter == DashboardStatusCategory.PENDING)) {
            item(key = "friendly_wagers_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = "Friendly",
                            tint = PulseCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FRIENDLY WAGERS (${friendlyWagers.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = onOpenCreateWager,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Wager", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(friendlyWagers, key = { "friendly_${it.id}" }) { wager ->
                FriendlyWagerDashboardCard(
                    wager = wager,
                    onResolve = onResolveWager
                )
            }
        }
    }
}

/**
 * Top KPI Summary Card displaying active, pending, resolved counts and amounts at risk.
 */
@Composable
private fun DashboardHeaderKpiCard(
    totalCount: Int,
    activeCount: Int,
    pendingCount: Int,
    resolvedCount: Int,
    totalAtRisk: Double,
    potentialReturns: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_kpi_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PulseEmerald.copy(alpha = 0.4f)))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PulseEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Bets Dashboard",
                            tint = PulseEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BETS DASHBOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            ),
                            color = TextSecondary
                        )
                        Text(
                            text = "Live Wagers & Status Tracking",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevatedDark)
                        .border(1.dp, BorderStroke, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$totalCount Total Bets",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PulseCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Distribution Row with clear badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active Card
                StatusCountBox(
                    count = activeCount,
                    title = "Active",
                    color = PulseEmerald,
                    icon = Icons.Default.FiberManualRecord,
                    modifier = Modifier.weight(1f)
                )

                // Pending Card
                StatusCountBox(
                    count = pendingCount,
                    title = "Pending",
                    color = PulseAmber,
                    icon = Icons.Default.HourglassEmpty,
                    modifier = Modifier.weight(1f)
                )

                // Resolved Card
                StatusCountBox(
                    count = resolvedCount,
                    title = "Resolved",
                    color = PulseCyan,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderStroke)
            Spacer(modifier = Modifier.height(12.dp))

            // Financial Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL AT RISK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format("$%,.2f", totalAtRisk),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "POTENTIAL RETURN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format("$%,.2f", potentialReturns),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = PulseEmerald
                    )
                }
            }
        }
    }
}

/**
 * Single Status Count Indicator inside header KPI card.
 */
@Composable
private fun StatusCountBox(
    count: Int,
    title: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }
    }
}

/**
 * Action controls to fast-forward live games or kickoff pending bets.
 */
@Composable
private fun DashboardActionControls(
    hasPending: Boolean,
    hasActive: Boolean,
    onKickoffPending: () -> Unit,
    onSimulateFastForward: () -> Unit,
    onNewWager: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Fast-Forward / Settle Active bets button
        Button(
            onClick = onSimulateFastForward,
            modifier = Modifier
                .weight(1f)
                .testTag("dashboard_settle_fast_forward_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevatedDark, contentColor = PulseEmerald),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PulseEmerald.copy(alpha = 0.5f)))
        ) {
            Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = "Simulate",
                tint = PulseEmerald,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Fast-Forward",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PulseEmerald
            )
        }

        // Kickoff Pending Match button (if pending bets exist)
        if (hasPending) {
            Button(
                onClick = onKickoffPending,
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_kickoff_pending_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PulseAmber, contentColor = Color.Black)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Kickoff",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kickoff Pending",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Filter Chip with optional status icon.
 */
@Composable
private fun StatusChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium
            )
        },
        leadingIcon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
            }
        } else null,
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedColor.copy(alpha = 0.2f),
            selectedLabelColor = selectedColor,
            selectedLeadingIconColor = selectedColor,
            containerColor = SurfaceDark,
            labelColor = TextSecondary,
            iconColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = BorderStroke,
            selectedBorderColor = selectedColor,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.5.dp
        ),
        modifier = Modifier.testTag(testTag)
    )
}

/**
 * Card representing a single bet in the LazyColumn, featuring the PROMINENT status indicator.
 */
@Composable
fun DashboardBetCard(
    bet: BetEntity,
    onCashOut: (Long) -> Unit,
    onKickoffPending: () -> Unit,
    onSettleSingle: (Long, Boolean) -> Unit
) {
    val category = bet.getDashboardStatusCategory()
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val placedDateText = remember(bet.placedAt) { dateFormat.format(Date(bet.placedAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_bet_card_${bet.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when (category) {
                    DashboardStatusCategory.PENDING -> PulseAmber.copy(alpha = 0.4f)
                    DashboardStatusCategory.ACTIVE -> PulseEmerald.copy(alpha = 0.45f)
                    DashboardStatusCategory.RESOLVED -> if (bet.status == "WON") PulseEmerald.copy(alpha = 0.35f) else BorderStroke
                    else -> BorderStroke
                }
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Header: Type Badge, Parlay Boost, and Prominent STATUS INDICATOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bet Type Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PulseCyan.copy(alpha = 0.15f))
                            .border(1.dp, PulseCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (bet.betType == "PARLAY") "PARLAY (${bet.legsCount} LEGS)" else "SINGLE BET",
                            color = PulseCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (bet.boostPercent > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PulseAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "+${bet.boostPercent}% BOOST",
                                color = PulseAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // THE PROMINENT STATUS INDICATOR FOR 'PENDING', 'ACTIVE', AND 'RESOLVED'
                BetStatusIndicatorBadge(
                    statusCategory = category,
                    rawStatus = bet.status,
                    betId = bet.id
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selections / Leg Details
            BetLegsSummaryView(
                summary = bet.selectionsSummary,
                detailJson = bet.selectionsDetailJson
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderStroke)
            Spacer(modifier = Modifier.height(10.dp))

            // Financials Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Stake", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        String.format("$%,.2f", bet.stake),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Odds", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        String.format("%.2f", bet.totalOdds),
                        color = PulseCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (category == DashboardStatusCategory.RESOLVED) "Settled Return" else "Potential Payout",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (category == DashboardStatusCategory.RESOLVED) {
                            String.format("$%,.2f", bet.payoutReceived)
                        } else {
                            String.format("$%,.2f", bet.potentialPayout)
                        },
                        color = if (category == DashboardStatusCategory.RESOLVED && bet.status == "LOST") PulseCrimson else PulseEmerald,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            // Footer based on status: Active (Cash Out), Pending (Awaiting Match), Resolved (Outcome summary)
            Spacer(modifier = Modifier.height(12.dp))

            when (category) {
                DashboardStatusCategory.ACTIVE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCashOut(bet.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cashout_button_${bet.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PulseAmber,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = "Cash out", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CASH OUT  •  ${String.format("$%,.2f", bet.cashOutOffer)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onSettleSingle(bet.id, true) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("quick_settle_win_${bet.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Win", tint = PulseEmerald, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                DashboardStatusCategory.PENDING -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevatedDark)
                            .border(1.dp, PulseAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = "Pending Kickoff",
                                tint = PulseAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scheduled kickoff • Starts soon",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = onKickoffPending,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PulseAmber, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("kickoff_bet_${bet.id}")
                        ) {
                            Text("Kickoff Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    val profit = bet.payoutReceived - bet.stake
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevatedDark)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (bet.status == "WON") Icons.Default.CheckCircle else if (bet.status == "CASHED_OUT") Icons.Default.Payments else Icons.Default.Cancel,
                                contentDescription = "Outcome",
                                tint = if (bet.status == "WON") PulseEmerald else if (bet.status == "CASHED_OUT") PulseAmber else PulseCrimson,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Placed: $placedDateText",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Text(
                            text = if (profit >= 0) "+${String.format("$%,.2f", profit)} Net" else "${String.format("$%,.2f", profit)} Net",
                            color = if (profit >= 0) PulseEmerald else PulseCrimson,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Status Indicator Badge Composable:
 * Clearly represents 'Pending', 'Active', and 'Resolved' statuses with distinct styling,
 * pulsing indicators, and test tags.
 */
@Composable
fun BetStatusIndicatorBadge(
    statusCategory: DashboardStatusCategory,
    rawStatus: String,
    betId: Long = 0,
    modifier: Modifier = Modifier
) {
    // Pulse animation for Active status indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_dot_pulse"
    )

    when (statusCategory) {
        DashboardStatusCategory.PENDING -> {
            Box(
                modifier = modifier
                    .testTag("status_indicator_pending_${betId}")
                    .clip(RoundedCornerShape(8.dp))
                    .background(PulseAmber.copy(alpha = 0.16f))
                    .border(1.dp, PulseAmber.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Pending status",
                        tint = PulseAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PENDING",
                        color = PulseAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        DashboardStatusCategory.ACTIVE -> {
            Box(
                modifier = modifier
                    .testTag("status_indicator_active_${betId}")
                    .clip(RoundedCornerShape(8.dp))
                    .background(PulseEmerald.copy(alpha = 0.16f))
                    .border(1.dp, PulseEmerald.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing live indicator dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PulseEmerald)
                            .alpha(alphaAnim)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "ACTIVE",
                        color = PulseEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        else -> {
            val isWon = rawStatus.equals("WON", ignoreCase = true)
            val isCashedOut = rawStatus.equals("CASHED_OUT", ignoreCase = true)
            val badgeColor = when {
                isWon -> PulseEmerald
                isCashedOut -> PulseAmber
                rawStatus.equals("LOST", ignoreCase = true) -> PulseCrimson
                else -> PulseCyan
            }
            val statusLabel = when {
                isWon -> "RESOLVED • WON"
                isCashedOut -> "RESOLVED • CASHED OUT"
                rawStatus.equals("LOST", ignoreCase = true) -> "RESOLVED • LOST"
                else -> "RESOLVED"
            }
            val statusIcon = when {
                isWon -> Icons.Default.CheckCircle
                isCashedOut -> Icons.Default.Payments
                rawStatus.equals("LOST", ignoreCase = true) -> Icons.Default.Cancel
                else -> Icons.Default.DoneAll
            }

            Box(
                modifier = modifier
                    .testTag("status_indicator_resolved_${betId}")
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.16f))
                    .border(1.dp, badgeColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Resolved status",
                        tint = badgeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusLabel,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
    }
}

/**
 * Leg breakdown inside a Bet Card.
 */
@Composable
private fun BetLegsSummaryView(
    summary: String,
    detailJson: String
) {
    if (detailJson.contains("||")) {
        // Multi-leg Parlay: split and display individual legs
        val legs = detailJson.split("||").filter { it.isNotBlank() }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            legs.forEach { legStr ->
                val parts = legStr.split("##")
                val matchTitle = parts.getOrNull(1) ?: ""
                val pick = parts.getOrNull(2) ?: ""
                val market = parts.getOrNull(3) ?: ""
                val odds = parts.getOrNull(4) ?: ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevatedDark)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = matchTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pick,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            if (market.isNotBlank()) {
                                Text(
                                    text = " ($market)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    if (odds.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PulseCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = odds,
                                color = PulseCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Single bet selection summary
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
        )
    }
}

/**
 * Friendly wager card within dashboard.
 */
@Composable
private fun FriendlyWagerDashboardCard(
    wager: FriendlyWagerEntity,
    onResolve: (FriendlyWagerEntity, Boolean) -> Unit
) {
    val category = wager.getDashboardStatusCategory()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friendly_wager_card_${wager.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PulseCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FRIENDLY WAGER",
                            color = PulseCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                BetStatusIndicatorBadge(
                    statusCategory = category,
                    rawStatus = wager.status,
                    betId = wager.id
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = wager.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Opponent: ${wager.opponent}  •  ${wager.category}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                    fontSize = 11.sp
                )

                if (wager.status == "PENDING") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onResolve(wager, true) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PulseEmerald, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("I Won", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onResolve(wager, false) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Lost", fontSize = 10.sp, color = PulseCrimson)
                        }
                    }
                }
            }
        }
    }
}
