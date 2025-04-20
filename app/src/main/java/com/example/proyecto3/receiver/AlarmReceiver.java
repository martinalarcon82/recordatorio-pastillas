package com.example.proyecto3.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.proyecto3.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

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

            if (nombreMedicamento == null || hora == null) {
                Log.e(TAG, "Datos del medicamento incompletos");
                return;
            }

            // 2. Crear notificación (versión simplificada pero segura)
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "canal_medicamentos")
                    .setSmallIcon(R.drawable.ic_pill)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_content, nombreMedicamento, hora))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT); // Usamos DEFAULT para menos requisitos

            // 3. Mostrar notificación con verificación
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            try {
                notificationManager.notify(nombreMedicamento.hashCode(), builder.build());
            } catch (SecurityException e) {
                Log.e(TAG, "Error de permisos: " + e.getMessage());
                // Opcional: Lanzar intent para dirigir al usuario a configuración
                // context.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inesperado: " + e.getMessage());
        }
    }
}