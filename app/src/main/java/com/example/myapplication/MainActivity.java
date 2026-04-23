package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static int nivelAlcanzado = 5; // Desbloqueado temporalmente para ver todos los niveles
    private int ultimoNivelRegistrado = 5;
    private String dificultadSeleccionada = "FACIL";
    private LinearLayout layoutFacil, layoutMedio, layoutDificil, layoutEntrenamiento, layoutInverso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vincularVistas();
        configurarListeners();
        ultimoNivelRegistrado = nivelAlcanzado;
    }

    private void vincularVistas() {
        layoutFacil = findViewById(R.id.layoutFacil);
        layoutMedio = findViewById(R.id.layoutMedio);
        layoutDificil = findViewById(R.id.layoutDificil);
        layoutEntrenamiento = findViewById(R.id.layoutEntrenamiento);
        layoutInverso = findViewById(R.id.layoutInverso);
    }

    private void configurarListeners() {
        Button btnComenzar = findViewById(R.id.btnComenzarJuego);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        ImageButton btnTrophy = findViewById(R.id.btnTrophy);
        ImageButton btnStats = findViewById(R.id.btnStats);

        layoutFacil.setOnClickListener(v -> seleccionarNivel("FACIL"));
        
        layoutMedio.setOnClickListener(v -> {
            if (nivelAlcanzado >= 2) seleccionarNivel("MEDIO");
            else mostrarBloqueado();
        });
        
        layoutDificil.setOnClickListener(v -> {
            if (nivelAlcanzado >= 3) seleccionarNivel("DIFICIL");
            else mostrarBloqueado();
        });

        layoutEntrenamiento.setOnClickListener(v -> seleccionarNivel("ENTRENAMIENTO"));
        layoutInverso.setOnClickListener(v -> seleccionarNivel("INVERSO"));

        btnComenzar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, JuegoActivity.class);
            intent.putExtra("DIFICULTAD", dificultadSeleccionada);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
        btnTrophy.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MejoresResultadosActivity.class)));
        btnStats.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EstadisticasActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Detectar si se desbloqueó un nivel nuevo
        if (nivelAlcanzado > ultimoNivelRegistrado) {
            animarDesbloqueo(nivelAlcanzado);
            ultimoNivelRegistrado = nivelAlcanzado;
        } else {
            ultimoNivelRegistrado = nivelAlcanzado;
            actualizarInterfazNiveles();
        }
    }

    private void actualizarInterfazNiveles() {
        layoutFacil.setAlpha(1.0f);
        layoutMedio.setAlpha(nivelAlcanzado >= 2 ? 1.0f : 0.4f);
        layoutDificil.setAlpha(nivelAlcanzado >= 3 ? 1.0f : 0.4f);
        layoutEntrenamiento.setAlpha(nivelAlcanzado >= 4 ? 1.0f : 0.4f);
        layoutInverso.setAlpha(nivelAlcanzado >= 5 ? 1.0f : 0.4f);
        
        if (dificultadSeleccionada.equals("MEDIO") && nivelAlcanzado < 2) seleccionarNivel("FACIL");
        if (dificultadSeleccionada.equals("DIFICIL") && nivelAlcanzado < 3) seleccionarNivel("FACIL");
        
        refrescarFondos();
    }

    private void animarDesbloqueo(int nivel) {
        actualizarInterfazNiveles();
        View viewAAnimar = (nivel == 2) ? layoutMedio : (nivel == 3) ? layoutDificil : (nivel == 4) ? layoutEntrenamiento : (nivel == 5) ? layoutInverso : null;
        
        if (viewAAnimar != null) {
            viewAAnimar.setAlpha(1.0f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(viewAAnimar, "scaleX", 1f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(viewAAnimar, "scaleY", 1f, 1.2f, 1f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(scaleX, scaleY);
            animSet.setDuration(800);
            animSet.start();
        }
    }

    private void seleccionarNivel(String nivel) {
        dificultadSeleccionada = nivel;
        refrescarFondos();
    }

    private void refrescarFondos() {
        layoutFacil.setBackgroundResource(dificultadSeleccionada.equals("FACIL") ? R.drawable.bg_modo_selected : R.drawable.bg_modo_boton);
        layoutMedio.setBackgroundResource(dificultadSeleccionada.equals("MEDIO") ? R.drawable.bg_modo_selected : R.drawable.bg_modo_boton);
        layoutDificil.setBackgroundResource(dificultadSeleccionada.equals("DIFICIL") ? R.drawable.bg_modo_selected : R.drawable.bg_modo_boton);
        layoutEntrenamiento.setBackgroundResource(dificultadSeleccionada.equals("ENTRENAMIENTO") ? R.drawable.bg_modo_selected : R.drawable.bg_modo_boton);
        layoutInverso.setBackgroundResource(dificultadSeleccionada.equals("INVERSO") ? R.drawable.bg_modo_selected : R.drawable.bg_modo_boton);
    }

    private void mostrarBloqueado() {
        Toast.makeText(this, "Nivel bloqueado. Completa el anterior para desbloquearlo.", Toast.LENGTH_SHORT).show();
    }
}
