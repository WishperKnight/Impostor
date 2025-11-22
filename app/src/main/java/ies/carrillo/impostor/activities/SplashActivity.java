package ies.carrillo.impostor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ies.carrillo.impostor.R;

public class SplashActivity extends AppCompatActivity {

    // Duración del splash screen en milisegundos (2000 ms = 2 segundos)
    private static final int SPLASH_SCREEN_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Lógica del Splash Screen ---

        // Creamos un Handler para retrasar la ejecución de la siguiente acción
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 1. Crear un Intent para iniciar la MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);

                // 2. Iniciar la MainActivity
                startActivity(intent);

                // 3. Finalizar la SplashActivity para que el usuario no pueda volver a ella
                // con el botón de retroceso
                finish();
            }
        }, SPLASH_SCREEN_DURATION); // El retraso se aplica aquí (2000 ms)

        // --- Fin de la lógica del Splash Screen ---
    }
}