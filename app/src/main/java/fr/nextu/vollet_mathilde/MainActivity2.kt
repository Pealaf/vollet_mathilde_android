package fr.nextu.vollet_mathilde

import android.os.Bundle
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import fr.nextu.vollet_mathilde.databinding.ActivityMain2Binding
import fr.nextu.vollet_mathilde.entity.ListeMovies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainActivity2 : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMain2Binding

    private lateinit var json: TextView
    private lateinit var movies_recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main2)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        test()

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        //json = findViewById(R.id.json)
        movies_recycler = findViewById<RecyclerView>(R.id.recycle_view).apply{
            adapter   = MovieAdapter(ListeMovies(emptyList()))
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity2)
        }
    }

    override fun onStart() {
        super.onStart()
        getPictureList()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main2)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun test() {
        BeersService.startAction(this)
    }

    /*fun requestMoviesList() = CoroutineScope(Dispatchers.IO).async {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("http://api.betaseries.com/movies/list")
            .get()
            .addHeader("X-BetaSeries-Key", "470d2afc452f")
            .build()

        val response : Response = client.newCall(request).execute()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifyNewData(response)
        }
        moviesFromJson(response.body?.string() ?: "")
    }*/

    fun getPictureList() {
        CoroutineScope(Dispatchers.IO).launch {
            requestPictureList {

                var gson = Gson()
                // var movies = gson.fromJson(it, Movie::class.java)
                var movies = gson.fromJson(it, ListeMovies::class.java)
                //json.text = movies.toString()

                movies_recycler.adapter = MovieAdapter(movies)
            }
        }
    }

    fun requestPictureList(callback: (String) -> Unit) {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://api.betaseries.com/movies/list")
            .get()
            .addHeader("X-BetaSeries-Key", "77b233b849ac")
            .build()

        val response: Response = client.newCall(request).execute()
        CoroutineScope(Dispatchers.Main).launch {
            callback(response.body?.string() ?: "")
        }
    }
}