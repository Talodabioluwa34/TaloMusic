package com.talodabi.talomusic.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.talodabi.talomusic.data.local.dao.AnalysisDao
import com.talodabi.talomusic.data.local.dao.FavoriteDao
import com.talodabi.talomusic.data.local.dao.PlaylistDao
import com.talodabi.talomusic.data.local.dao.RecentPlayDao
import com.talodabi.talomusic.data.local.entity.ChordEventEntity
import com.talodabi.talomusic.data.local.entity.FavoriteSongEntity
import com.talodabi.talomusic.data.local.entity.LoopPresetEntity
import com.talodabi.talomusic.data.local.entity.PlaylistEntity
import com.talodabi.talomusic.data.local.entity.PlaylistSongCrossRef
import com.talodabi.talomusic.data.local.entity.RecentPlayEntity
import com.talodabi.talomusic.data.local.entity.SongAnalysisEntity
import com.talodabi.talomusic.data.local.entity.SongSectionEntity

@Database(
    entities = [
        FavoriteSongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        RecentPlayEntity::class,
        SongAnalysisEntity::class,
        ChordEventEntity::class,
        SongSectionEntity::class,
        LoopPresetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TaloDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentPlayDao(): RecentPlayDao
    abstract fun analysisDao(): AnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: TaloDatabase? = null

        fun getInstance(context: Context): TaloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaloDatabase::class.java,
                    "talo_music.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
