package fr.nextu.vollet_mathilde.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie")
data class Movie(
    @PrimaryKey var id: String,
    @ColumnInfo var title: String,
    @ColumnInfo var tmdb_id: String,
    @ColumnInfo var imdb_id: String? = null,
    @ColumnInfo var followers: String,
    @ColumnInfo var production_year: String,
    @ColumnInfo var poster: String? = null
)

data class ListeMovies(var movies: List<Movie>)