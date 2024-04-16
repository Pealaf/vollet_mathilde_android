package fr.nextu.vollet_mathilde.entity

data class Movie(var id: String, var title: String, var tmdb_id: String, var imdb_id: String? = null, var followers: String, var production_year: String, var poster: String? = null
)

data class ListeMovies(var movies: List<Movie>)