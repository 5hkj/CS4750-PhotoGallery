package com.hyunkyung.cs4750.photogallery

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hyunkyung.cs4750.photogallery.api.FlickrFetchr

private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {
    override fun doWork(): Result {
        val query = QueryPreference.getStoredQuery(context)
        val lastResultId = QueryPreference.getLastResultId(context)
        val items: List<GalleryItem> =
            if (query.isEmpty()) {
                FlickrFetchr().fetchPhotosRequest()
                    .execute()
                    .body()
                    ?.photos
                    ?.galleryItems
            } else {
                FlickrFetchr().searchPhotosRequest(query)
                    .execute()
                    .body()
                    ?.photos
                    ?.galleryItems
            } ?: emptyList()

        if (items.isEmpty())
            return Result.success()

        val resultId = items.first().id
        if (resultId == lastResultId)
            Log.i(TAG, "Got an old result: $resultId")
        else {
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreference.setLastResultId(context, resultId)

            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val resources = context.resources
            val notification = NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            showBackgroundNotification(0, notification)
        }
        return Result.success()
    }

    private fun showBackgroundNotification(
        requestCode: Int,
        notification: Notification
    ) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }
        context.sendOrderedBroadcast(intent, PERM_PRIVATE)
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.hyunkyung.cs4750.photogallery.SHOW_NOTIFICATION"
        const val PERM_PRIVATE = "com.hyunkyung.cs4750.photogallery.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}