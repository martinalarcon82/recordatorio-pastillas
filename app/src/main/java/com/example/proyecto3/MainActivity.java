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

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText editEmail, editPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencias de los elementos del layout
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(view -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Intentar iniciar sesión
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Inicio de sesión: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        redirectToInterfazPrincipal();
                    })
                    .addOnFailureListener(e -> {
                        // Si falla, intenta registrar
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(result -> {
                                    Toast.makeText(this, "Usuario registrado: " + email, Toast.LENGTH_SHORT).show();
                                    redirectToInterfazPrincipal();
                                })
                                .addOnFailureListener(regError -> {
                                    Toast.makeText(this, "Error en autenticación: " + regError.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
        });
    }

    private void redirectToInterfazPrincipal() {
        Intent intent = new Intent(MainActivity.this, InterfazPrincipalActivity.class);
        startActivity(intent);
        finish();  // Esto cierra la actividad de login
    }
}
