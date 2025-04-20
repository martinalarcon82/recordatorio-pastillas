package com.example.proyecto3;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Intent;
import androidx.annotation.NonNull;

public class Medicamento implements Parcelable {
    private String id;
    private String nombre;
    private String hora;
    private String fecha;

    // 1. Constructor vacío con visibilidad package-private (mejor práctica para Firestore)
    Medicamento() {}

    // 2. Constructor con parámetros marcado como @NonNull
    public Medicamento(@NonNull String nombre, @NonNull String hora, @NonNull String fecha) {
        this.nombre = nombre;
        this.hora = hora;
        this.fecha = fecha;
    }

    // --- Getters y Setters mejorados ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @NonNull
    public String getNombre() { return nombre; }

    @NonNull
    public String getHora() { return hora; }

    @NonNull
    public String getFecha() { return fecha; }

    // 3. Añadido toString() para debugging
    @Override
    public String toString() {
        return "Medicamento{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", hora='" + hora + '\'' +
                ", fecha='" + fecha + '\'' +
                '}';
    }

    // --- Implementación Parcelable mejorada ---
    protected Medicamento(Parcel in) {
        // 4. Orden de lectura debe coincidir exactamente con writeToParcel
        id = in.readString();
        nombre = in.readString();
        hora = in.readString();
        fecha = in.readString();
    }

    public static final Creator<Medicamento> CREATOR = new Creator<Medicamento>() {
        @Override
        public Medicamento createFromParcel(Parcel in) {
            return new Medicamento(in);
        }

        @Override
        public Medicamento[] newArray(int size) {
            return new Medicamento[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // 5. Documentar el orden de escritura
        dest.writeString(id);
        dest.writeString(nombre);
        dest.writeString(hora);
        dest.writeString(fecha);
    }

    @Override
    public int describeContents() {
        return 0; // 6. Solo necesario si usas FileDescriptor
    }

    // 7. (Opcional) Método de conveniencia para crear Intent
    public void putToIntent(Intent intent, String key) {
        intent.putExtra(key, this);
    }
}