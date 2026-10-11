package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserAccountEntity::class, BetEntity::class, FriendlyWagerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun betDao(): BetDao
    abstract fun userDao(): UserDao
    abstract fun friendlyWagerDao(): FriendlyWagerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "betpulse_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
