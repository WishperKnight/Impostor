package ies.carrillo.impostor.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.adapters.PlayersAdapter;
import ies.carrillo.impostor.adapters.ProfileImageAdapter;
import ies.carrillo.impostor.dataBase.DataBase;
import ies.carrillo.impostor.model.Jugador;
import ies.carrillo.impostor.utils.ProfileImageSelectionListener;

// IMPLEMENTACIÓN DE LA INTERFAZ DE SELECCIÓN DE IMAGEN
public class AddPlayersActivity extends AppCompatActivity implements PlayersAdapter.OnPlayerActionListener, ProfileImageSelectionListener {

    private TextInputEditText etPlayerName;
    private MaterialButton btnAddPlayer;
    private MaterialButton btnConfirmPlayers;
    private RecyclerView rvPlayers;
    private TextView tvListHeader;

    private PlayersAdapter adapter;
    private List<Jugador> playersList = new ArrayList<>();

    private final int MIN_PLAYERS = 3;
    private final int MAX_PLAYERS = 20;

    // --- VARIABLES DE FOTO DE PERFIL ---
    private int playerToEditPosition = RecyclerView.NO_POSITION;
    private ActivityResultLauncher<Intent> imagePickerLauncher; // NECESARIO

    // Lista de IDs de recursos predefinidos
    private final List<Integer> predefinedImageIds = Arrays.asList(
            R.drawable.img_avatar_buttler,
            R.drawable.img_avatar_detective,
            R.drawable.img_avatar_engenieer,
            R.drawable.img_avatar_heir,
            R.drawable.img_avatar_maid,
            R.drawable.img_avatar_millonaire
    );
    // ---------------------------------------------

    private final List<String> availableColors = Arrays.asList(
            // 1. 🟥 ROJO
            "#FFB6C1",
            // ... (otros colores) ...
            "#90EE90", "#ADD8E6", "#FFFACD", "#FFDAB9", "#CBA3D3",
            "#AFEEEE", "#FFD1DC", "#B8860B", "#A9A9A9"
    );

    private final List<String> colorNames = Arrays.asList(
            "Rojo", "Verde", "Azul", "Amarillo", "Naranja",
            "Morado", "Cian", "Rosa", "Marrón", "Gris"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_players);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadExistingPlayers();

        inicializarVistas();
        configurarRecyclerView();
        configurarListeners();
        setupImagePickerLauncher(); // 💥 Descomentado: Inicializa el launcher para la Galería

