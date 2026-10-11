package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SlipSelection
import com.example.ui.theme.BorderStroke
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BetSlipDrawer(
    selections: List<SlipSelection>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onRemoveSelection: (String) -> Unit,
    onClearAll: () -> Unit,
    isParlay: Boolean,
    onParlayChange: (Boolean) -> Unit,
    currentStake: Double,
    onStakeChange: (Double) -> Unit,
    onAddStake: (Double) -> Unit,
    onMaxStake: () -> Unit,
    totalOdds: Double,
    potentialPayout: Double,
    parlayBoost: Int,
    userBalance: Double,
    onPlaceBet: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selections.isEmpty()) return

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isExpanded) 0.dp else 12.dp)
                .testTag("bet_slip_container"),
            shape = if (isExpanded) RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) else RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PulseEmerald.copy(alpha = 0.5f)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isExpanded) 16.dp else 12.dp)
            ) {
                // Collapsed Bar or Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandChange(!isExpanded) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PulseEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${selections.size}",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isParlay && selections.size > 1) "PARLAY ACCUMULATOR" else "BET SLIP",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = TextPrimary
                                )
                                if (parlayBoost > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+$parlayBoost% BOOST",
                                        color = PulseAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Odds: ${String.format("%.2f", totalOdds)}x  •  Pot. Win: ${String.format("$%,.2f", potentialPayout)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = PulseEmerald
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isExpanded) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PulseEmerald)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "VIEW SLIP",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        } else {
                            IconButton(onClick = { onClearAll() }, modifier = Modifier.size(32.dp)) {
                                Text("Clear", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Expanded Content
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderStroke)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Parlay Boost Badge
                    if (selections.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PulseAmber.copy(alpha = 0.15f))
                                .border(1.dp, PulseAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Boost",
                                tint = PulseAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Parlay Multiplier: ${String.format("%.2f", totalOdds)}x with +$parlayBoost% extra payout!",
                                color = PulseAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Selections List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(selections, key = { it.selectionId }) { sel ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceVariantDark)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${sel.pickName}  @ ${String.format("%.2f", sel.odds)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = PulseEmerald,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${sel.matchTitle} • ${sel.marketName}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveSelection(sel.selectionId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove pick",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Stake Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10.0, 25.0, 50.0, 100.0).forEach { amount ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (currentStake == amount) PulseEmerald else SurfaceElevatedDark)
                                    .clickable { onStakeChange(amount) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$${amount.toInt()}",
                                    color = if (currentStake == amount) Color.Black else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PulseAmber.copy(alpha = 0.2f))
                                .clickable { onMaxStake() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MAX",
                                color = PulseAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stake Input & Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Stake Amount",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = String.format("$%,.2f", currentStake),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Potential Payout",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = String.format("$%,.2f", potentialPayout),
                                color = PulseEmerald,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Place Bet Button
                    val isBalanceSufficient = userBalance >= currentStake && currentStake > 0
                    Button(
                        onClick = { onPlaceBet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("place_bet_button"),
                        enabled = isBalanceSufficient,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseEmerald,
                            contentColor = Color.Black,
                            disabledContainerColor = SurfaceElevatedDark,
                            disabledContentColor = TextMuted
                        )
                    ) {
                        Text(
                            text = if (isBalanceSufficient) "PLACE WAGER  •  ${String.format("$%,.2f", currentStake)}" else "INSUFFICIENT BALANCE",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
