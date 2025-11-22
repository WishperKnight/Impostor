package ies.carrillo.impostor.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import ies.carrillo.impostor.dataBase.DataBase;
import ies.carrillo.impostor.model.Jugador;

public class AddPlayersActivity extends AppCompatActivity implements PlayersAdapter.OnPlayerActionListener {

    private TextInputEditText etPlayerName;
    private MaterialButton btnAddPlayer;
    private MaterialButton btnConfirmPlayers;
    private RecyclerView rvPlayers;
    private TextView tvListHeader;

    private PlayersAdapter adapter;
    private List<Jugador> playersList = new ArrayList<>();

    private final int MIN_PLAYERS = 3;
    private final int MAX_PLAYERS = 10;

    private final List<String> availableColors = Arrays.asList(
            "#FF0000", "#008000", "#0000FF", "#FFFF00", "#FFA500",
            "#800080", "#00FFFF", "#FFC0CB", "#A52A2A", "#FFFFFF"
    );

    // Nombres descriptivos para los colores (para el diálogo)
    private final List<String> colorNames = Arrays.asList(
            "Rojo", "Verde", "Azul", "Amarillo", "Naranja",
            "Morado", "Cian", "Rosa", "Marrón", "Blanco"
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

        onPlayerCountChanged(playersList.size());
    }

    private void loadExistingPlayers() {
        DataBase db = DataBase.getInstance();
        if (!db.getJugadores().isEmpty()) {
            // Se usa new ArrayList para obtener una copia mutable de la lista de la DB
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
        // Se pasa 'this' como listener ya que AddPlayersActivity implementa OnPlayerActionListener
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
                .orElse("#FF4081");
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
        // Se guarda la lista final en la DB
        db.setJugadores(new ArrayList<>(playersList));

        Toast.makeText(this, "Jugadores guardados: " + playersList.size(), Toast.LENGTH_SHORT).show();
        finish();
    }

    // --- Lógica del Diálogo de Selección de Color ---

    /**
     * Muestra un diálogo para que el usuario seleccione un nuevo color para el jugador.
     *
     * @param position La posición del jugador en la lista.
     */
    public void showColorPickerDialog(int position) {
        if (position < 0 || position >= playersList.size()) return;

        // Crear un array de CharSequence con los nombres de los colores para el diálogo
        final CharSequence[] items = colorNames.toArray(new CharSequence[0]);

        // Determinar el color actual del jugador para preseleccionar
        final Jugador jugadorActual = playersList.get(position);
        int currentColorIndex = availableColors.indexOf(jugadorActual.getColorHex());
        if (currentColorIndex == -1) currentColorIndex = 0; // Por si el color no está en la lista

        new AlertDialog.Builder(this)
                .setTitle("Selecciona un Color para " + jugadorActual.getName())
                .setSingleChoiceItems(items, currentColorIndex, (dialog, which) -> {
                    // Obtener el color HEX y notificar el cambio
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
            playersList.remove(position);
            adapter.notifyItemRemoved(position);
            onPlayerCountChanged(playersList.size());
        }
    }

    /**
     * Implementación de la nueva acción para cambiar el color.
     * Llamada por el adaptador o por el diálogo de color.
     */
    @Override
    public void onPlayerColorChange(int position, String newColorHex) {
        if (position >= 0 && position < playersList.size()) {
            Jugador jugador = playersList.get(position);

            // 1. Verificar si el color ya está en uso por otro jugador
            boolean colorAlreadyUsed = playersList.stream()
                    .filter(j -> j != jugador)
                    .anyMatch(j -> j.getColorHex().equalsIgnoreCase(newColorHex));

            if (colorAlreadyUsed) {
                Toast.makeText(this, "Ese color ya está en uso.", Toast.LENGTH_SHORT).show();
                // Forzar la actualización visual para mantener el color antiguo
                adapter.notifyItemChanged(position);
                return;
            }

            // 2. Asignar el nuevo color y notificar la vista
            jugador.setColorHex(newColorHex);
            adapter.notifyItemChanged(position);
        }
    }

    // Método para manejar la acción de clic en el indicador de color del adaptador
    @Override
    public void onColorIndicatorClicked(int position) {
        showColorPickerDialog(position);
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