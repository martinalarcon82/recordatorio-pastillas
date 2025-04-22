package com.example.proyecto3;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto3.receiver.AlarmReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AgregarMedicamentoActivity extends AppCompatActivity {

    // Elementos de la interfaz
    private EditText etNombre, etHora, etFecha;
    private Button btnGuardar;

    // Instancias de Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Si se está editando un medicamento, se carga aquí
    private Medicamento medicamentoExistente = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_medicamento);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etHora = findViewById(R.id.etHora);
        etFecha = findViewById(R.id.etFecha);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Verificar si estamos editando un medicamento
        if (getIntent().hasExtra("medicamento")) {
            medicamentoExistente = (Medicamento) getIntent().getParcelableExtra("medicamento");
            if (medicamentoExistente != null) {
                cargarDatosExistente(); // Cargar datos en los campos
            }
        }

        // Configurar eventos (listeners)
        configurarListeners();
    }

    // Cargar datos del medicamento que se está editando
    private void cargarDatosExistente() {
        etNombre.setText(medicamentoExistente.getNombre());
        etHora.setText(medicamentoExistente.getHora());
        etFecha.setText(medicamentoExistente.getFecha());
    }

    // Configurar clics para botones y campos
    private void configurarListeners() {
        etHora.setOnClickListener(v -> mostrarTimePicker());   // Mostrar selector de hora
        etFecha.setOnClickListener(v -> mostrarDatePicker());  // Mostrar selector de fecha
        btnGuardar.setOnClickListener(v -> guardarMedicamento()); // Guardar al presionar
    }

    // Mostrar diálogo para seleccionar hora
    private void mostrarTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String hora = String.format("%02d:%02d", hourOfDay, minute);
                    etHora.setText(hora); // Formatear y mostrar la hora seleccionada
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true).show();
    }

    // Mostrar diálogo para seleccionar fecha
    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    etFecha.setText(fecha); // Formatear y mostrar la fecha seleccionada
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Guardar o actualizar medicamento
    private void guardarMedicamento() {
        String nombre = etNombre.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();

        if (validarCampos(nombre, hora, fecha)) return; // Validar campos vacíos

        Medicamento medicamento = new Medicamento(nombre, hora, fecha);
        guardarEnFirestore(medicamento); // Guardar en Firestore
        programarAlarma(nombre, hora);   // Programar alarma para la hora indicada
        setResult(RESULT_OK);            // Devolver resultado a la actividad anterior
        finish();                        // Cerrar actividad
    }

    // Validar que los campos no estén vacíos
    private boolean validarCampos(String nombre, String hora, String fecha) {
        if (nombre.isEmpty() || hora.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    // Guardar medicamento en Firebase Firestore
    private void guardarEnFirestore(Medicamento medicamento) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        if (medicamentoExistente != null && medicamentoExistente.getId() != null) {
            // Actualizar medicamento existente
            db.collection("usuarios")
                    .document(uid)
                    .collection("medicamentos")
                    .document(medicamentoExistente.getId())
                    .set(medicamento)
                    .addOnCompleteListener(task -> manejarResultado(task.isSuccessful(), "actualizado"));
        } else {
            // Guardar nuevo medicamento
            db.collection("usuarios")
                    .document(uid)
                    .collection("medicamentos")
                    .add(medicamento)
                    .addOnCompleteListener(task -> manejarResultado(task.isSuccessful(), "guardado"));
        }
    }

    // Mostrar mensaje según si se guardó con éxito o no
    private void manejarResultado(boolean exito, String accion) {
        if (exito) {
            Toast.makeText(this, "Medicamento " + accion, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent().putExtra("refresh", true)); // Devolver que se actualice la lista
            finish();
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    // Programar una alarma para recordar tomar el medicamento
    private void programarAlarma(String nombreMedicamento, String hora) {
        try {
            // Separar hora y minutos desde el formato HH:mm
            String[] partesHora = hora.split(":");
            int hour = Integer.parseInt(partesHora[0]);
            int minute = Integer.parseInt(partesHora[1]);

            // Crear objeto Calendar con la hora seleccionada
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // Si la hora ya pasó, se programa para el día siguiente
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Crear Intent para AlarmReceiver (quien mostrará la notificación)
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("nombreMedicamento", nombreMedicamento);
            intent.putExtra("hora", hora);

            // Crear PendingIntent con código único basado en el nombre y la hora
            int requestCode = (nombreMedicamento + hora).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Obtener el AlarmManager y configurar la alarma
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al programar alarma", Toast.LENGTH_SHORT).show();
        }
    }
}
