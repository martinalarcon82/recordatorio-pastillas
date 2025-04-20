package com.example.proyecto3.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto3.R;
import com.example.proyecto3.Medicamento;
import java.util.List;

public class RecordatorioAdapter extends RecyclerView.Adapter<RecordatorioAdapter.MedicamentoViewHolder> {

    private List<Medicamento> listaMedicamentos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Medicamento medicamento);
    }

    public RecordatorioAdapter(List<Medicamento> listaMedicamentos, OnItemClickListener listener) {
        this.listaMedicamentos = listaMedicamentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = listaMedicamentos.get(position);

        // Asignación directa de valores
        holder.tvHora.setText(medicamento.getHora());  // Hora a la izquierda
        holder.tvNombre.setText(medicamento.getNombre());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(medicamento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos.size();
    }

    // ViewHolder actualizado para el nuevo diseño
    public static class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvHora;
        TextView tvNombre;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHora = itemView.findViewById(R.id.tvHora);  // Asegúrate que estos IDs coincidan con tu XML
            tvNombre = itemView.findViewById(R.id.tvNombre);
        }
    }

    // Método para actualizar datos (mantenido por si lo necesitas)
    public void actualizarLista(List<Medicamento> nuevaLista) {
        listaMedicamentos = nuevaLista;
        notifyDataSetChanged();
    }
}