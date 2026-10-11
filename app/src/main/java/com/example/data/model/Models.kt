package com.example.data.model

enum class SportType(val displayName: String, val emoji: String) {
    SOCCER("Soccer", "⚽"),
    BASKETBALL("Basketball", "🏀"),
    TENNIS("Tennis", "🎾"),
    FOOTBALL("NFL", "🏈"),
    ESPORTS("Esports", "🎮")
}

data class BetOption(
    val id: String,
    val name: String,
    val odds: Double,
    val marketName: String
)

data class Market(
    val name: String,
    val options: List<BetOption>
)

data class Match(
    val id: String,
    val sport: SportType,
    val league: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val isLive: Boolean,
    val matchStatusText: String, // e.g. "72'", "Q3 04:15", "Set 2 5-4", "Tomorrow 19:45"
    val markets: List<Market>,
    val recentEvent: String? = null
)

data class SlipSelection(
    val selectionId: String,
    val matchId: String,
    val matchTitle: String,
    val sport: SportType,
    val marketName: String,
    val pickName: String,
    val odds: Double
)

enum class BetStatus(val label: String) {
    PENDING("Pending"),
    ACTIVE("Active"),
    RESOLVED("Resolved"),
    WON("Won"),
    LOST("Lost"),
    CASHED_OUT("Cashed Out")
}

enum class DashboardStatusCategory(val displayName: String) {
    ALL("All Bets"),
    PENDING("Pending"),
    ACTIVE("Active"),
    RESOLVED("Resolved")
}

