package ies.carrillo.impostor.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // ¡Importante para el indicador de color!
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.model.Jugador;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder> {

    private final List<Jugador> playersList;
    private final OnPlayerActionListener listener;

    /**
     * Interfaz para manejar las acciones que deben ser manejadas por la Activity.
     * Incluye las nuevas acciones relacionadas con el color.
     */
    public interface OnPlayerActionListener {
        void onPlayerDelete(int position);

        void onPlayerCountChanged(int count);

        void onPlayerColorChange(int position, String newColorHex); // Necesario para la Activity, aunque aquí no se use directamente.

        void onColorIndicatorClicked(int position); // Nuevo: Llama a la Activity para mostrar el diálogo.
    }

    public PlayersAdapter(List<Jugador> playersList, OnPlayerActionListener listener) {
        this.playersList = playersList;
        this.listener = listener;
        // Notificamos el conteo inicial (Mejor que la Activity lo llame tras cargar la lista)
        // listener.onPlayerCountChanged(playersList.size()); // Se puede quitar si la Activity lo gestiona al inicio
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos el layout corregido: R.layout.list_item_player
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Jugador jugador = playersList.get(position);
        holder.tvPlayerName.setText(jugador.getName());

        // --- LÓGICA DE MOSTRAR Y CAMBIAR COLOR ---
        try {
            int colorInt = Color.parseColor(jugador.getColorHex());

            // Aplicar el tinte al ImageView del círculo
            holder.imgColorIndicator.setColorFilter(colorInt, PorterDuff.Mode.SRC_IN);

        } catch (IllegalArgumentException e) {
            // Manejar color no válido (ej: usar Gris por defecto)
            holder.imgColorIndicator.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }

        // Listener para CAMBIAR COLOR (al hacer clic en el círculo)
        holder.imgColorIndicator.setOnClickListener(v -> {
            // Llama al nuevo método de la Activity para abrir el diálogo de selección
            listener.onColorIndicatorClicked(holder.getAdapterPosition());
        });


        // Lógica para eliminar jugador
        holder.btnDeletePlayer.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                // La Activity manejará la eliminación de la lista y la notificación al adaptador
                listener.onPlayerDelete(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    /**
     * NOTA: El método removePlayer en el Adapter no es necesario
     * si la Activity maneja la lista de datos (playersList.remove) y luego
     * llama a notifyItemRemoved().
     */

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlayerName;
        ImageButton btnDeletePlayer;
        ImageView imgColorIndicator; // ¡Nuevo elemento para el color!

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asegúrate de que los IDs coincidan con list_item_player.xml
            tvPlayerName = itemView.findViewById(R.id.tv_player_name_item);
            btnDeletePlayer = itemView.findViewById(R.id.btn_delete_player);
            imgColorIndicator = itemView.findViewById(R.id.img_color_indicator);
        }
    }
}