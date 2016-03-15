package xsoft.com.rucal;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class GcmMessageHandler extends IntentService {

    private String mes;
    private double lat;
    private double lon;
    private String tipo;
    private Handler handler;
    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        try {
            JSONObject jObj = new JSONObject(extras.getString("msg"));
            mes=jObj.getString("comentario");
            tipo=jObj.getString("tipo");
            lat=jObj.getDouble("lat");
            lon=jObj.getDouble("lon");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showNotificacion();
        Log.i("GCM", "Received : (" +messageType+")  "+extras.getString("msg"));
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void showNotificacion(){
        handler.post(new Runnable() {
            public void run() {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                mBuilder.setSmallIcon(R.drawable.ic_warning_black_24dp);
                mBuilder.setContentTitle("Alerta!!");
                mBuilder.setContentText(mes);
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("lat", lat);
                resultIntent.putExtra("lon", lon);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setAutoCancel(true);

                MediaPlayer.create(getApplicationContext(), R.raw.accidente).start();
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(700);
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());
            }
        });
    }

}