package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UserAccountEntity
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

@Composable
fun AnalyticsScreen(
    account: UserAccountEntity?,
    onClaimDailyBonus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val acc = account ?: UserAccountEntity()
    val netProfit = acc.totalWon - acc.totalWagered
    val winRate = if (acc.betsPlaced > 0) (acc.betsWon.toDouble() / acc.betsPlaced.toDouble()) * 100.0 else 0.0
    val roi = if (acc.totalWagered > 0) (netProfit / acc.totalWagered) * 100.0 else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Bankroll Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PulseEmerald.copy(alpha = 0.5f)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL BANKROLL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PulseEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "VIRTUAL CREDITS",
                                color = PulseEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format("$%,.2f", acc.balance),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        color = PulseEmerald
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderStroke)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Won", color = TextSecondary, fontSize = 11.sp)
                            Text(String.format("$%,.2f", acc.totalWon), color = PulseCyan, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net Profit/Loss", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = if (netProfit >= 0) "+${String.format("$%,.2f", netProfit)}" else String.format("$%,.2f", netProfit),
                                color = if (netProfit >= 0) PulseEmerald else PulseCrimson,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Est. ROI", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = String.format("%.1f%%", roi),
                                color = if (roi >= 0) PulseEmerald else PulseCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Daily Reload Bonus Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PulseAmber.copy(alpha = 0.4f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PulseAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Bonus",
                                tint = PulseAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Daily Bankroll Reload",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Claim $500 free virtual credits every day",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onClaimDailyBonus,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseAmber,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("claim_daily_bonus_button")
                    ) {
                        Text("Claim +$500", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }

        // Performance Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Win Rate Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Win Rate", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PulseEmerald, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.1f%%", winRate),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = PulseEmerald
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (winRate / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = PulseEmerald,
                            trackColor = SurfaceElevatedDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${acc.betsWon} won / ${acc.betsPlaced} total",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Win Streak Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Streak", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = PulseAmber, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${acc.winStreak} WINS",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = PulseAmber
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Best Streak: ${acc.bestStreak} Wins in a row",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Trophies / Achievements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Trophies", tint = PulseAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bettor Badges & Milestones",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val badges = listOf(
                        Triple("🎯 Rookie", "Placed 1st bet", acc.betsPlaced >= 1),
                        Triple("🔥 Hot Streak", "3+ Win Streak", acc.bestStreak >= 3),
                        Triple("🚀 High Roller", "Wagered $1,000+", acc.totalWagered >= 1000.0),
                        Triple("👑 Champion", "Won 5+ bets", acc.betsWon >= 5)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        badges.forEach { (name, desc, unlocked) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (unlocked) PulseEmerald.copy(alpha = 0.15f) else SurfaceVariantDark)
                                    .border(1.dp, if (unlocked) PulseEmerald else BorderStroke, RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (unlocked) PulseEmerald else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (unlocked) "Unlocked" else "Locked",
                                        fontSize = 9.sp,
                                        color = if (unlocked) PulseEmerald else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pro Betting Guide & Safe Staking Rule
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Bankroll management", tint = PulseCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Smart Bankroll Strategy Tips",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulseCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• The 1-3% Rule: Keep individual wager sizes below 3% of your total virtual balance to endure cold streaks.\n" +
                                "• Parlay Boosts: Combine 3+ legs to trigger high multiplier bonuses, but balance with high-probability singles.\n" +
                                "• Timed Cash-Outs: Monitor in-play games and lock in profit early when underdogs take a sudden lead.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
