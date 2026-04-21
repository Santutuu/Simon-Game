package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnComenzar = findViewById(R.id.btnComenzarJuego);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        ImageButton btnTrophy = findViewById(R.id.btnTrophy);
        ImageButton btnStats = findViewById(R.id.btnStats);

        btnComenzar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, JuegoActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnTrophy.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MejoresResultadosActivity.class);
            startActivity(intent);
        });

        btnStats.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EstadisticasActivity.class);
            startActivity(intent);
        });
    }
}
