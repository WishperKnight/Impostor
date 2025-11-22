package ies.carrillo.impostor.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.List;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.model.Categoria;
import ies.carrillo.impostor.model.Jugador;
import ies.carrillo.impostor.roles.Roles;

public class JuegoActivity extends AppCompatActivity {

    // Constantes de Claves de Intent (Definidas en MainActivity para consistencia)
    // Aquí usamos las cadenas literales ya que no podemos asumir la clase de constantes
    private static final String KEY_JUGADORES = "JUGADORES_CON_ROLES";
    private static final String KEY_CATEGORIA = "CATEGORIA_SELECCIONADA";

    // Vistas
    private TextView tvCurrentPlayerName;
    private TextView tvPlayerRole;
    private TextView tvPlayerWord;
    private ImageView imgMaskCover;
    private LinearLayout llRoleContent;
    private MaterialButton btnAction; // Botón "Revelar"
    private MaterialButton btnNextPlayer; // Botón "Siguiente"

    // Datos del juego
    private List<Jugador> jugadores;
    private Categoria categoriaSeleccionada;
    private int currentPlayerIndex = 0;
    private boolean isRoleRevealed = false;

    // Constante de estado del botón
    private static final String STATE_REVEAL = "REVELAR ROL Y PALABRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        recuperarDatosDeIntent();
        configurarListeners();

