package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BorderStroke
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreateWagerDialog(
    userBalance: Double,
    onDismiss: () -> Unit,
    onCreate: (title: String, opponent: String, category: String, userPick: String, opponentPick: String, stake: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var opponent by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sports Challenge") }
    var userPick by remember { mutableStateOf("") }
    var opponentPick by remember { mutableStateOf("") }
    var stakeText by remember { mutableStateOf("50") }

    val stakeAmount = stakeText.toDoubleOrNull() ?: 0.0
    val isValid = title.isNotBlank() && opponent.isNotBlank() && userPick.isNotBlank() && opponentPick.isNotBlank() && stakeAmount > 0 && stakeAmount <= userBalance

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_wager_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderStroke))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "CREATE FRIENDLY WAGER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = PulseCyan
                )
                Text(
                    text = "Challenge a friend on any custom game, match, or prediction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Wager Title (e.g. FIFA 24 Rematch)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("wager_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseEmerald,
                        unfocusedBorderColor = BorderStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = opponent,
                    onValueChange = { opponent = it },
                    label = { Text("Friend / Opponent Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("wager_opponent_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseEmerald,
                        unfocusedBorderColor = BorderStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = userPick,
                        onValueChange = { userPick = it },
                        label = { Text("Your Pick") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PulseEmerald,
                            unfocusedBorderColor = BorderStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = opponentPick,
                        onValueChange = { opponentPick = it },
                        label = { Text("Opponent's Pick") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PulseCyan,
                            unfocusedBorderColor = BorderStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stakeText,
                    onValueChange = { stakeText = it },
                    label = { Text("Wager Stake ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("wager_stake_input"),
                    supportingText = {
                        Text("Available balance: ${String.format("$%,.2f", userBalance)}", color = TextSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseEmerald,
                        unfocusedBorderColor = BorderStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            onCreate(title, opponent, category, userPick, opponentPick, stakeAmount)
                        },
                        enabled = isValid,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseEmerald,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("submit_wager_button")
                    ) {
                        Text("Start Wager", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
