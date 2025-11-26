package ies.carrillo.impostor.adapters; // Ajusta el paquete si es necesario

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Importante para la referencia del diálogo
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.utils.ProfileImageSelectionListener;

public class ProfileImageAdapter extends RecyclerView.Adapter<ProfileImageAdapter.ImageViewHolder> {

    private final List<Integer> imageIds;
    private final ProfileImageSelectionListener listener;
    private AlertDialog dialogRef; // 💥 REFERENCIA AÑADIDA: Referencia al diálogo contenedor

    /**
     * Constructor del adaptador.
     * @param imageIds Lista de IDs de recursos (R.drawable.xxx) de las imágenes.
     * @param listener Interfaz para comunicar la selección a la Activity.
     */
    public ProfileImageAdapter(List<Integer> imageIds, ProfileImageSelectionListener listener) {
        this.imageIds = imageIds;
        this.listener = listener;
    }

    // 💥 MÉTODO AÑADIDO: Permite a la Activity pasar la referencia del diálogo
    public void setDialogRef(AlertDialog dialog) {
        this.dialogRef = dialog;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout del item_profile_image.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        int drawableId = imageIds.get(position);

        // Usar Picasso para cargar el recurso drawable
        Picasso.get()
                .load(drawableId) // Carga directamente el ID del recurso
                .fit()
                .centerCrop()
                .into(holder.imageView);

        // Listener de clic para devolver el ID de recurso a la Activity
        holder.itemView.setOnClickListener(v -> {
            listener.onProfileImageSelected(drawableId);

            // 💥 LÓGICA AÑADIDA: Cerrar el diálogo inmediatamente tras la selección
            if (dialogRef != null && dialogRef.isShowing()) {
                dialogRef.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageIds.size();
    }

    /**
     * ViewHolder para mantener las referencias de las vistas del item.
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            // Referencia al ImageView definido en item_profile_image.xml
            imageView = itemView.findViewById(R.id.iv_profile_item);
        }
    }
}