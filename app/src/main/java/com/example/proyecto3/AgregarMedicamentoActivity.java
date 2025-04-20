package com.example.proyecto3;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AgregarMedicamentoActivity extends AppCompatActivity {

    private EditText etNombre, etHora, etFecha;
    private Button btnGuardar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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

        // Verificar si estamos editando
        if (getIntent().hasExtra("medicamento")) {
            medicamentoExistente = (Medicamento) getIntent().getParcelableExtra("medicamento");
            if (medicamentoExistente != null) {
                cargarDatosExistente();
            }
        }

        // Configurar listeners
        configurarListeners();
    }

    private void cargarDatosExistente() {
        etNombre.setText(medicamentoExistente.getNombre());
        etHora.setText(medicamentoExistente.getHora());
        etFecha.setText(medicamentoExistente.getFecha());
    }

    private void configurarListeners() {
        etHora.setOnClickListener(v -> mostrarTimePicker());
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarMedicamento());
    }

    private void mostrarTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String hora = String.format("%02d:%02d", hourOfDay, minute);
                    etHora.setText(hora);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true).show();
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    etFecha.setText(fecha);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void guardarMedicamento() {
        String nombre = etNombre.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();

        if (validarCampos(nombre, hora, fecha)) return;

        Medicamento medicamento = new Medicamento(nombre, hora, fecha);
        guardarEnFirestore(medicamento);
        setResult(RESULT_OK);
        finish();
    }

    private boolean validarCampos(String nombre, String hora, String fecha) {
        if (nombre.isEmpty() || hora.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void guardarEnFirestore(Medicamento medicamento) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        if (medicamentoExistente != null && medicamentoExistente.getId() != null) {
            // Actualización
            db.collection("usuarios")
                    .document(uid)
                    .collection("medicamentos")
                    .document(medicamentoExistente.getId())
                    .set(medicamento)
                    .addOnCompleteListener(task -> manejarResultado(task.isSuccessful(), "actualizado"));
        } else {
            // Creación
            db.collection("usuarios")
                    .document(uid)
                    .collection("medicamentos")
                    .add(medicamento)
                    .addOnCompleteListener(task -> manejarResultado(task.isSuccessful(), "guardado"));
        }
    }

    private void manejarResultado(boolean exito, String accion) {
        if (exito) {
                Toast.makeText(this, "Medicamento " + accion, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent().putExtra("refresh", true)); // <- ¡Cambio clave!
            finish();
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}