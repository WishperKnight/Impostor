package ies.carrillo.impostor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import ies.carrillo.impostor.R;

public class TimeSelectorActivity extends AppCompatActivity {

    private TextView tvSelectedTime;
    private SeekBar sbTimeSelector;
    private MaterialButton btnConfirmTime;

    // Constantes de tiempo
    private final int MIN_TIME = 3;  // Mínimo de 3 minutos
    private final int MAX_TIME = 15; // Máximo de 15 minutos
    private final int DEFAULT_TIME = 5; // Valor por defecto

    private int selectedTime = DEFAULT_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_selector);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        configurarSeekBar();
        configurarBotonConfirmar();
    }

    private void inicializarVistas() {
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        sbTimeSelector = findViewById(R.id.sb_time_selector);
        btnConfirmTime = findViewById(R.id.btn_confirm_time);
    }

    private void configurarSeekBar() {
        // Establecer el rango de la SeekBar: de 0 (MIN_TIME) a 12 (MAX_TIME)
        sbTimeSelector.setMax(MAX_TIME - MIN_TIME);

        // Establecer el progreso inicial para el tiempo por defecto (5 min)
        sbTimeSelector.setProgress(DEFAULT_TIME - MIN_TIME);

        // Inicializar el TextView con el valor por defecto
        updateTimeDisplay(DEFAULT_TIME);

        sbTimeSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Mapear el progreso (0-12) al tiempo real (3-15)
                selectedTime = progress + MIN_TIME;
                updateTimeDisplay(selectedTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No se necesita implementación específica aquí
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Opcional: Mostrar un Toast al finalizar la selección
                Toast.makeText(TimeSelectorActivity.this, "Tiempo seleccionado: " + selectedTime + " min", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTimeDisplay(int time) {
        tvSelectedTime.setText(time + " minutos");
    }

    private void configurarBotonConfirmar() {
        btnConfirmTime.setOnClickListener(v -> confirmTime());
    }

    private void confirmTime() {
        // 1. Crear el Intent para devolver el resultado
        Intent resultIntent = new Intent();

        // 2. Adjuntar el tiempo seleccionado
        resultIntent.putExtra("GAME_DURATION", selectedTime);

        // 3. Establecer el resultado y finalizar la Activity
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Duración guardada: " + selectedTime + " minutos", Toast.LENGTH_SHORT).show();
        finish();
    }
}