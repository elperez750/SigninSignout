package com.example.lab4signinsignout.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 *
 * Receives Firebase Cloud Messaging (FCM) events: new device tokens and
 * incoming push notifications.
 */
public class MessagingService extends FirebaseMessagingService {

    /**
     * Called when Firebase generates a new device token.
     *
     * @param token the new FCM token for this device
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "Token: " + token);
    }

    /**
     * Called when a push notification arrives while the app is in the foreground.
     *
     * @param message the message received from Firebase
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        if (message.getNotification() != null) {
            Log.d("FCM", "460 Message: " + message.getNotification().getBody());
        }
    }
}