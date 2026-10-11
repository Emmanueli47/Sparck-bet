package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.getDashboardStatusCategory
import com.example.data.model.SlipSelection
import com.example.data.model.SportType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BetPulse", appName)
  }

  @Test
  fun `parlay odds multiplier calculation`() {
    val leg1 = SlipSelection("1", "m1", "Real vs City", SportType.SOCCER, "Winner", "Real Madrid", 2.00)
    val leg2 = SlipSelection("2", "m2", "Lakers vs Warriors", SportType.BASKETBALL, "Winner", "Warriors", 1.50)
    val leg3 = SlipSelection("3", "m3", "Arsenal vs Chelsea", SportType.SOCCER, "Winner", "Arsenal", 1.80)

    val selections = listOf(leg1, leg2, leg3)
    val rawOdds = selections.fold(1.0) { acc, s -> acc * s.odds }
    val roundedOdds = (rawOdds * 100.0).roundToInt() / 100.0

    // 2.00 * 1.50 * 1.80 = 5.40
    assertEquals(5.40, roundedOdds, 0.01)

    // With 3 legs: +10% boost
    val boost = 10
    val boostMultiplier = 1.0 + (boost / 100.0)
    val stake = 100.0
    val potentialPayout = (stake * roundedOdds * boostMultiplier * 100.0).roundToInt() / 100.0
    // 100 * 5.40 * 1.10 = 594.00
    assertEquals(594.00, potentialPayout, 0.01)
  }

  @Test
  fun `verify dashboard status indicators for pending active and resolved`() {
    val pendingBet = com.example.data.db.BetEntity(
        id = 1,
        betType = "SINGLE",
        selectionsSummary = "KC Chiefs (1.82)",
        selectionsDetailJson = "m5##KC vs BUF##KC Chiefs##Moneyline##1.82##FOOTBALL",
        legsCount = 1,
        stake = 50.0,
        totalOdds = 1.82,
        potentialPayout = 91.0,
        status = "PENDING",
        cashOutOffer = 45.0
    )
    val activeBet = com.example.data.db.BetEntity(
        id = 2,
        betType = "PARLAY",
        selectionsSummary = "Real Madrid (1.85); Warriors (1.70)",
        selectionsDetailJson = "...",
        legsCount = 2,
        stake = 40.0,
        totalOdds = 3.15,
        potentialPayout = 132.30,
        status = "ACTIVE",
        cashOutOffer = 52.8
    )
    val wonBet = com.example.data.db.BetEntity(
        id = 3,
        betType = "SINGLE",
        selectionsSummary = "FaZe Clan (1.62)",
        selectionsDetailJson = "...",
        legsCount = 1,
        stake = 30.0,
        totalOdds = 1.62,
        potentialPayout = 48.6,
        status = "WON",
        cashOutOffer = 0.0
    )
    val cashedOutBet = com.example.data.db.BetEntity(
        id = 4,
        betType = "SINGLE",
        selectionsSummary = "Alcaraz (1.75)",
        selectionsDetailJson = "...",
        legsCount = 1,
        stake = 25.0,
        totalOdds = 1.75,
        potentialPayout = 43.75,
        status = "CASHED_OUT",
        cashOutOffer = 0.0
    )

    assertEquals(com.example.data.model.DashboardStatusCategory.PENDING, pendingBet.getDashboardStatusCategory())
    assertEquals(com.example.data.model.DashboardStatusCategory.ACTIVE, activeBet.getDashboardStatusCategory())
    assertEquals(com.example.data.model.DashboardStatusCategory.RESOLVED, wonBet.getDashboardStatusCategory())
    assertEquals(com.example.data.model.DashboardStatusCategory.RESOLVED, cashedOutBet.getDashboardStatusCategory())
  }
}
