package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String dificultadSeleccionada = "FACIL"; // Por defecto
    private LinearLayout layoutFacil, layoutMedio, layoutDificil, layoutEntrenamiento, layoutInverso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias a los layouts de niveles
        layoutFacil = findViewById(R.id.layoutFacil);
        layoutMedio = findViewById(R.id.layoutMedio);
        layoutDificil = findViewById(R.id.layoutDificil);
        layoutEntrenamiento = findViewById(R.id.layoutEntrenamiento);
        layoutInverso = findViewById(R.id.layoutInverso);

        Button btnComenzar = findViewById(R.id.btnComenzarJuego);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        ImageButton btnTrophy = findViewById(R.id.btnTrophy);
        ImageButton btnStats = findViewById(R.id.btnStats);

        // Listeners para selección de nivel
        layoutFacil.setOnClickListener(v -> seleccionarNivel("FACIL"));
        layoutMedio.setOnClickListener(v -> seleccionarNivel("MEDIO"));
        layoutDificil.setOnClickListener(v -> seleccionarNivel("DIFICIL"));
        layoutEntrenamiento.setOnClickListener(v -> seleccionarNivel("ENTRENAMIENTO"));
        layoutInverso.setOnClickListener(v -> seleccionarNivel("INVERSO"));

        btnComenzar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, JuegoActivity.class);
            intent.putExtra("DIFICULTAD", dificultadSeleccionada);
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
        
        // Inicializar visualmente (por defecto Fácil)
        seleccionarNivel("FACIL");
    }

    private void seleccionarNivel(String nivel) {
        dificultadSeleccionada = nivel;

        // Resetear todos los fondos al estado normal
        layoutFacil.setBackgroundResource(R.drawable.bg_modo_boton);
        layoutMedio.setBackgroundResource(R.drawable.bg_modo_boton);
        layoutDificil.setBackgroundResource(R.drawable.bg_modo_boton);
        layoutEntrenamiento.setBackgroundResource(R.drawable.bg_modo_boton);
        layoutInverso.setBackgroundResource(R.drawable.bg_modo_boton);

        // Resaltar el seleccionado
        switch (nivel) {
            case "FACIL":
                layoutFacil.setBackgroundResource(R.drawable.bg_modo_selected);
                break;
            case "MEDIO":
                layoutMedio.setBackgroundResource(R.drawable.bg_modo_selected);
                break;
            case "DIFICIL":
                layoutDificil.setBackgroundResource(R.drawable.bg_modo_selected);
                break;
            case "ENTRENAMIENTO":
                layoutEntrenamiento.setBackgroundResource(R.drawable.bg_modo_selected);
                break;
            case "INVERSO":
                layoutInverso.setBackgroundResource(R.drawable.bg_modo_selected);
                break;
        }
    }
}
