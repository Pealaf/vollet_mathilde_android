package fr.nextu.vollet_mathilde

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import fr.nextu.vollet_mathilde.databinding.ActivityMainBinding
import fr.nextu.vollet_mathilde.entity.Movies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    val db: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }
    lateinit var movies_recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab?.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        movies_recycler = findViewById<RecyclerView>(R.id.movies_recylcer).apply {
            adapter = MovieAdapter(emptyList())
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
        }

        createNotificationChannel()
        showExpandableNotification()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        updateViewFromDB()
        requestMoviesList(::moviesFromJson)
    }

    fun updateViewFromDB() {
        CoroutineScope(Dispatchers.IO).launch {
            val flow = db.movieDao().getFlowData()
            flow.collect{
                CoroutineScope(Dispatchers.Main).launch {
                    movies_recycler.adapter = MovieAdapter(it)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showExpandableNotification() {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.new_notification_title))
            .setContentText("Contenu de la notification")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Contenu de la notification dépliée"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val requestPermissionLauncher =
            this.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    NotificationManagerCompat.from(this).notify(1, builder.build())
                }
            }

        when {
            ContextCompat.checkSelfPermission(this,POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                NotificationManagerCompat.from(this).notify(1, builder.build())
            }
            else -> {
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }
    }

    fun requestMoviesList(callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch{
            val client = OkHttpClient()

            val request: Request = Request.Builder()
                .url("https://api.betaseries.com/movies/list")
                .get()
                .addHeader("X-BetaSeries-Key", getString(R.string.betaseries_api_key))
                .build()

            val response: Response = client.newCall(request).execute()

            callback(response.body?.string() ?: "")
        }
    }

    fun moviesFromJson(json: String) {
        val gson = Gson()
        val om = gson.fromJson(json,  Movies::class.java)
        Log.d("tag", "moviesFromJson: ${om.movies.size}")
        db.movieDao().insertAll(*om.movies.toTypedArray())
    }

    companion object {
        const val CHANNEL_ID = "fr_nextu_vollet_mathilde_channel_notification"
    }
}