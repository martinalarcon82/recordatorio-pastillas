package com.example.proyecto3.receiver;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.PendingIntent;
import android.content.ComponentName;

import com.example.proyecto3.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private static final String CHANNEL_ID = "canal_medicamentos";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // 1. Verificar datos del Intent
            if (intent == null || intent.getExtras() == null) {
                Log.e(TAG, "Intent o extras nulos");
                return;
            }

            String nombreMedicamento = intent.getStringExtra("nombreMedicamento");
            String hora = intent.getStringExtra("hora");

            Intent abrirAppIntent = new Intent();
            abrirAppIntent.setComponent(new ComponentName(context.getPackageName(), "com.example.proyecto3.InterfazPrincipalActivity"));
            abrirAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    abrirAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );


            if (nombreMedicamento == null || hora == null) {
                Log.e(TAG, "Datos del medicamento incompletos");
                return;
            }

            // 2. Vibrar medio segundo
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(500);
            }

            // 3. Crear canal de notificación si es necesario
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Recordatorios de Medicamentos",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notificaciones para recordar medicamentos");
                channel.enableVibration(true);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            // 4. Notificación con vibración y sonido
            Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_pill)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_content, nombreMedicamento, hora))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(sonido)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);  // ← ¡Aquí lo agregas!

            // 5. Mostrar notificación
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            try {
                notificationManager.notify(nombreMedicamento.hashCode(), builder.build());
            } catch (SecurityException e) {
                Log.e(TAG, "Error de permisos: " + e.getMessage());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inesperado: " + e.getMessage());
        }
    }
}
