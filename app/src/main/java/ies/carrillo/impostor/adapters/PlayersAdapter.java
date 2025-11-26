package ies.carrillo.impostor.adapters;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.model.Jugador;
import ies.carrillo.impostor.utils.CircleTransform;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Jugador jugador = playersList.get(position);
        holder.tvPlayerName.setText(jugador.getName());

        // Carga la foto de perfil con Picasso y recorte circular
        String imageUriString = jugador.getProfileImageUri();
        if (imageUriString != null && !imageUriString.isEmpty()) {
            Picasso.get()
                    .load(Uri.parse(imageUriString))
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .transform(new CircleTransform())
                    .into(holder.imgColorIndicator);
        } else {
            Picasso.get()
                    .load(R.drawable.ic_default_profile)
                    .transform(new CircleTransform())
                    .into(holder.imgColorIndicator);
        }

        // Listener para cambiar color/foto
        holder.imgColorIndicator.setOnClickListener(v ->
                listener.onColorIndicatorClicked(holder.getAdapterPosition())
        );

        // Eliminar jugador
        holder.btnDeletePlayer.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPlayerDelete(adapterPosition);
            }
        });

        // Fondo del item según colorHex
        try {
            int colorInt = Color.parseColor(jugador.getColorHex());
            holder.clPlayerItemContainer.setBackgroundColor(colorInt);
        } catch (IllegalArgumentException e) {
            holder.clPlayerItemContainer.setBackgroundColor(Color.GRAY);
        }
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
