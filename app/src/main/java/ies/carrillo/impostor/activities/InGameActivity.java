package ies.carrillo.impostor.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.adapters.PlayersAdapter;
import ies.carrillo.impostor.model.Jugador;
import ies.carrillo.impostor.roles.Roles;

public class InGameActivity extends AppCompatActivity implements PlayersAdapter.OnPlayerActionListener {

    // Constantes de Intent (Buenas prácticas)
    public static final String EXTRA_PLAYERS_WITH_ROLES = "JUGADORES_CON_ROLES";
    public static final String EXTRA_DURATION_MINUTES = "DURACION_MINUTOS";

    // UI
    private TextView tvTimer;
    private RecyclerView rvActivePlayers;
    private LinearLayout overlayEndGame;
    private TextView tvEndGameTitle;
    private TextView tvEndGameSubtitle;
    private MaterialButton btnBackToMenu;

    // Lógica
    private PlayersAdapter adapter;
    private List<Jugador> activePlayers = new ArrayList<>();
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_in_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        recuperarDatosIntent();
        configurarRecyclerView();
        startTimer();
    }

    private void inicializarVistas() {
        tvTimer = findViewById(R.id.tv_timer);
        rvActivePlayers = findViewById(R.id.rv_active_players);

        // Pantalla de Fin de Juego
        overlayEndGame = findViewById(R.id.overlay_end_game);
        tvEndGameTitle = findViewById(R.id.tv_end_game_title);
        tvEndGameSubtitle = findViewById(R.id.tv_end_game_subtitle);
        btnBackToMenu = findViewById(R.id.btn_back_to_menu);

        btnBackToMenu.setOnClickListener(v -> {
            // Volver al menú principal y limpiar la pila de actividades
            Intent intent = new Intent(InGameActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void recuperarDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // 1. Recuperar Jugadores usando la constante
            List<Jugador> receivedPlayers = (List<Jugador>) intent.getSerializableExtra(EXTRA_PLAYERS_WITH_ROLES);
            if (receivedPlayers != null) {
                activePlayers.clear();
                activePlayers.addAll(receivedPlayers);
            }

            // 2. Recuperar Duración usando la constante
            int minutos = intent.getIntExtra(EXTRA_DURATION_MINUTES, 5);
            timeLeftInMillis = (long) minutos * 60 * 1000;
        }
    }

    private void configurarRecyclerView() {
        rvActivePlayers.setLayoutManager(new LinearLayoutManager(this));
        // Usamos el mismo adaptador que antes, esta actividad implementa la interfaz para la eliminación/votación
        adapter = new PlayersAdapter(activePlayers, this);
        rvActivePlayers.setAdapter(adapter);
    }

    private void startTimer() {
        if (timeLeftInMillis <= 0) {
            Toast.makeText(this, "Duración del juego no válida, usando 5 minutos.", Toast.LENGTH_SHORT).show();
            timeLeftInMillis = 5 * 60 * 1000;
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                tvTimer.setText("¡TIEMPO!");
                tvTimer.setTextColor(Color.RED);
                Toast.makeText(InGameActivity.this, "El tiempo ha terminado. ¡Votad!", Toast.LENGTH_LONG).show();
                // Si el tiempo se acaba y hay suficientes impostores, ganan automáticamente (opcional).
                // checkWinCondition();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeFormatted);
    }

    // ****************************************************************
    // IMPLEMENTACIÓN DE PlayersAdapter.OnPlayerActionListener
    // ****************************************************************

    @Override
    public void onPlayerDelete(int position) {
        // En esta pantalla, el botón "Borrar" o "Click" actúa como ELIMINAR/VOTAR
        confirmarEliminacion(position);
    }

    @Override
    public void onPlayerCountChanged(int count) {
        // Método de la interfaz, no crítico en esta Activity.
        // Se podría usar para actualizar un contador global de jugadores.
    }

    /**
     * IMPORTANTE: Implementación necesaria del método de la interfaz,
     * aunque no se use en esta Activity (el color ya está fijado).
     */
    @Override
    public void onPlayerColorChange(int position, String newColorHex) {
        // No implementado ni requerido en el juego.
    }

    /**
     * IMPORTANTE: Implementación necesaria del método de la interfaz,
     * aunque no se use en esta Activity (el color ya está fijado).
     */
    @Override
    public void onColorIndicatorClicked(int position) {
        // No implementado ni requerido en el juego.
        // Opcional: Mostrar un Toast informando que no se puede cambiar el color.
        // Toast.makeText(this, "No se puede cambiar el color durante el juego.", Toast.LENGTH_SHORT).show();
    }

    // ****************************************************************
    // LÓGICA DE ELIMINACIÓN Y FIN DE JUEGO
    // ****************************************************************

    private void confirmarEliminacion(int position) {
        Jugador jugador = activePlayers.get(position);

        new AlertDialog.Builder(this)
                .setTitle("¿VOTACIÓN FINAL?")
                .setMessage("¿Estáis seguros de ELIMINAR a " + jugador.getName().toUpperCase(Locale.getDefault()) + "? ¡No hay vuelta atrás!")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    eliminarJugador(position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarJugador(int position) {
        Jugador eliminado = activePlayers.get(position);

        // Mostrar su rol al ser eliminado
        String rol = (eliminado.getRole().equals(Roles.IMPOSTOR)) ? "IMPOSTOR" : "CIVIL";
        Toast.makeText(this, eliminado.getName() + " era " + rol, Toast.LENGTH_LONG).show();

        // Eliminar de la lista y notificar al adaptador
        activePlayers.remove(position);
        adapter.notifyItemRemoved(position);

        // Comprobar victoria
        checkWinCondition();
    }

    private void checkWinCondition() {
        long impostorCount = 0;

        // Contar el número de impostores activos
        for (Jugador j : activePlayers) {
            if (j.getRole().equals(Roles.IMPOSTOR)) {
                impostorCount++;
            }
        }

        int totalPlayers = activePlayers.size();
        long civilCount = totalPlayers - impostorCount;

        // 1. VICTORIA DE LOS CIVILES 🟢
        // Condición: No queda ningún impostor.
        if (impostorCount == 0) {
            showEndGame(true, "¡VICTORIA de los Civiles! 🏆", "Todos los impostores han sido eliminados.");
            return;
        }

        // 2. VICTORIA DEL IMPOSTOR 🔴
        // Condición: Quedan tantos o MÁS impostores que civiles (ImpostorCount >= CivilCount)
        if (impostorCount >= civilCount) {
            showEndGame(false, "¡VICTORIA del Impostor! 🔪", "Los impostores han superado en número a los civiles.");
            return;
        }

        // Si no se cumple ninguna condición, el juego continúa.
    }

    private void showEndGame(boolean civilWin, String title, String subtitle) {
        // Detener timer
        if (countDownTimer != null) countDownTimer.cancel();

        // Bloquear la interacción con la lista (no más eliminaciones)
        // Opcional: Podrías usar rvActivePlayers.setEnabled(false) si fuera necesario.

        // Configurar la pantalla final
        overlayEndGame.setVisibility(View.VISIBLE);

        int overlayColor = civilWin ?
                Color.argb(190, 76, 175, 80) : // Verde semitransparente (más opaco)
                Color.argb(190, 244, 67, 54);  // Rojo semitransparente (más opaco)

        overlayEndGame.setBackgroundColor(overlayColor);

        tvEndGameTitle.setText(title);
        tvEndGameSubtitle.setText(subtitle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}