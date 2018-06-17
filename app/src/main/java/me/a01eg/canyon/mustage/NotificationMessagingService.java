package me.a01eg.canyon.mustage;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";

    public NotificationMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        super.onMessageReceived(remoteMessage);
    }
}
