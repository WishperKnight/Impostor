package ies.carrillo.impostor.adapters;

import android.graphics.Color;
import android.net.Uri; // Necesario para parsear la URI de la imagen
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
        // Asegúrate de que este layout (player_item.xml) existe y contiene la ImageView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Jugador jugador = playersList.get(position);
        holder.tvPlayerName.setText(jugador.getName());

        // --- LÓGICA DE FOTO DE PERFIL (NUEVO) ---
        String imageUriString = jugador.getProfileImageUri();

        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                // Si existe una URI, la cargamos en el ImageView.
                holder.imgColorIndicator.setImageURI(Uri.parse(imageUriString));
                // Aseguramos que el ImageView tiene el fondo de forma circular para el borde
                holder.imgColorIndicator.setBackgroundResource(R.drawable.shape_circle_stroke);
            } catch (Exception e) {
                // Manejar errores de URI, si el archivo ha sido eliminado
                holder.imgColorIndicator.setImageResource(R.drawable.ic_default_profile);
                holder.imgColorIndicator.setBackgroundResource(R.drawable.shape_circle_stroke);
            }
        } else {
            // Si no hay URI, mostramos la imagen por defecto.
            holder.imgColorIndicator.setImageResource(R.drawable.ic_default_profile);
            // Aseguramos que el ImageView tiene el fondo de forma circular para el borde
            holder.imgColorIndicator.setBackgroundResource(R.drawable.shape_circle_stroke);
        }
        // ------------------------------------------

        // --- LÓGICA DE COLOR DE FONDO ---
        try {
            int colorInt = Color.parseColor(jugador.getColorHex());
            holder.clPlayerItemContainer.setBackgroundColor(colorInt);
        } catch (IllegalArgumentException e) {
            holder.clPlayerItemContainer.setBackgroundColor(Color.GRAY);
        }

        // Listener para CAMBIAR COLOR / FOTO (al hacer clic en el círculo)
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
        ImageView imgColorIndicator; // Ahora muestra la foto de perfil
        ConstraintLayout clPlayerItemContainer;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tv_player_name_item);
            btnDeletePlayer = itemView.findViewById(R.id.btn_delete_player);
            // Asegúrate de que el ID es correcto según tu layout
            imgColorIndicator = itemView.findViewById(R.id.img_color_indicator);
            clPlayerItemContainer = itemView.findViewById(R.id.cl_player_item_container);
        }
    }
}