        onPlayerCountChanged(playersList.size());
    }

    // Método auxiliar para convertir ID de recurso a String URI
    private String getUriFromResourceId(int resourceId) {
        return "android.resource://" + getPackageName() + "/" + resourceId;
    }

    /*
     * LÓGICA DE SELECCIÓN DE GALERÍA (ACTION_OPEN_DOCUMENT)
     */
    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (playerToEditPosition != RecyclerView.NO_POSITION) {
                            Uri imageUri = result.getData().getData();
                            if (imageUri != null) {

                                // 💥 Persistir el permiso URI (CRÍTICO para URIs de Galería)
                                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

                                try {
                                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                                } catch (SecurityException e) {
                                    Log.e("AddPlayersActivity", "Error al tomar permiso persistente de URI: " + e.getMessage());
                                }

                                // Guardar la URI como String en el modelo
                                String uriString = imageUri.toString();
                                Jugador jugador = playersList.get(playerToEditPosition);
                                jugador.setProfileImageUri(uriString);

                                adapter.notifyItemChanged(playerToEditPosition);
                                Toast.makeText(this, "Foto de " + jugador.getName() + " actualizada.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    playerToEditPosition = RecyclerView.NO_POSITION; // Resetear posición
                }
        );
    }

    /**
     * Abre el selector de imágenes de la galería del dispositivo.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        // Añadir flags de persistencia para mantener el acceso
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }
    /* ----------------------------------------------------------------- */

    // --- LÓGICA DE SELECCIÓN DE IMAGEN PREDEFINIDA ---

    /**
     * Muestra un diálogo con un RecyclerView para seleccionar una de las 6 imágenes predefinidas.
     */
    private void showPredefinedImageDialog(int position) {
        playerToEditPosition = position; // Guarda la posición a editar

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        ProfileImageAdapter adapter = new ProfileImageAdapter(predefinedImageIds, this);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Selecciona el Avatar")
                .setView(recyclerView)
                .create();

        // Esto permite que el adaptador cierre el diálogo al hacer clic en un item
        adapter.setDialogRef(dialog);
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    /**
     * Implementación de la interfaz para guardar la imagen predefinida seleccionada.
     */
    @Override
    public void onProfileImageSelected(int drawableId) {
        if (playerToEditPosition != RecyclerView.NO_POSITION) {

            // Si el adaptador ya cerró el diálogo, solo actualizamos el modelo

            // 1. Convertir el ID de recurso (int) a URI (String)
            String profileUri = getUriFromResourceId(drawableId);

            // 2. Guardar la URI en el modelo Jugador
            Jugador jugador = playersList.get(playerToEditPosition);
            jugador.setProfileImageUri(profileUri);

            // 3. Notificar al adaptador y limpiar
            adapter.notifyItemChanged(playerToEditPosition);
            Toast.makeText(this, "Avatar de " + jugador.getName() + " actualizado.", Toast.LENGTH_SHORT).show();
        }
        playerToEditPosition = RecyclerView.NO_POSITION; // Resetear posición
    }

    // --- FIN LÓGICA DE SELECCIÓN DE IMAGEN PREDEFINIDA ---


    private void loadExistingPlayers() {
        DataBase db = DataBase.getInstance();
        if (!db.getJugadores().isEmpty()) {
            this.playersList = new ArrayList<>(db.getJugadores());
        }
    }

    private void inicializarVistas() {
        etPlayerName = findViewById(R.id.et_player_name);
        btnAddPlayer = findViewById(R.id.btn_add_player);
        btnConfirmPlayers = findViewById(R.id.btn_confirm_players);
        rvPlayers = findViewById(R.id.rv_players);
        tvListHeader = findViewById(R.id.tv_list_header);
    }

    private void configurarRecyclerView() {
        rvPlayers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlayersAdapter(playersList, this);
        rvPlayers.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnAddPlayer.setOnClickListener(v -> addPlayer());
        btnConfirmPlayers.setOnClickListener(v -> confirmPlayers());
    }

    private String getNextAvailableColor() {
        List<String> usedColors = playersList.stream()
                .map(Jugador::getColorHex)
                .collect(Collectors.toList());

        return availableColors.stream()
                .filter(color -> !usedColors.contains(color))
                .findFirst()
                .orElse("#A9A9A9"); // Color por defecto si no quedan
    }

    private void addPlayer() {
        String name = etPlayerName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (playersList.size() >= MAX_PLAYERS) {
            Toast.makeText(this, "Máximo de " + MAX_PLAYERS + " jugadores alcanzado.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Jugador j : playersList) {
            if (j.getName().equalsIgnoreCase(name)) {
                Toast.makeText(this, "El jugador " + name + " ya está en la lista.", Toast.LENGTH_SHORT).show();
                etPlayerName.setText("");
                return;
            }
        }

        String color = getNextAvailableColor();
        Jugador nuevoJugador = new Jugador(name);
        nuevoJugador.setColorHex(color);
        // Asignar una imagen por defecto de la lista predefinida al añadir.
        if (!predefinedImageIds.isEmpty()) {
            String defaultUri = getUriFromResourceId(predefinedImageIds.get(playersList.size() % predefinedImageIds.size()));
            nuevoJugador.setProfileImageUri(defaultUri);
        }

        playersList.add(nuevoJugador);

        adapter.notifyItemInserted(playersList.size() - 1);
        rvPlayers.scrollToPosition(playersList.size() - 1);

        etPlayerName.setText("");

        onPlayerCountChanged(playersList.size());
    }

    private void confirmPlayers() {
        if (playersList.size() < MIN_PLAYERS) {
            Toast.makeText(this, "Necesitas al menos " + MIN_PLAYERS + " jugadores.", Toast.LENGTH_SHORT).show();
            return;
        }

        DataBase db = DataBase.getInstance();
        db.setJugadores(new ArrayList<>(playersList));

        Toast.makeText(this, "Jugadores guardados: " + playersList.size(), Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Muestra un diálogo de opciones para elegir el método de cambio de foto.
     */
    private void showProfileOptionsDialog(int position) {
        if (position < 0 || position >= playersList.size()) return;

        final Jugador jugadorActual = playersList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Perfil de " + jugadorActual.getName())
                // 💥 Opción AÑADIDA: Elegir de Galería
                .setItems(new CharSequence[]{"Cambiar Color", "Cambiar Foto (Avatares)", "Elegir de Galería"}, (dialog, which) -> {
                    if (which == 0) {
                        showColorPickerDialog(position);
                    } else if (which == 1) {
                        showPredefinedImageDialog(position);
                    } else {
                        // Opción 2: Elegir de Galería
                        playerToEditPosition = position;
                        openImagePicker(); // Llama al método que usa el ActivityResultLauncher
                    }
                })
                .show();
    }


    // --- Lógica del Diálogo de Selección de Color (sin cambios) ---

    public void showColorPickerDialog(int position) {
        if (position < 0 || position >= playersList.size()) return;

        final CharSequence[] items = colorNames.toArray(new CharSequence[0]);
        final Jugador jugadorActual = playersList.get(position);
        int currentColorIndex = availableColors.indexOf(jugadorActual.getColorHex());
        if (currentColorIndex == -1) currentColorIndex = 0;

        new AlertDialog.Builder(this)
                .setTitle("Selecciona un Color para " + jugadorActual.getName())
                .setSingleChoiceItems(items, currentColorIndex, (dialog, which) -> {
                    String selectedColorHex = availableColors.get(which);
                    onPlayerColorChange(position, selectedColorHex);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    // --- Implementación de PlayersAdapter.OnPlayerActionListener ---

    @Override
    public void onPlayerDelete(int position) {
        if (position >= 0 && position < playersList.size()) {
            // ... (Lógica de eliminación)
            playersList.remove(position);
            adapter.notifyItemRemoved(position);
            onPlayerCountChanged(playersList.size());
        }
    }

    @Override
    public void onPlayerColorChange(int position, String newColorHex) {
        if (position >= 0 && position < playersList.size()) {
            Jugador jugador = playersList.get(position);

            boolean colorAlreadyUsed = playersList.stream()
                    .filter(j -> j != jugador)
                    .anyMatch(j -> j.getColorHex().equalsIgnoreCase(newColorHex));

            if (colorAlreadyUsed) {
                Toast.makeText(this, "Ese color ya está en uso.", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
                return;
            }

            jugador.setColorHex(newColorHex);
            adapter.notifyItemChanged(position);
        }
    }

    // Método para manejar el clic: Ahora muestra un diálogo de opciones
    @Override
    public void onColorIndicatorClicked(int position) {
        showProfileOptionsDialog(position);
    }


    @Override
    public void onPlayerCountChanged(int count) {
        tvListHeader.setText("Jugadores Añadidos (" + count + "/" + MAX_PLAYERS + ")");

        if (count >= MIN_PLAYERS) {
            btnConfirmPlayers.setEnabled(true);
            btnConfirmPlayers.setAlpha(1.0f);
        } else {
            btnConfirmPlayers.setEnabled(false);
            btnConfirmPlayers.setAlpha(0.5f);
        }
    }
}