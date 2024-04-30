package fr.nextu.vollet_mathilde

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
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
    lateinit var db: AppDatabase

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

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "movies.db"
        ).build()
        /*db = AppDatabase by lazy {
            AppDatabase.getInstance(applicationContext)
        }*/

        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        updateViewFromDB()
        //requestPictureList(::getPictureList)
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getPictureList() {
        CoroutineScope(Dispatchers.IO).launch {
            requestPictureList {

                var gson = Gson()
                // var movies = gson.fromJson(it, Movie::class.java)
                var movies = gson.fromJson(it, ListeMovies::class.java)
                //json.text = movies.toString()
                db.movieDao().insertAll(*movies.movies.toTypedArray())
                //movies_recycler.adapter = MovieAdapter(ListeMovies(db.movieDao().getAll()))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPictureList(callback: (String) -> Unit) {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://api.betaseries.com/movies/list")
            .get()
            .addHeader("X-BetaSeries-Key", "77b233b849ac")
            .build()

        val response: Response = client.newCall(request).execute()
        val reponse = response.body?.string() ?: ""
        CoroutineScope(Dispatchers.Main).launch {
            notifyNewData(reponse)
        }
        callback(reponse)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Movie update"
            val descriptionText = "A update notification when new movies come"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notifyNewData(response: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Movies list updated")
            .setContentText(response ?: "")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {

            if(ActivityCompat.checkSelfPermission(
                    this@MainActivity2,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
               /*this@MainActivity2.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        notify(1, builder.build())
                    } else {
                        Log.d("MainActivity2", "Permission denied")
                    }
                }.launch(android.Manifest.permission.POST_NOTIFICATIONS)*/
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
                notify(1, builder.build())
                return@with
            }

        }
    }

     companion object {
         const val CHANNEL_ID = "";
     }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }*/

    fun updateViewFromDB() {
        CoroutineScope(Dispatchers.IO).launch {
            val flow = db.movieDao().getFlowData()
            flow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    movies_recycler.adapter = MovieAdapter(ListeMovies(it))
                }
            }
            //movies_recycler.adapter = MovieAdapter(db.movieDao().getAll())
        }
    }
}