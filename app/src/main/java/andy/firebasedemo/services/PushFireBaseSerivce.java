package andy.firebasedemo.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import andy.firebasedemo.ChatRoomActivity;
import andy.firebasedemo.R;

/**
 * Created by andyli on 2016/10/23.
 */

public class PushFireBaseSerivce extends FirebaseMessagingService {
	private static final String TAG = "PushFireBaseSerivce";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {

		// TODO(developer): Handle FCM messages here.
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: " + remoteMessage.getMessageType());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
			sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
		}

		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.
	}
	// [END receive_message]

	/**
	 * Create and show a simple notification containing the received FCM message.
	 *
	 * @param messageBody FCM message body received.
	 */
	private void sendNotification(String title, String messageBody) {
		Intent intent = new Intent(this, ChatRoomActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
				PendingIntent.FLAG_ONE_SHOT);

		int defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setDefaults(defaults)
				.setSmallIcon(R.mipmap.messageing)
				.setContentTitle(title)
				.setContentText(messageBody)
				.setAutoCancel(true)
				.setContentIntent(pendingIntent)
				.setFullScreenIntent(pendingIntent, true);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(9999, notificationBuilder.build());

	}

}
