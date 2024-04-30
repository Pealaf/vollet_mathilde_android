package fr.nextu.vollet_mathilde

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.nextu.vollet_mathilde.entity.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDAO {

    @Query("SELECT * FROM movie")
    fun getAll(): List<Movie>

    @Query("SELECT * FROM movie WHERE id IN (:id)")
    fun loadAllByIds(id: IntArray): List<Movie>

    @Query("SELECT * FROM movie WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): Movie

    @Insert
    fun insertAll(vararg movies: Movie)

    @Delete
    fun delete(movie: Movie)

    @Query("SELECT * FROM movie")
    fun getFlowData(): Flow<List<Movie>>

    /*@Query("SELECT * FROM movie WHERE id = :id")
    fun selectByTitle()*/
}