        if (jugadores != null && !jugadores.isEmpty()) {
            mostrarTurnoActual();
        } else {
            Toast.makeText(this, "Error: El juego no tiene jugadores válidos.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void inicializarVistas() {
        tvCurrentPlayerName = findViewById(R.id.tv_current_player_name);
        tvPlayerRole = findViewById(R.id.tv_player_role);
        tvPlayerWord = findViewById(R.id.tv_player_word);
        imgMaskCover = findViewById(R.id.img_mask_cover);
        llRoleContent = findViewById(R.id.ll_role_content);
        btnAction = findViewById(R.id.btn_action);
        btnNextPlayer = findViewById(R.id.btn_next_player);
    }

    private void recuperarDatosDeIntent() {
        Intent intent = getIntent();

        if (intent == null) {
            Toast.makeText(this, "Error: No se recibió ningún Intent válido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 1. Recuperar la lista de jugadores con roles asignados
        jugadores = (List<Jugador>) intent.getSerializableExtra(KEY_JUGADORES);

        // 2. Recuperar la categoría seleccionada
        categoriaSeleccionada = (Categoria) intent.getSerializableExtra(KEY_CATEGORIA);

        // 3. Verificación de datos
        if (jugadores == null || jugadores.isEmpty() || categoriaSeleccionada == null) {
            Toast.makeText(this, "Error: Faltan datos críticos para iniciar el juego.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 💥 FIX CRÍTICO: SELECCIONAR LA PALABRA ALEATORIA AL INICIO
        // Esto inicializa getWord() y getClue() para toda la partida.
        categoriaSeleccionada.seleccionarPalabraAleatoria();
        Log.i("Categoria",categoriaSeleccionada.getName());
    }

    private void configurarListeners() {
        btnAction.setOnClickListener(v -> handleActionButton());
        btnNextPlayer.setOnClickListener(v -> pasarSiguienteJugador());
    }

    // ******************************************************
    // LÓGICA DE REVELACIÓN DE ROLES
    // ******************************************************

    /**
     * Alterna entre el estado de oculto y revelado.
     */
    private void handleActionButton() {
        if (!isRoleRevealed) {
            revelarRol();
            isRoleRevealed = true;

            // 1. Deshabilitar el botón de acción una vez se ha revelado
            btnAction.setText(getString(R.string.btn_role_revealed)); // Usar string resource si existe
            btnAction.setEnabled(false);

            // 2. Mostrar el botón de siguiente
            btnNextPlayer.setVisibility(View.VISIBLE);
        }
    }

    private void revelarRol() {
        Jugador actual = jugadores.get(currentPlayerIndex);
        Roles rolActual = Roles.valueOf(actual.getRole()); // Obtenemos el Enum

        // Ocultar la máscara
        imgMaskCover.setVisibility(View.GONE);
        llRoleContent.setVisibility(View.VISIBLE);

        String rolDisplay;
        String wordToDisplay;

        // Cargar colores una sola vez
        int colorCivil = ContextCompat.getColor(this, R.color.colorPrimary);
        int colorImpostor = ContextCompat.getColor(this, R.color.icon_impostor);

        // 1. Lógica condicional
        if (rolActual == Roles.IMPOSTOR) {
            rolDisplay = getResources().getString(R.string.impostor);
            // La pista del impostor (solo si está habilitado en Main)
            // Aquí deberías comprobar si las pistas están habilitadas, si no, usarías un mensaje genérico.
            wordToDisplay = categoriaSeleccionada.getClue();
            tvPlayerRole.setTextColor(colorImpostor);
        } else { // Roles.CIVIL
            rolDisplay = getResources().getString(R.string.civil);
            wordToDisplay = categoriaSeleccionada.getWord();
            tvPlayerRole.setTextColor(colorCivil);
        }

        // 2. Asignación final
        tvPlayerWord.setText(wordToDisplay != null ? wordToDisplay.toUpperCase() : "ERROR");
        tvPlayerRole.setText(rolDisplay);
    }

    /**
     * Mueve al siguiente jugador o finaliza la fase de revelación.
     */
    private void pasarSiguienteJugador() {
        // Incrementamos el índice
        currentPlayerIndex++;

        if (currentPlayerIndex < jugadores.size()) {
            // Sigue habiendo jugadores, reiniciamos la vista y mostramos el siguiente turno
            isRoleRevealed = false;

            // --- REINICIO DE ESTADOS DE BOTONES ---
            btnAction.setText(STATE_REVEAL);
            btnAction.setEnabled(true);
            btnNextPlayer.setVisibility(View.GONE);
            // -------------------------------------

            // Ocultar contenido y mostrar máscara para el nuevo jugador
            llRoleContent.setVisibility(View.GONE);
            imgMaskCover.setVisibility(View.VISIBLE);

            mostrarTurnoActual();

        } else {
            // Todos los jugadores han visto su rol
            Toast.makeText(this, "¡Todos listos! Comienza el debate.", Toast.LENGTH_LONG).show();

            // Pasar los datos necesarios a InGameActivity
            Intent intent = new Intent(JuegoActivity.this, InGameActivity.class);
            intent.putExtra(KEY_JUGADORES, (Serializable) jugadores);

            // La categoría ya tiene la palabra seleccionada, la enviamos completa
            intent.putExtra(KEY_CATEGORIA, categoriaSeleccionada);

            // NOTA: Recuerda enviar también la duración del juego y si las pistas están habilitadas, si son necesarias en InGameActivity.

            startActivity(intent);
            finish();
        }
    }

    /**
     * Actualiza el nombre del jugador que debe tomar el dispositivo y muestra un diálogo de advertencia.
     */
    private void mostrarTurnoActual() {
        // 1. Obtener el jugador actual
        Jugador actual = jugadores.get(currentPlayerIndex);

        // 2. Actualizar el TextView principal
        tvCurrentPlayerName.setText(getString(R.string.turn_of, actual.getName().toUpperCase()));

        // 3. Crear y mostrar el AlertDialog (Mejorado para claridad)
        new AlertDialog.Builder(this, R.style.AlertDialogTheme) // Usar un tema si tienes uno personalizado
                .setTitle(getString(R.string.dialog_turn_change_title))
                .setMessage(getString(R.string.dialog_pass_device_to, actual.getName().toUpperCase()))
                .setCancelable(false) // Forzar que el usuario presione OK
                .setPositiveButton("OK", null) // Simplemente cierra el diálogo
                .setIcon(R.drawable.ic_player_transfer) // Usar un icono de transferencia, si existe
                .show();
    }
}