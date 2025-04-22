package com.example.proyecto3;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Intent;
import androidx.annotation.NonNull;

// Clase Medicamento que implementa Parcelable para poder pasarse entre actividades
public class Medicamento implements Parcelable {
    private String id;      // ID único del medicamento (usado por Firestore)
    private String nombre;  // Nombre del medicamento
    private String hora;    // Hora en la que debe tomarse
    private String fecha;   // Fecha en la que debe tomarse

    // 1. Constructor vacío, necesario para que Firestore pueda deserializar el objeto automáticamente
    Medicamento() {}

    // 2. Constructor que obliga a que los campos no sean nulos al crear un nuevo medicamento
    public Medicamento(@NonNull String nombre, @NonNull String hora, @NonNull String fecha) {
        this.nombre = nombre;
        this.hora = hora;
        this.fecha = fecha;
    }

    // --- Getters y Setters ---

    // Getter para ID (puede ser nulo si aún no se ha guardado en Firestore)
    public String getId() { return id; }

    // Setter para ID
    public void setId(String id) { this.id = id; }

    // Getter para nombre del medicamento
    @NonNull
    public String getNombre() { return nombre; }

    // Getter para hora
    @NonNull
    public String getHora() { return hora; }

    // Getter para fecha
    @NonNull
    public String getFecha() { return fecha; }

    // 3. Método toString útil para depurar y ver fácilmente los valores de un medicamento
    @Override
    public String toString() {
        return "Medicamento{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", hora='" + hora + '\'' +
                ", fecha='" + fecha + '\'' +
                '}';
    }

    // --- Implementación de Parcelable ---

    // 4. Constructor privado que reconstruye el objeto desde un Parcel
    protected Medicamento(Parcel in) {
        // El orden debe coincidir exactamente con writeToParcel()
        id = in.readString();
        nombre = in.readString();
        hora = in.readString();
        fecha = in.readString();
    }

    // 5. CREATOR requerido para Parcelable. Android lo usa para reconstruir objetos desde Parcels.
    public static final Creator<Medicamento> CREATOR = new Creator<Medicamento>() {
        @Override
        public Medicamento createFromParcel(Parcel in) {
            return new Medicamento(in); // Crea una instancia usando el constructor Parcel
        }

        @Override
        public Medicamento[] newArray(int size) {
            return new Medicamento[size]; // Crea un array de medicamentos
        }
    };

    // 6. Escribe los datos del objeto en un Parcel para pasarlos entre actividades
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // El orden de escritura debe coincidir con el del constructor Parcel
        dest.writeString(id);
        dest.writeString(nombre);
        dest.writeString(hora);
        dest.writeString(fecha);
    }

    // 7. Describe el contenido del Parcelable (solo útil si usas FileDescriptor, en este caso no)
    @Override
    public int describeContents() {
        return 0;
    }

    // 8. Método opcional de conveniencia para agregar este objeto como extra en un Intent
    public void putToIntent(Intent intent, String key) {
        intent.putExtra(key, this); // Guarda el objeto como extra del intent
    }
}
