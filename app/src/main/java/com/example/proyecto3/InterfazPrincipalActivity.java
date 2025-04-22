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
import android.content.pm.PackageManager;

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

    // Declaración de vistas
    private MaterialToolbar toolbar;
    private TextView tvGreeting;
    private Button btnAddMed;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;
    private CalendarView calendarView;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Adaptador y listas
    private RecordatorioAdapter adapter;
    private List<Medicamento> listaFiltrada;
    private List<Medicamento> todosLosMedicamentos;

    // Para manejar el resultado de la actividad de agregar medicamento
    private ActivityResultLauncher<Intent> agregarMedicamentoLauncher;

    // Fecha seleccionada en el calendario
    private String fechaSeleccionada = "";

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfaz_principal);

        // Llamada a métodos de configuración e inicialización
        inicializarComponentes();
        configurarToolbar();
        configurarFirebase();
        configurarRecyclerView();
        configurarCalendarView();
        configurarBottomNavigation();
        configurarBotonAgregar();
        configurarActivityResult();
        cargarMedicamentos();
        pedirPermisoNotificaciones();
    }

    private void inicializarComponentes() {
        // Vincular vistas del layout con variables
        toolbar       = findViewById(R.id.toolbar);
        tvGreeting    = findViewById(R.id.tvGreeting);
        btnAddMed     = findViewById(R.id.btnAddMed);
        recyclerView  = findViewById(R.id.recyclerView);
        bottomNav     = findViewById(R.id.bottomNav);
        calendarView  = findViewById(R.id.calendarView);
    }

    private void configurarToolbar() {
        // Configura la toolbar y el listener del ícono de navegación
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
        // Inicializa Firebase y muestra el saludo con el correo del usuario
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        String email = mAuth.getCurrentUser() != null
                ? mAuth.getCurrentUser().getEmail()
                : "invitado";
        tvGreeting.setText("¡Hola, " + email.split("@")[0] + "!");
    }

    private void configurarRecyclerView() {
        // Inicializa listas y adaptador del RecyclerView
        listaFiltrada      = new ArrayList<>();
        todosLosMedicamentos = new ArrayList<>();
        adapter            = new RecordatorioAdapter(listaFiltrada, this::mostrarDialogoMedicamento);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void configurarCalendarView() {
        // Listener para cuando el usuario selecciona una fecha del calendario
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            fechaSeleccionada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            filtrarPorFecha(fechaSeleccionada);
        });
    }

    private void configurarBottomNavigation() {
        // Configura navegación inferior
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
        // Listener del botón para añadir medicamento
        btnAddMed.setOnClickListener(v -> lanzarAgregarMedicamento());
    }

    private void configurarActivityResult() {
        // Permite recibir el resultado de AgregarMedicamentoActivity
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
        // Abre la actividad para agregar medicamentos
        Intent intent = new Intent(this, AgregarMedicamentoActivity.class);
        agregarMedicamentoLauncher.launch(intent);
    }

    private void cargarMedicamentos() {
        // Carga medicamentos desde Firestore
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
                        programarRecordatorio(med); // Programa alarma si es futuro
                    }
                    filtrarPorFecha(fechaSeleccionada);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar medicamentos", Toast.LENGTH_SHORT).show()
                );
    }

    private void filtrarPorFecha(String fecha) {
        // Filtra los medicamentos según la fecha seleccionada
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
            // Parsear fecha y hora del medicamento
            String[] partesFecha = medicamento.getFecha().split("/");
            int day = Integer.parseInt(partesFecha[0]);
            int month = Integer.parseInt(partesFecha[1]) - 1;
            int year = Integer.parseInt(partesFecha[2]);

            String[] partesHora = medicamento.getHora().split(":");
            int hora = Integer.parseInt(partesHora[0]);
            int minuto = Integer.parseInt(partesHora[1]);

            // Crear objeto Calendar con la fecha y hora
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hora, minuto, 0);

            // Ignorar si ya pasó
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) return;

            // Cancelar alarma existente
            cancelarAlarma(medicamento);

            // Crear intent para AlarmReceiver
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("nombreMedicamento", medicamento.getNombre());
            intent.putExtra("hora", medicamento.getHora());

            // Crear requestCode único usando los datos del medicamento
            int requestCode = (medicamento.getId() + medicamento.getNombre() + medicamento.getHora() + medicamento.getFecha()).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Programar la alarma
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al programar alarma para " + medicamento.getNombre(), Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoMedicamento(Medicamento medicamento) {
        // Muestra diálogo con opciones para editar o eliminar medicamento
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
        // Muestra confirmación antes de eliminar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar")
                .setMessage("¿Eliminar " + medicamento.getNombre() + "?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarMedicamento(medicamento))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarMedicamento(Medicamento medicamento) {
        // Elimina el medicamento de Firestore y cancela la alarma
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
        // Cancela una alarma programada
        try {
            int requestCode = (medicamento.getId() + medicamento.getNombre() + medicamento.getHora() + medicamento.getFecha()).hashCode();
            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
            pendingIntent.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogoCerrarSesion() {
        // Muestra diálogo para confirmar cierre de sesión
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
        // Aquí se podría abrir la pantalla de perfil (comentado)
        // startActivity(new Intent(this, PerfilActivity.class));
    }

    private void abrirConfiguracionNotificaciones() {
        // Aquí se podría abrir la pantalla de configuración (comentado)
        // startActivity(new Intent(this, ConfiguracionActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú de la toolbar
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    private void pedirPermisoNotificaciones() {
        // Solicita permisos de notificaciones si es Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

}

