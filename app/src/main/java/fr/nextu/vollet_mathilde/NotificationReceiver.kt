package fr.nextu.vollet_mathilde

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationReceiver  : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_CLICK") {
            // Gérer l'action de notification ici
            Toast.makeText(context, "Action de notification cliquée", Toast.LENGTH_SHORT).show()
        }
    }
}