package com.example.proyecto3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Instancias de Firebase Auth y Firestore
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Elementos del layout
    private EditText editEmail, editPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase Authentication y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtener referencias a los elementos de la interfaz
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Listener para el botón de login
        btnLogin.setOnClickListener(view -> {
            // Obtener texto ingresado por el usuario
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // Verificar que los campos no estén vacíos
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Intentar iniciar sesión con Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        // Si inicia sesión con éxito
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Inicio de sesión: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        redirectToInterfazPrincipal(); // Ir a la siguiente pantalla
                    })
                    .addOnFailureListener(e -> {
                        // Si falla el login, se intenta registrar un nuevo usuario
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(result -> {
                                    // Registro exitoso
                                    Toast.makeText(this, "Usuario registrado: " + email, Toast.LENGTH_SHORT).show();
                                    redirectToInterfazPrincipal(); // Ir a la siguiente pantalla
                                })
                                .addOnFailureListener(regError -> {
                                    // Falla al registrar
                                    Toast.makeText(this, "Error en autenticación: " + regError.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
        });
    }

    // Método para pasar a la actividad principal después de login/registro
    private void redirectToInterfazPrincipal() {
        Intent intent = new Intent(MainActivity.this, InterfazPrincipalActivity.class);
        startActivity(intent);
        finish();  // Cierra la pantalla de login para que el usuario no vuelva atrás
    }
}
