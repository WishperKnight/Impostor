package ies.carrillo.impostor.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

    // Constantes de estado del botón
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

        // Iniciar el primer turno
        if (jugadores != null && !jugadores.isEmpty()) {
            mostrarTurnoActual();
        } else {
            // Este caso ya se maneja en recuperarDatosDeIntent, pero se mantiene como backup
            Toast.makeText(this, "Error: No se pudieron cargar los jugadores.", Toast.LENGTH_LONG).show();
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

        // 1. Intentar recuperar la lista de jugadores con roles asignados
        jugadores = (List<Jugador>) intent.getSerializableExtra("JUGADORES_CON_ROLES");

        // 2. Intentar recuperar la categoría seleccionada
        categoriaSeleccionada = (Categoria) intent.getSerializableExtra("CATEGORIA_SELECCIONADA");

        // 3. Verificación de seguridad y finalización si faltan datos
        if (jugadores == null || jugadores.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo iniciar el juego. Faltan jugadores o roles.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (categoriaSeleccionada == null) {
            Toast.makeText(this, "Error: No se pudo iniciar el juego. Falta la categoría.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 💥 FIX CRÍTICO: SELECCIONAR LA PALABRA ALEATORIA AL INICIO
        // Esto inicializa getWord() y getClue() para toda la partida.
        categoriaSeleccionada.seleccionarPalabraAleatoria();

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
     * Este método solo se encarga de REVELAR el rol.
     */
    private void handleActionButton() {
        if (!isRoleRevealed) {
            // Estado 1: Revelar
            revelarRol();
            isRoleRevealed = true;

            // 1. Deshabilitar el botón de acción una vez se ha revelado
            btnAction.setText("ROL REVELADO");
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

        // Obtener el contexto para getColor, usando ContextCompat para mejor práctica
        int colorCivil = ContextCompat.getColor(this, R.color.colorPrimary);
        int colorImpostor = ContextCompat.getColor(this, R.color.icon_impostor);
        String wordToDisplay; // Variable temporal para la palabra/pista

        // 1. Lógica condicional
        if (rolActual == Roles.IMPOSTOR) {
            rolDisplay = getResources().getString(R.string.impostor); // IMPOSTOR
            wordToDisplay = categoriaSeleccionada.getClue(); // Ya no es null gracias al FIX

            tvPlayerRole.setTextColor(colorImpostor);

        } else { // Roles.CIVIL
            rolDisplay = getResources().getString(R.string.civil); // CIVIL
            wordToDisplay = categoriaSeleccionada.getWord(); // Ya no es null gracias al FIX

            tvPlayerRole.setTextColor(colorCivil);
        }

        // 2. Asignación final (se hace solo una vez)
        // Se añade un manejo de nulos por si acaso, aunque ya no debería ocurrir
        tvPlayerWord.setText(wordToDisplay != null ? wordToDisplay.toUpperCase() : "ERROR: PALABRA PERDIDA");

        // 3. Asignar el texto del rol
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
            btnAction.setEnabled(true); // Habilitar el botón "Revelar" para el nuevo jugador
            btnNextPlayer.setVisibility(View.GONE);
            // -------------------------------------

            // Ocultar contenido y mostrar máscara para el nuevo jugador
            llRoleContent.setVisibility(View.GONE);
            imgMaskCover.setVisibility(View.VISIBLE);

            mostrarTurnoActual();

        } else {
            // Todos los jugadores han visto su rol
            Toast.makeText(this, "¡Todos listos! Comienza el debate.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(JuegoActivity.this, InGameActivity.class);
            intent.putExtra("JUGADORES_CON_ROLES", (Serializable) jugadores);
            intent.putExtra("CATEGORIA_SELECCIONADA", categoriaSeleccionada); // Opcional: pasar la categoría a la siguiente Activity
            startActivity(intent);
            finish();
        }
    }

    /**
     * Actualiza el nombre del jugador que debe tomar el dispositivo.
     */
    private void mostrarTurnoActual() {
        // 1. Obtener el jugador actual
        Jugador actual = jugadores.get(currentPlayerIndex);

        // 2. Actualizar el TextView (esto se mantiene igual)
        tvCurrentPlayerName.setText("Turno de " + actual.getName().toUpperCase());

        // 3. Crear el mensaje para el diálogo
        String mensaje = "Pasa el dispositivo a " + actual.getName();

        // 4. Crear y mostrar el AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("¡Cambio de Turno!") // Título del diálogo
                .setMessage(mensaje)          // El mensaje que antes estaba en el Toast
                // Opcional: Agregar un botón para que el usuario lo cierre
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Código si se necesita hacer algo al cerrar el diálogo
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info) // Un icono, opcional
                .show();
    }
}