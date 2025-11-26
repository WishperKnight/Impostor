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

    public static final String EXTRA_PLAYERS_WITH_ROLES = "JUGADORES_CON_ROLES";
    public static final String EXTRA_DURATION_MINUTES = "DURACION_MINUTOS";

    private TextView tvTimer;
    private RecyclerView rvActivePlayers;
    private LinearLayout overlayEndGame;
    private TextView tvEndGameTitle, tvEndGameSubtitle;
    private MaterialButton btnBackToMenu;

    private PlayersAdapter adapter;
    private final List<Jugador> activePlayers = new ArrayList<>();
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_in_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        initViews();
        loadIntentData();
        setupRecyclerView();
        startTimer();
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tv_timer);
        rvActivePlayers = findViewById(R.id.rv_active_players);

        overlayEndGame = findViewById(R.id.overlay_end_game);
        tvEndGameTitle = findViewById(R.id.tv_end_game_title);
        tvEndGameSubtitle = findViewById(R.id.tv_end_game_subtitle);
        btnBackToMenu = findViewById(R.id.btn_back_to_menu);

        btnBackToMenu.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void loadIntentData() {
        Intent intent = getIntent();

        List<Jugador> received = (List<Jugador>) intent.getSerializableExtra(EXTRA_PLAYERS_WITH_ROLES);
        if (received != null) {
            activePlayers.clear();
            activePlayers.addAll(received);
        }

        int minutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 5);
        timeLeftInMillis = minutes * 60_000L;
    }

    private void setupRecyclerView() {
        rvActivePlayers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlayersAdapter(activePlayers, this);
        rvActivePlayers.setAdapter(adapter);
    }

    private void startTimer() {
        if (timeLeftInMillis <= 0) timeLeftInMillis = 300_000;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long remaining) {
                timeLeftInMillis = remaining;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                tvTimer.setText("¡TIEMPO!");
                tvTimer.setTextColor(Color.RED);
                Toast.makeText(InGameActivity.this, "El tiempo ha terminado. ¡Votad!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void updateTimerText() {
        int m = (int) (timeLeftInMillis / 1000) / 60;
        int s = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
    }

    @Override
    public void onPlayerDelete(int position) {
        confirmDeletion(position);
    }

    @Override
    public void onPlayerCountChanged(int count) {
    }

    @Override
    public void onPlayerColorChange(int position, String newColorHex) {
    }

    @Override
    public void onColorIndicatorClicked(int position) {
    }

    private void confirmDeletion(int position) {
        Jugador j = activePlayers.get(position);

        new AlertDialog.Builder(this)
                .setTitle("¿VOTACIÓN FINAL?")
                .setMessage("¿Eliminar a " + j.getName().toUpperCase() + "?")
                .setPositiveButton("Eliminar", (d, w) -> removePlayer(position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void removePlayer(int position) {
        Jugador out = activePlayers.get(position);

        // Comparación correcta con String
        String rolTexto = "IMPOSTOR".equals(out.getRole()) ? "IMPOSTOR" : "CIVIL";
        Toast.makeText(this, out.getName() + " era " + rolTexto, Toast.LENGTH_LONG).show();

        activePlayers.remove(position);
        adapter.notifyItemRemoved(position);

        checkWinCondition();
    }

    private void checkWinCondition() {
        if (activePlayers.isEmpty()) return;

        // Contar impostores correctamente usando String
        long impostors = activePlayers.stream()
                .filter(j -> "IMPOSTOR".equals(j.getRole()))
                .count();

        long civils = activePlayers.size() - impostors;

        if (impostors == 0) {
            showEndGame(true,
                    "¡VICTORIA de los Civiles! 🏆",
                    "Todos los impostores han sido eliminados.");
            return;
        }

        if (impostors >= civils) {
            showEndGame(false,
                    "¡VICTORIA del Impostor! 🔪",
                    "Los impostores han alcanzado el mismo número que los civiles.");
        }
    }


    private void showEndGame(boolean civilWin, String title, String subtitle) {
        if (countDownTimer != null) countDownTimer.cancel();

        overlayEndGame.setVisibility(View.VISIBLE);
        overlayEndGame.setBackgroundColor(
                Color.argb(190,
                        civilWin ? 76 : 244,
                        civilWin ? 175 : 67,
                        civilWin ? 80 : 54)
        );

        tvEndGameTitle.setText(title);
        tvEndGameSubtitle.setText(subtitle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
