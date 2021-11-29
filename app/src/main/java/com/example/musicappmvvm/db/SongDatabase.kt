package com.example.musicappmvvm.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.utils.Constants.DB_NAME


@Database(
    entities = [SongItem::class],
    version = 2,
    exportSchema = false
)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var instance: SongDatabase? = null

        fun getDatabase(context: Context): SongDatabase = instance ?: synchronized(this) {
            return Room.databaseBuilder(
                context.applicationContext,
                SongDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration()
                .build()
                .also {
                instance = it
            }
        }

    }
}