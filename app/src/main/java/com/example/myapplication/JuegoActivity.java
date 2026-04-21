package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JuegoActivity extends AppCompatActivity {

    private View cuadro1, cuadro2, cuadro3, cuadro4;
    private ImageButton btnPlay, btnHome;
    private TextView txtPuntaje;

    private final List<Integer> secuencia = new ArrayList<>();
    private final List<Integer> entradaJugador = new ArrayList<>();

    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean mostrandoSecuencia = false;
    private boolean juegoActivo = false;

    private int ronda = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);

        cuadro1 = findViewById(R.id.cuadro1);
        cuadro2 = findViewById(R.id.cuadro2);
        cuadro3 = findViewById(R.id.cuadro3);
        cuadro4 = findViewById(R.id.cuadro4);
        btnPlay = findViewById(R.id.btnPlay);
        btnHome = findViewById(R.id.btnHome);
        txtPuntaje = findViewById(R.id.txtPuntaje);

        btnPlay.setOnClickListener(v -> iniciarJuego());
        
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(JuegoActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        cuadro1.setOnClickListener(v -> manejarToqueJugador(0));
        cuadro2.setOnClickListener(v -> manejarToqueJugador(1));
        cuadro3.setOnClickListener(v -> manejarToqueJugador(2));
        cuadro4.setOnClickListener(v -> manejarToqueJugador(3));
    }

    private void iniciarJuego() {
        secuencia.clear();
        entradaJugador.clear();
        ronda = 0;
        juegoActivo = true;
        mostrandoSecuencia = false;
        txtPuntaje.setText("0");
        btnPlay.setVisibility(View.GONE);

        siguienteRonda();
    }

    private void siguienteRonda() {
        if (!juegoActivo) return;

        entradaJugador.clear();
        secuencia.add(random.nextInt(4));
        ronda++;
        txtPuntaje.setText(String.valueOf(ronda - 1));

        mostrarSecuencia();
    }

    private void mostrarSecuencia() {
        mostrandoSecuencia = true;

        long delay = 500;

        for (int i = 0; i < secuencia.size(); i++) {
            int colorIndex = secuencia.get(i);
            long tiempoActual = delay;

            handler.postDelayed(() -> animarColor(colorIndex), tiempoActual);
            delay += 800;
        }

        handler.postDelayed(() -> mostrandoSecuencia = false, delay);
    }

    private void manejarToqueJugador(int colorIndex) {
        if (!juegoActivo || mostrandoSecuencia) return;

        animarColor(colorIndex);
        entradaJugador.add(colorIndex);

        int posicionActual = entradaJugador.size() - 1;

        if (!entradaJugador.get(posicionActual).equals(secuencia.get(posicionActual))) {
            perder();
            return;
        }

        if (entradaJugador.size() == secuencia.size()) {
            handler.postDelayed(this::siguienteRonda, 700);
        }
    }

    private void perder() {
        juegoActivo = false;
        mostrandoSecuencia = false;

        Toast.makeText(this, "Perdiste", Toast.LENGTH_SHORT).show();
        btnPlay.setVisibility(View.VISIBLE);
    }

    private void animarColor(int colorIndex) {
        View cuadro = obtenerCuadroPorIndice(colorIndex);
        if (cuadro == null) return;

        // Validamos que tenga un fondo de color para evitar errores de cast
        if (cuadro.getBackground() instanceof ColorDrawable) {
            int colorOriginal = ((ColorDrawable) cuadro.getBackground()).getColor();
            int colorClaro = aclararColor(colorOriginal);
            cuadro.setBackgroundColor(colorClaro);
            handler.postDelayed(() -> cuadro.setBackgroundColor(colorOriginal), 300);
        }

        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(cuadro, "scaleX", 1f, 1.08f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(cuadro, "scaleY", 1f, 1.08f);
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(cuadro, "scaleX", 1.08f, 1f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(cuadro, "scaleY", 1.08f, 1f);

        scaleXUp.setDuration(150);
        scaleYUp.setDuration(150);
        scaleXDown.setDuration(150);
        scaleYDown.setDuration(150);

        AnimatorSet up = new AnimatorSet();
        up.playTogether(scaleXUp, scaleYUp);

        AnimatorSet down = new AnimatorSet();
        down.playTogether(scaleXDown, scaleYDown);

        AnimatorSet total = new AnimatorSet();
        total.playSequentially(up, down);
        total.start();
    }

    private View obtenerCuadroPorIndice(int index) {
        switch (index) {
            case 0: return cuadro1;
            case 1: return cuadro2;
            case 2: return cuadro3;
            case 3: return cuadro4;
            default: return null;
        }
    }

    private int aclararColor(int color) {
        int a = (color >> 24) & 0xff;
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;

        r = Math.min(255, r + 80);
        g = Math.min(255, g + 80);
        b = Math.min(255, b + 80);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
