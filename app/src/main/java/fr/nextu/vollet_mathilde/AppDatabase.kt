package fr.nextu.vollet_mathilde

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.nextu.vollet_mathilde.entity.Movie

@Database(entities = [Movie::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDAO
}