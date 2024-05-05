package fr.nextu.vollet_mathilde

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.nextu.vollet_mathilde.entity.Movie
import fr.nextu.vollet_mathilde.entity.MovieDAO

@Database(entities = [Movie::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDAO

    companion object {
        fun getInstance(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "app_exemple.db"
            ).build()
        }
    }
}