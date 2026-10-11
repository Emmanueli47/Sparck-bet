package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BetOption
import com.example.data.model.Match
import com.example.ui.theme.BorderStroke
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
fun MatchCard(
    match: Match,
    isOptionSelected: (String) -> Boolean,
    onOptionClick: (Match, BetOption) -> Unit,
    onOpenDetails: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: League & Match status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = match.sport.emoji,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = match.league,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Live badge or Upcoming
                if (match.isLive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PulseCrimson.copy(alpha = 0.15f))
                            .border(1.dp, PulseCrimson.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "Live",
                            tint = PulseCrimson,
                            modifier = Modifier.size(8.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE ${match.matchStatusText}",
                            color = PulseCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Text(
                        text = match.matchStatusText,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scoreboard / Teams
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = match.homeTeam,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scores
                if (match.isLive) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevatedDark)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${match.homeScore}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PulseCyan
                        )
                        Text(
                            text = " : ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = "${match.awayScore}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PulseCyan
                        )
                    }
                } else {
                    Text(
                        text = "VS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }

                // Away Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = match.awayTeam,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Recent Event Ticker
            if (!match.recentEvent.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceVariantDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.recentEvent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = PulseCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Market Odds Buttons (e.g. 1X2 or Moneyline)
            val primaryMarket = match.markets.firstOrNull()
            if (primaryMarket != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    primaryMarket.options.forEach { option ->
                        OddsButton(
                            option = option,
                            isSelected = isOptionSelected(option.id),
                            onClick = { onOptionClick(match, option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // More Markets link
            if (match.markets.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetails(match) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+${match.markets.size - 1} More Markets (Over/Under, BTTS)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = PulseCyan
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Expand Markets",
                        tint = PulseCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OddsButton(
    option: BetOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PulseEmerald else SurfaceVariantDark,
        label = "oddsBg"
    )
    val textColor = if (isSelected) Color.Black else TextPrimary
    val oddsColor = if (isSelected) Color.Black else PulseEmerald

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isSelected) PulseEmerald else BorderStroke,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .testTag("odds_button_${option.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = option.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format("%.2f", option.odds),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                ),
                color = oddsColor
            )
        }
    }
}
