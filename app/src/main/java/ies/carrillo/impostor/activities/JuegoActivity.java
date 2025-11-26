package ies.carrillo.impostor.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

    private static final String KEY_JUGADORES = "JUGADORES_CON_ROLES";
    private static final String KEY_CATEGORIA = "CATEGORIA_SELECCIONADA";

    // Vistas
    private TextView tvCurrentPlayerName;
    private TextView tvPlayerRole;
    private TextView tvPlayerWord;
    private ImageView imgMaskCover;
    private TextView tvInstruction;
    private ImageView imgLockIcon;
    private LinearLayout llRoleContent;
    private MaterialButton btnAction;
    private MaterialButton btnNextPlayer;

    // Gestos y Constantes
    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    // Datos del juego
    private List<Jugador> jugadores;
    private Categoria categoriaSeleccionada;
    private int currentPlayerIndex = 0;
    private boolean isRoleRevealed = false;

    // Constante de estado del botón
    // Asegúrate de actualizar el string en strings.xml si es necesario
    private static final String STATE_REVEAL = "PULSA O DESLIZA PARA REVELAR";
    private static final String STATE_REVEALED = "ENTENDIDO, PASAR EL TELÉFONO";

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

        // ¡El orden es importante! Configurar gestos ANTES de configurarListeners,
        // ya que el gesto se basa en un listener que debe existir primero.
        configurarGestos();
        configurarListeners();

        if (jugadores != null && !jugadores.isEmpty()) {
            mostrarTurnoActual();
        } else {
            Toast.makeText(this, "Error: El juego no tiene jugadores válidos.", Toast.LENGTH_LONG).show();
            finish();
        }

        btnNextPlayer.setVisibility(View.GONE);
    }

    private void inicializarVistas() {
        tvCurrentPlayerName = findViewById(R.id.tv_current_player_name);
        tvPlayerRole = findViewById(R.id.tv_player_role);
        tvPlayerWord = findViewById(R.id.tv_player_word);

        imgMaskCover = findViewById(R.id.img_mask_cover);
        tvInstruction = findViewById(R.id.tv_instruction);
        imgLockIcon = findViewById(R.id.img_lock_icon);

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

        jugadores = (List<Jugador>) intent.getSerializableExtra(KEY_JUGADORES);
        categoriaSeleccionada = (Categoria) intent.getSerializableExtra(KEY_CATEGORIA);

        if (jugadores == null || jugadores.isEmpty() || categoriaSeleccionada == null) {
            Toast.makeText(this, "Error: Faltan datos críticos para iniciar el juego.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        categoriaSeleccionada.seleccionarPalabraAleatoria();
        Log.i("Categoria", categoriaSeleccionada.getName());
    }

    private void configurarListeners() {
        // btnAction ahora tiene DOS funciones: REVELAR/PULSAR y PASAR TURNO
        btnAction.setOnClickListener(v -> handleActionButton());

        // El TextView de instrucción debe ser clickeable para disparar la revelación
        tvInstruction.setOnClickListener(v -> {
            if (!isRoleRevealed) {
                handleActionButton(); // El clic en la instrucción también dispara la revelación
            }
        });

        // El icono de candado también
        imgLockIcon.setOnClickListener(v -> {
            if (!isRoleRevealed) {
                handleActionButton();
            }
        });
    }

    /**
     * Configura el GestureDetector para detectar gestos en la máscara.
     */
    private void configurarGestos() {
        gestureDetector = new GestureDetector(this, new GestureListener());

        // Asignar el OnTouchListener a la MÁSCARA
        imgMaskCover.setOnTouchListener((v, event) -> {
            // Si el rol ya está revelado, no consumimos el evento touch
            if (isRoleRevealed) {
                return false;
            }

            // Pasamos el evento al GestureDetector para la detección de deslizamiento
            return gestureDetector.onTouchEvent(event);
        });
    }

    // ******************************************************
    // CLASE INTERNA PARA DETECTAR EL GESTO DE DESLIZAMIENTO
    // ******************************************************
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true; // Necesario para procesar onFling
        }

        // También podemos usar onSingleTapUp para revelar si el usuario solo toca la tarjeta
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!isRoleRevealed) {
                handleActionButton(); // Trata el toque simple como una pulsación del botón
                return true;
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            // Deslizamiento vertical hacia arriba (Swipe UP)
            if (Math.abs(diffY) > Math.abs(diffX) && Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY < 0) {
                    // Swipe UP detectado
                    if (!isRoleRevealed) {
                        revelarRol();
                        isRoleRevealed = true;
                        btnAction.setEnabled(false); // Deshabilitamos temporalmente
                        return true;
                    }
                }
            }
            return false;
        }
    }
    // ******************************************************

    /**
     * Alterna entre el estado de REVELAR (inicia animación) y el estado de PASAR TURNO (avanza al siguiente jugador).
     */
    private void handleActionButton() {
        if (!isRoleRevealed) {
            // Estado 1: REVELAR ROL (Pulsar o tocar)
            revelarRol();
            isRoleRevealed = true;
            btnAction.setEnabled(false); // Deshabilita temporalmente durante la animación/revelación
        } else {
            // Estado 2: ENTENDIDO (Pulsa y pasa al siguiente jugador)
            pasarSiguienteJugador();
        }
    }

    private void revelarRol() {
        Jugador actual = jugadores.get(currentPlayerIndex);
        Roles rolActual = Roles.valueOf(actual.getRole());

        prepararContenidoRol(rolActual);

        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                llRoleContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Ocultar permanentemente los elementos de la máscara
                imgMaskCover.setVisibility(View.GONE);
                tvInstruction.setVisibility(View.GONE);
                imgLockIcon.setVisibility(View.GONE);

                // Habilitamos el botón y cambiamos el texto
                btnAction.setText(STATE_REVEALED);
                btnAction.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        imgMaskCover.startAnimation(slideOut);
        tvInstruction.startAnimation(slideOut);
        imgLockIcon.startAnimation(slideOut);
    }

    private void prepararContenidoRol(Roles rolActual) {
        String rolDisplay;
        String wordToDisplay;

        int colorCivil = ContextCompat.getColor(this, R.color.colorPrimary);
        int colorImpostor = ContextCompat.getColor(this, R.color.role_impostor);

        if (rolActual == Roles.IMPOSTOR) {
            rolDisplay = getResources().getString(R.string.impostor);
            wordToDisplay = categoriaSeleccionada.getClue();
            tvPlayerRole.setTextColor(colorImpostor);
        } else {
            rolDisplay = getResources().getString(R.string.civil);
            wordToDisplay = categoriaSeleccionada.getWord();
            tvPlayerRole.setTextColor(colorCivil);
        }

        tvPlayerWord.setText(wordToDisplay != null ? wordToDisplay.toUpperCase() : "ERROR");
        tvPlayerRole.setText(rolDisplay);
    }

    private void pasarSiguienteJugador() {
        currentPlayerIndex++;

        if (currentPlayerIndex < jugadores.size()) {
            isRoleRevealed = false;

            // --- REINICIO DE ESTADOS VISUALES ---
            btnAction.setText(STATE_REVEAL);
            btnAction.setEnabled(true);

            // Restablecer la Máscara a su posición inicial
            imgMaskCover.setVisibility(View.VISIBLE);
            imgMaskCover.clearAnimation();

            tvInstruction.setVisibility(View.VISIBLE);
            tvInstruction.clearAnimation();

            imgLockIcon.setVisibility(View.VISIBLE);
            imgLockIcon.clearAnimation();

            llRoleContent.setVisibility(View.INVISIBLE);

            mostrarTurnoActual();

        } else {
            Toast.makeText(this, "¡Todos listos! Comienza el debate.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(JuegoActivity.this, InGameActivity.class);
            intent.putExtra(KEY_JUGADORES, (Serializable) jugadores);
            intent.putExtra(KEY_CATEGORIA, categoriaSeleccionada);

            startActivity(intent);
            finish();
        }
    }

    private void mostrarTurnoActual() {
        Jugador actual = jugadores.get(currentPlayerIndex);

        tvCurrentPlayerName.setText(getString(R.string.turn_of, actual.getName().toUpperCase()));

        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle(getString(R.string.dialog_turn_change_title))
                .setMessage(getString(R.string.dialog_pass_device_to, actual.getName().toUpperCase()))
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.ic_player_transfer)
                .show();
    }
}