package ies.carrillo.impostor.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.model.Jugador;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder> {

    private final List<Jugador> playersList;
    private final OnPlayerActionListener listener;

    public interface OnPlayerActionListener {
        void onPlayerDelete(int position);
        void onPlayerCountChanged(int count);
        void onPlayerColorChange(int position, String newColorHex);
        void onColorIndicatorClicked(int position);
    }

    public PlayersAdapter(List<Jugador> playersList, OnPlayerActionListener listener) {
        this.playersList = playersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Jugador jugador = playersList.get(position);
        holder.tvPlayerName.setText(jugador.getName());

        // --- LÓGICA DE APLICAR EL COLOR DE FONDO Y AL INDICADOR ---
        try {
            int colorInt = Color.parseColor(jugador.getColorHex());

            // 1. Aplicar el color de fondo al ConstraintLayout completo
            // Esto es correcto y se mantiene.
            holder.clPlayerItemContainer.setBackgroundColor(colorInt);

            /*
             * CAMBIO CLAVE:
             * Se ELIMINA la línea que aplica el tinte (setColorFilter) al ImageView
             * del indicador (imgColorIndicator). Esto permite que el icono
             * mantenga su color original (ej: gris, negro, o el que tenga su vector drawable).
             *
             * holder.imgColorIndicator.setColorFilter(colorInt, PorterDuff.Mode.SRC_IN);
             */

        } catch (IllegalArgumentException e) {
            // Manejar color no válido (ej: usar Gris por defecto)
            holder.clPlayerItemContainer.setBackgroundColor(Color.GRAY);
            // Si hay un error, dejamos el icono sin tinte.
        }

        // Listener para CAMBIAR COLOR (al hacer clic en el círculo)
        holder.imgColorIndicator.setOnClickListener(v -> {
            listener.onColorIndicatorClicked(holder.getAdapterPosition());
        });

        // Lógica para eliminar jugador
        holder.btnDeletePlayer.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPlayerDelete(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlayerName;
        ImageButton btnDeletePlayer;
        ImageView imgColorIndicator;
        ConstraintLayout clPlayerItemContainer;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tv_player_name_item);
            btnDeletePlayer = itemView.findViewById(R.id.btn_delete_player);
            imgColorIndicator = itemView.findViewById(R.id.img_color_indicator);
            clPlayerItemContainer = itemView.findViewById(R.id.cl_player_item_container);
        }
    }
}