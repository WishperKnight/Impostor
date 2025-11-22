package ies.carrillo.impostor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.dataBase.DataBase;
import ies.carrillo.impostor.model.Categoria;
import ies.carrillo.impostor.model.Jugador;
import ies.carrillo.impostor.utils.GameLogic;

public class MainActivity extends AppCompatActivity {

    private TextView tvNumImpostores;
    private TextView tvNumJugadores;
    private TextView tvTiempoSeleccionado;
    private TextView tvPaquetesSeleccionados;
    private MaterialButton btnIniciarJuego;

    private int numImpostores = 1;
    private final int MIN_IMPOSTORES = 1;
    private final int MAX_IMPOSTORES = 2;

    private int duracionJuegoSegundos = 300; // 5 minutos por defecto, en segundos
    private final int MIN_PLAYERS = 3;

    // Constantes de claves de Intent
    public static final String KEY_SELECTED_CATEGORIES = "CATEGORIAS_SELECCIONADAS";
    public static final String KEY_SELECTED_TIME = "DURACION_SEGUNDOS";

    // Lista real de categorías seleccionadas por el usuario
    private ArrayList<Categoria> categoriasSeleccionadas = new ArrayList<>();

    // Launchers para manejar resultados de otras actividades
    private ActivityResultLauncher<Intent> packagesLauncher;
    private ActivityResultLauncher<Intent> timeLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        inicializarVistas();
        setupActivityLaunchers();
        configurarContadorImpostores();
        configurarNavegacion();
        configurarBotonInicio();

        inicializarCategoriasDefault();
        updateDisplays();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplays();
    }

    private void inicializarCategoriasDefault() {
        if (categoriasSeleccionadas.isEmpty()) {
            DataBase db = DataBase.getInstance();
            // Selecciona las categorías predefinidas por defecto al iniciar
            categoriasSeleccionadas.add(db.getSemanaSanta());
            categoriasSeleccionadas.add(db.getNaturaleza());
        }
    }


    private void inicializarVistas() {
        tvNumImpostores = findViewById(R.id.tv_num_impostores);
        tvNumJugadores = findViewById(R.id.tv_num_jugadores);
        tvTiempoSeleccionado = findViewById(R.id.tv_duracion_tiempo);
        tvPaquetesSeleccionados = findViewById(R.id.tv_cantidad_paquetes);
        btnIniciarJuego = findViewById(R.id.btn_iniciar_juego);
    }

    private void setupActivityLaunchers() {
        // --- Launcher para Seleccionar Paquetes ---
        packagesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Serializable categoriesExtra = data.getSerializableExtra(KEY_SELECTED_CATEGORIES);

                        if (categoriesExtra instanceof ArrayList) {
                            categoriasSeleccionadas = (ArrayList<Categoria>) categoriesExtra;
                            updateDisplays();
                            Toast.makeText(this, categoriasSeleccionadas.size() + " paquetes seleccionados.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // --- Launcher para Seleccionar Tiempo ---
        timeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        duracionJuegoSegundos = data.getIntExtra(KEY_SELECTED_TIME, 300);
                        updateDisplays();
                        Toast.makeText(this, "Tiempo ajustado.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void updateDisplays() {
        DataBase db = DataBase.getInstance();
        ArrayList<Jugador> jugadores = db.getJugadores();
        int numJugadores = jugadores.size();

        // 1. Actualizar Jugadores
        tvNumJugadores.setText(String.valueOf(numJugadores));

        // 2. Actualizar Impostores
        if (numImpostores >= numJugadores && numJugadores > 0) {
            numImpostores = 1;
        } else if (numJugadores < MIN_PLAYERS) {
            numImpostores = MIN_IMPOSTORES;
        }
        tvNumImpostores.setText(String.valueOf(numImpostores));

        // 3. Actualizar Tiempo
        int duracionMinutos = duracionJuegoSegundos / 60;
        tvTiempoSeleccionado.setText(duracionMinutos + " min");

        // 4. Actualizar Paquetes
        int countPaquetes = categoriasSeleccionadas.size();
        tvPaquetesSeleccionados.setText(countPaquetes + " Selecc.");

        // Habilitar/Deshabilitar el botón de inicio
        boolean canStart = numJugadores >= MIN_PLAYERS && countPaquetes > 0;
        btnIniciarJuego.setEnabled(canStart);
        btnIniciarJuego.setAlpha(canStart ? 1.0f : 0.5f);
    }

    private void configurarContadorImpostores() {
        ImageButton btnSumar = findViewById(R.id.btn_sumar_impostor);
        ImageButton btnRestar = findViewById(R.id.btn_restar_impostor);

        btnSumar.setOnClickListener(v -> {
            int totalJugadores = DataBase.getInstance().getJugadores().size();
            if (numImpostores < MAX_IMPOSTORES && numImpostores < totalJugadores - 1) {
                numImpostores++;
                updateDisplays();
            } else {
                Toast.makeText(this, "Máximo alcanzado o faltan jugadores para el juego.", Toast.LENGTH_SHORT).show();
            }
        });

        btnRestar.setOnClickListener(v -> {
            if (numImpostores > MIN_IMPOSTORES) {
                numImpostores--;
                updateDisplays();
            }
        });
    }

    private void configurarNavegacion() {
        LinearLayout llJugadores = findViewById(R.id.ll_jugadores_config);
        llJugadores.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddPlayersActivity.class)));

        LinearLayout llPaquetes = findViewById(R.id.ll_paquetes_config);
        // Usamos el Launcher para obtener la lista seleccionada
        llPaquetes.setOnClickListener(v -> packagesLauncher.launch(new Intent(MainActivity.this, SelectPackagesActivity.class)));

        LinearLayout llTiempo = findViewById(R.id.ll_tiempo_config);
        // Usamos el Launcher para obtener la duración
        llTiempo.setOnClickListener(v -> timeLauncher.launch(new Intent(MainActivity.this, TimeSelectorActivity.class)));
    }


    private void configurarBotonInicio() {
        SwitchMaterial switchPistas = findViewById(R.id.switch_pistas);

        btnIniciarJuego.setOnClickListener(v -> {
            List<Jugador> jugadores = DataBase.getInstance().getJugadores();
            boolean pistasHabilitadas = switchPistas.isChecked();

            if (jugadores.size() < MIN_PLAYERS) {
                Toast.makeText(this, "Mínimo " + MIN_PLAYERS + " jugadores.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (categoriasSeleccionadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un paquete de palabras.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Obtener Categoría Aleatoria
            Categoria categoriaSeleccionada = obtenerCategoriaAleatoria(categoriasSeleccionadas);
            if (categoriaSeleccionada == null) {
                Toast.makeText(this, "Error al seleccionar una categoría.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Asignar Roles
            GameLogic.assignRoles(jugadores, numImpostores);

            // 3. Iniciar JuegoActivity
            Intent intent = new Intent(MainActivity.this, JuegoActivity.class);

            intent.putExtra("CATEGORIA_SELECCIONADA", (Serializable) categoriaSeleccionada);
            intent.putExtra("JUGADORES_CON_ROLES", (Serializable) jugadores);
            intent.putExtra("DURACION_SEGUNDOS", duracionJuegoSegundos);
            intent.putExtra("PISTAS_HABILITADAS", pistasHabilitadas); // Corregido el typo

            startActivity(intent);
        });
    }

    private Categoria obtenerCategoriaAleatoria(List<Categoria> listaSeleccionada) {
        if (listaSeleccionada.isEmpty()) return null;

        Random random = new Random();
        return listaSeleccionada.get(random.nextInt(listaSeleccionada.size()));
    }
}