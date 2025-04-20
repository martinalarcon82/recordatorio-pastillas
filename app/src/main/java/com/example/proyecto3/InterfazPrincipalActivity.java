package com.example.proyecto3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto3.adapter.RecordatorioAdapter;
import com.example.proyecto3.receiver.AlarmReceiver;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InterfazPrincipalActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvGreeting;
    private Button btnAddMed;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;
    private CalendarView calendarView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private RecordatorioAdapter adapter;
    private List<Medicamento> listaFiltrada;
    private List<Medicamento> todosLosMedicamentos;

    private ActivityResultLauncher<Intent> agregarMedicamentoLauncher;
    private String fechaSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfaz_principal);

        inicializarComponentes();
        configurarToolbar();
        configurarFirebase();
        configurarRecyclerView();
        configurarCalendarView();
        configurarBottomNavigation();
        configurarBotonAgregar();
        configurarActivityResult();
        cargarMedicamentos();
    }

    private void inicializarComponentes() {
        toolbar       = findViewById(R.id.toolbar);
        tvGreeting    = findViewById(R.id.tvGreeting);
        btnAddMed     = findViewById(R.id.btnAddMed);
        recyclerView  = findViewById(R.id.recyclerView);
        bottomNav     = findViewById(R.id.bottomNav);
        calendarView  = findViewById(R.id.calendarView);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> mostrarDialogoCerrarSesion());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_notifications) {
                abrirConfiguracionNotificaciones();
                return true;
            }
            return false;
        });
    }

    private void configurarFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        String email = mAuth.getCurrentUser() != null
                ? mAuth.getCurrentUser().getEmail()
                : "invitado";
        tvGreeting.setText("¡Hola, " + email.split("@")[0] + "!");
    }

    private void configurarRecyclerView() {
        listaFiltrada      = new ArrayList<>();
        todosLosMedicamentos = new ArrayList<>();
        adapter            = new RecordatorioAdapter(listaFiltrada, this::mostrarDialogoMedicamento);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void configurarCalendarView() {
        // Inicialmente vacía, se llena al seleccionar fecha
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // month viene de 0..11, ajustamos +1
            fechaSeleccionada = String.format("%02d/%02d/%04d",
                    dayOfMonth, month + 1, year);
            filtrarPorFecha(fechaSeleccionada);
        });
    }

    private void configurarBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                cargarMedicamentos();
                return true;
            } else if (id == R.id.nav_add) {
                lanzarAgregarMedicamento();
                return true;
            } else if (id == R.id.nav_profile) {
                abrirPerfilUsuario();
                return true;
            }
            return false;
        });
    }

    private void configurarBotonAgregar() {
        btnAddMed.setOnClickListener(v -> lanzarAgregarMedicamento());
    }

    private void configurarActivityResult() {
        agregarMedicamentoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarMedicamentos();
                        filtrarPorFecha(fechaSeleccionada);
                    }
                }
        );
    }

    private void lanzarAgregarMedicamento() {
        Intent intent = new Intent(this, AgregarMedicamentoActivity.class);
        agregarMedicamentoLauncher.launch(intent);
    }

    private void cargarMedicamentos() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("usuarios")
                .document(uid)
                .collection("medicamentos")
                .get()
                .addOnSuccessListener(snapshot -> {
                    todosLosMedicamentos.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Medicamento med = doc.toObject(Medicamento.class);
                        med.setId(doc.getId());
                        todosLosMedicamentos.add(med);
                        programarRecordatorio(med);
                    }
                    filtrarPorFecha(fechaSeleccionada);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar medicamentos", Toast.LENGTH_SHORT).show()
                );
    }

    private void filtrarPorFecha(String fecha) {
        listaFiltrada.clear();
        for (Medicamento m : todosLosMedicamentos) {
            if (fecha.isEmpty() || m.getFecha().equals(fecha)) {
                listaFiltrada.add(m);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void programarRecordatorio(Medicamento medicamento) {
        try {
            String[] hm = medicamento.getHora().split(":");
            int hora   = Integer.parseInt(hm[0]);
            int minuto = Integer.parseInt(hm[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hora);
            calendar.set(Calendar.MINUTE, minuto);
            calendar.set(Calendar.SECOND, 0);

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("nombreMedicamento", medicamento.getNombre());
            intent.putExtra("hora", medicamento.getHora());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    medicamento.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogoMedicamento(Medicamento medicamento) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(medicamento.getNombre())
                .setMessage("Hora: " + medicamento.getHora())
                .setPositiveButton("Editar", (d, w) -> {
                    Intent i = new Intent(this, AgregarMedicamentoActivity.class);
                    i.putExtra("medicamento", medicamento);
                    agregarMedicamentoLauncher.launch(i);
                })
                .setNegativeButton("Eliminar", (d, w) -> confirmarEliminacion(medicamento))
                .setNeutralButton("Cerrar", null)
                .show();
    }

    private void confirmarEliminacion(Medicamento medicamento) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar")
                .setMessage("¿Eliminar " + medicamento.getNombre() + "?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarMedicamento(medicamento))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarMedicamento(Medicamento medicamento) {
        String uid = mAuth.getUid();
        if (uid == null || medicamento.getId() == null) return;

        db.collection("usuarios")
                .document(uid)
                .collection("medicamentos")
                .document(medicamento.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    cancelarAlarma(medicamento);
                    Toast.makeText(this, "Medicamento eliminado", Toast.LENGTH_SHORT).show();
                    cargarMedicamentos();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                );
    }

    private void cancelarAlarma(Medicamento medicamento) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                medicamento.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    private void mostrarDialogoCerrarSesion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres salir?")
                .setPositiveButton("Sí", (d, w) -> {
                    mAuth.signOut();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void abrirPerfilUsuario() {
        // startActivity(new Intent(this, PerfilActivity.class));
    }

    private void abrirConfiguracionNotificaciones() {
        // startActivity(new Intent(this, ConfiguracionActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }
}
