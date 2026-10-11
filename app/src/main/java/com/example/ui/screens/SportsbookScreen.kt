package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BetOption
import com.example.data.model.Match
import com.example.data.model.SportType
import com.example.ui.components.MatchCard
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
fun SportsbookScreen(
    matches: List<Match>,
    selectedSport: SportType?,
    onSportSelected: (SportType?) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isOptionSelected: (String) -> Unit, // we pass check lambda
    isOptionSelectedPredicate: (String) -> Boolean,
    onOptionClick: (Match, BetOption) -> Unit,
    onOpenDetails: (Match) -> Unit,
    onSimulateFastForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("sportsbook_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bet_hero_stadium),
                    contentDescription = "Championship Arena",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    SurfaceDark.copy(alpha = 0.85f),
                                    SurfaceDark
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PulseEmerald.copy(alpha = 0.2f))
                            .border(1.dp, PulseEmerald, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Boost",
                            tint = PulseEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PARLAY ACCUMULATOR BOOST UP TO +25%",
                            color = PulseEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Real-Time Odds & Live Markets",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary
                    )

                    Text(
                        text = "Build singles & parlays across football, basketball, tennis, and esports.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }
            }
        }

        // Fast Simulate bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevatedDark)
                    .border(1.dp, BorderStroke, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsScore,
                        contentDescription = "Live matches",
                        tint = PulseCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Live Match Simulation",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Scores & odds update live every few seconds",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = onSimulateFastForward,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PulseCyan, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("fast_simulate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Fast-forward",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Settle Bets",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("sportsbook_search_bar"),
                placeholder = {
                    Text("Search teams, leagues, or sports...", color = TextMuted, fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                },
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

        // Sports Category Filters Horizontal Scroll
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.width(8.dp))

                // All Sports
                val isAllSelected = selectedSport == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isAllSelected) PulseEmerald else SurfaceVariantDark)
                        .border(1.dp, if (isAllSelected) PulseEmerald else BorderStroke, RoundedCornerShape(20.dp))
                        .clickable { onSportSelected(null) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_all_sports"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ All Sports",
                        color = if (isAllSelected) Color.Black else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sport Categories
                SportType.entries.forEach { sport ->
                    val isSelected = selectedSport == sport
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PulseEmerald else SurfaceVariantDark)
                            .border(1.dp, if (isSelected) PulseEmerald else BorderStroke, RoundedCornerShape(20.dp))
                            .clickable { onSportSelected(sport) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_${sport.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sport.emoji} ${sport.displayName}",
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // Match List
        if (matches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matches found",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing search or picking another sport",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(matches, key = { it.id }) { match ->
                MatchCard(
                    match = match,
                    isOptionSelected = isOptionSelectedPredicate,
                    onOptionClick = onOptionClick,
                    onOpenDetails = onOpenDetails,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}
