package ies.carrillo.impostor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import ies.carrillo.impostor.R;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private final int SPLASH_DURATION = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);

        // Animación de la barra de progreso
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;
                runOnUiThread(() -> progressBar.setProgress(progressStatus));

                try {
                    Thread.sleep(SPLASH_DURATION / 100); // Distribuye el progreso en 3 segundos
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Abrir MainActivity al finalizar
            runOnUiThread(() -> {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }
}
