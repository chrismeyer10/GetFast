package com.example.getfast.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.getfast.R

class Notifier(private val context: Context) {
    fun notifyNewListing(title: String) {
        val builder = NotificationCompat.Builder(context, "new_listing")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(title.hashCode(), builder.build())
    }
}
