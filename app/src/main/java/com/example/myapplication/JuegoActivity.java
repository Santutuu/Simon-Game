package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class JuegoActivity extends AppCompatActivity {

    private View rootLayout;
    private TextView cuadro1, cuadro2, cuadro3, cuadro4;
    private TextView[] cuadrosSimon;
    private final int[] simonResIds = {
            R.drawable.bg_simon_green_3d,
            R.drawable.bg_simon_red_3d,
            R.drawable.bg_simon_yellow_3d,
            R.drawable.bg_simon_blue_3d
    };
    private final String[] simonNombres = {"VERDE", "ROJO", "AMARILLO", "AZUL"};
    private final List<Integer> mapaColores = new ArrayList<>(Arrays.asList(0, 1, 2, 3));

    private ImageButton btnPlay, btnHome;
    private Button btnSiguienteEtapa;
    private TextView txtPuntaje, txtIndicadorColor;
    private View barraTiempoProgreso;
    
    // Configuración dinámica
    private int iteracionesParaGanar = 20;
    private long tiempoConfiguradoIntent = 0;

    // Elementos para Nivel 2
    private GridLayout gridSimon, gridNumeros;
    private LinearLayout contenedorSecuencia, filaSecuencia1, filaSecuencia2;
    private final List<Integer> secuenciaNumeros = new ArrayList<>();
    private final List<Integer> numerosEnGrilla = new ArrayList<>();
    private int indiceActualSecuencia = 0;

    // Elementos para Nivel 1 y 3 (Simon)
    private final List<Integer> secuenciaSimon = new ArrayList<>();
    private final List<Integer> entradaJugadorSimon = new ArrayList<>();
    private int puntajeTotalSimon = 0;
    private int etapaActualSimon = 1;
    private long tiempoRestanteGlobal = 0;
    private long tiempoTotalConfigurado = 0;

    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean juegoActivo = false;
    private boolean mostrandoSecuenciaSimon = false;
    private CountDownTimer countDownTimer;

    private enum Dificultad { ENTRENAMIENTO, FACIL, MEDIO, DIFICIL, INVERSO }
    private Dificultad dificultadActual = Dificultad.FACIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);

        vincularVistas();
        recibirConfiguracion();
        configurarDificultad();
        configurarListeners();
    }

    private void vincularVistas() {
        rootLayout = findViewById(R.id.rootLayout);
        cuadro1 = findViewById(R.id.cuadro1);
        cuadro2 = findViewById(R.id.cuadro2);
        cuadro3 = findViewById(R.id.cuadro3);
        cuadro4 = findViewById(R.id.cuadro4);
        cuadrosSimon = new TextView[]{cuadro1, cuadro2, cuadro3, cuadro4};

        btnPlay = findViewById(R.id.btnPlay);
        btnHome = findViewById(R.id.btnHome);
        btnSiguienteEtapa = findViewById(R.id.btnSiguienteEtapa);
        txtPuntaje = findViewById(R.id.txtPuntaje);
        txtIndicadorColor = findViewById(R.id.txtIndicadorColor);
        barraTiempoProgreso = findViewById(R.id.barraTiempoProgreso);
        
        gridSimon = findViewById(R.id.gridSimon);
        gridNumeros = findViewById(R.id.gridNumeros);
        contenedorSecuencia = findViewById(R.id.contenedorSecuencia);
        filaSecuencia1 = findViewById(R.id.filaSecuencia1);
        filaSecuencia2 = findViewById(R.id.filaSecuencia2);
    }

    private void recibirConfiguracion() {
        iteracionesParaGanar = getIntent().getIntExtra("ITERACIONES", 20);
        tiempoConfiguradoIntent = getIntent().getLongExtra("TIEMPO_MAX", 0);
    }

    private void configurarDificultad() {
        String diff = getIntent().getStringExtra("DIFICULTAD");
        if (diff != null) {
            try { dificultadActual = Dificultad.valueOf(diff); } 
            catch (Exception e) { dificultadActual = Dificultad.FACIL; }
        }

        if (dificultadActual == Dificultad.MEDIO) {
            gridSimon.setVisibility(View.GONE);
            gridNumeros.setVisibility(View.VISIBLE);
            contenedorSecuencia.setVisibility(View.VISIBLE);
            txtIndicadorColor.setVisibility(View.GONE);
        } else if (dificultadActual == Dificultad.DIFICIL) {
            gridSimon.setVisibility(View.VISIBLE);
            gridNumeros.setVisibility(View.GONE);
            contenedorSecuencia.setVisibility(View.GONE);
            txtIndicadorColor.setVisibility(View.VISIBLE);
            gridSimon.setBackgroundColor(Color.WHITE); 
        } else {
            gridSimon.setVisibility(View.VISIBLE);
            gridNumeros.setVisibility(View.GONE);
            contenedorSecuencia.setVisibility(View.GONE);
            txtIndicadorColor.setVisibility(View.GONE);
            gridSimon.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void configurarListeners() {
        btnPlay.setOnClickListener(v -> iniciarJuego());
        btnHome.setOnClickListener(v -> finish());
        btnSiguienteEtapa.setOnClickListener(v -> {
            btnSiguienteEtapa.setVisibility(View.GONE);
            siguienteRondaSimon();
        });
        
        for (int i = 0; i < 4; i++) {
            final int posView = i;
            cuadrosSimon[i].setOnClickListener(v -> {
                int colorIdx = mapaColores.get(posView);
                manejarToqueSimon(colorIdx);
            });
        }

        rootLayout.setOnLongClickListener(v -> {
            if (juegoActivo && dificultadActual == Dificultad.MEDIO) {
                int numeroBuscado = secuenciaNumeros.get(indiceActualSecuencia);
                if (!numerosEnGrilla.contains(numeroBuscado)) {
                    avanzarSecuenciaNumeros();
                    return true;
                } else {
                    perder();
                    return true;
                }
            }
            return false;
        });
    }

    private void iniciarJuego() {
        juegoActivo = true;
        txtPuntaje.setText("0");
        btnPlay.setVisibility(View.GONE);
        
        // El usuario pidió que empiece con 20 segundos por ahora para todos los niveles
        if (tiempoConfiguradoIntent > 0) {
            tiempoTotalConfigurado = tiempoConfiguradoIntent;
        } else {
            tiempoTotalConfigurado = 20000;
        }
        
        tiempoRestanteGlobal = tiempoTotalConfigurado;

        if (dificultadActual == Dificultad.MEDIO) {
            iniciarNivelNumeros();
        } else {
            iniciarNivelSimon();
        }
    }

    // --- LÓGICA NIVEL 2: NÚMEROS ---

    private void iniciarNivelNumeros() {
        secuenciaNumeros.clear();
        indiceActualSecuencia = 0;
        for (int i = 0; i < iteracionesParaGanar; i++) {
            secuenciaNumeros.add(random.nextInt(10));
        }
        dibujarSecuenciaSuperior();
        actualizarTableroNumeros();
        iniciarTemporizadorGlobal();
    }

    private void dibujarSecuenciaSuperior() {
        filaSecuencia1.removeAllViews();
        filaSecuencia2.removeAllViews();
        for (int i = 0; i < iteracionesParaGanar; i++) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(secuenciaNumeros.get(i)));
            tv.setTextColor(i == indiceActualSecuencia ? Color.YELLOW : Color.GRAY);
            tv.setTextSize(20);
            tv.setPadding(10, 5, 10, 5);
            tv.setTypeface(null, i == indiceActualSecuencia ? Typeface.BOLD : Typeface.NORMAL);
            if (i < iteracionesParaGanar/2) filaSecuencia1.addView(tv);
            else filaSecuencia2.addView(tv);
        }
    }

    private void actualizarTableroNumeros() {
        gridNumeros.removeAllViews();
        numerosEnGrilla.clear();
        List<Integer> botones = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        Collections.shuffle(botones);
        for (int i = 0; i < 9; i++) {
            int num = botones.get(i);
            numerosEnGrilla.add(num);
            TextView btn = new TextView(this);
            btn.setText(String.valueOf(num));
            btn.setBackgroundResource(R.drawable.bg_modo_boton);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(30);
            btn.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; params.height = 0;
            params.rowSpec = GridLayout.spec(i / 3, 1f);
            params.columnSpec = GridLayout.spec(i % 3, 1f);
            params.setMargins(8, 8, 8, 8);
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> manejarToqueNumero(num));
            gridNumeros.addView(btn);
        }
    }

    private void manejarToqueNumero(int numeroTocado) {
        if (!juegoActivo) return;
        if (numeroTocado == secuenciaNumeros.get(indiceActualSecuencia)) avanzarSecuenciaNumeros();
        else perder();
    }

    private void avanzarSecuenciaNumeros() {
        indiceActualSecuencia++;
        txtPuntaje.setText(String.valueOf(indiceActualSecuencia));
        if (indiceActualSecuencia >= iteracionesParaGanar) ganar();
        else {
            dibujarSecuenciaSuperior();
            actualizarTableroNumeros();
        }
    }

    // --- LÓGICA NIVEL 1 y 3: SIMON ---

    private void iniciarNivelSimon() {
        secuenciaSimon.clear();
        puntajeTotalSimon = 0;
        etapaActualSimon = 1;
        actualizarEsteticaSimon();
        siguienteRondaSimon();
    }

    private void siguienteRondaSimon() {
        int limiteEtapa;
        if (etapaActualSimon == 1) limiteEtapa = iteracionesParaGanar / 4;
        else if (etapaActualSimon == 2) limiteEtapa = iteracionesParaGanar / 3;
        else limiteEtapa = iteracionesParaGanar - (iteracionesParaGanar / 4) - (iteracionesParaGanar / 3);
        
        if (secuenciaSimon.size() >= limiteEtapa) {
            secuenciaSimon.clear();
            etapaActualSimon++;
            if (etapaActualSimon > 3) { ganar(); return; }
            pausarParaSiguienteEtapa();
            return;
        }
        entradaJugadorSimon.clear();
        secuenciaSimon.add(random.nextInt(4));
        mostrarSecuenciaSimon();
    }

    private void pausarParaSiguienteEtapa() {
        cancelarTemporizador();
        if (dificultadActual == Dificultad.FACIL) {
            tiempoRestanteGlobal = tiempoTotalConfigurado; 
        } else if (dificultadActual == Dificultad.DIFICIL) {
            // Requerimiento: 10s más por etapa en fase 3 (Nivel 3)
            tiempoTotalConfigurado += 10000;
            if (tiempoTotalConfigurado > 30000) tiempoTotalConfigurado = 30000;
            tiempoRestanteGlobal = tiempoTotalConfigurado;
        }
        btnSiguienteEtapa.setText("EMPEZAR ETAPA " + etapaActualSimon);
        btnSiguienteEtapa.setVisibility(View.VISIBLE);
        actualizarEsteticaSimon();
    }

    private void actualizarEsteticaSimon() {
        int colorFondo = (etapaActualSimon == 1) ? Color.BLACK : (etapaActualSimon == 2) ? Color.parseColor("#1A1A1A") : Color.parseColor("#111111");
        rootLayout.setBackgroundColor(colorFondo);
        if (dificultadActual == Dificultad.DIFICIL) {
            gridSimon.setBackgroundColor(Color.WHITE);
            shuffleGridSimon();
        } else {
            resetGridSimon();
        }
    }

    private void shuffleGridSimon() {
        Collections.shuffle(mapaColores);
        for (int i = 0; i < 4; i++) {
            int colorIdx = mapaColores.get(i);
            cuadrosSimon[i].setBackgroundResource(simonResIds[colorIdx]);
            // Etiquetas falsas
            int labelIdx;
            do { labelIdx = random.nextInt(4); } while (labelIdx == colorIdx);
            cuadrosSimon[i].setText(simonNombres[labelIdx]);
            cuadrosSimon[i].setTextColor(Color.WHITE); 
            cuadrosSimon[i].setTypeface(null, Typeface.BOLD);
            cuadrosSimon[i].setShadowLayer(5, 0, 0, Color.BLACK);
            cuadrosSimon[i].setGravity(Gravity.CENTER);
            cuadrosSimon[i].setTextSize(24);
        }
    }

    private void resetGridSimon() {
        mapaColores.clear();
        for(int i=0; i<4; i++) mapaColores.add(i);
        for (int i = 0; i < 4; i++) {
            cuadrosSimon[i].setBackgroundResource(simonResIds[i]);
            cuadrosSimon[i].setText("");
            cuadrosSimon[i].setShadowLayer(0, 0, 0, 0);
        }
    }

    private void mostrarSecuenciaSimon() {
        mostrandoSecuenciaSimon = true;
        cancelarTemporizador();
        long tiempoEncendido = (etapaActualSimon == 1) ? 600 : (etapaActualSimon == 2) ? 450 : 300;
        long totalDelay = 500;
        
        for (int i = 0; i < secuenciaSimon.size(); i++) {
            int idxTarget = secuenciaSimon.get(i);
            handler.postDelayed(() -> {
                if (dificultadActual == Dificultad.DIFICIL) {
                    shuffleGridSimon();
                    mostrarEstimuloPalabra(idxTarget);
                } else {
                    animarColor(idxTarget);
                }
            }, totalDelay);
            totalDelay += tiempoEncendido + 200;
        }
        
        handler.postDelayed(() -> {
            txtIndicadorColor.setText("");
            mostrandoSecuenciaSimon = false;
            iniciarTemporizadorGlobal();
        }, totalDelay);
    }

    private void mostrarEstimuloPalabra(int idxTarget) {
        String[] nombres = {"VERDE", "ROJO", "AMARILLO", "AZUL"};
        int[] colores = {Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN};
        txtIndicadorColor.setTextColor(colores[idxTarget]);
        int idxPalabra;
        do { idxPalabra = random.nextInt(4); } while (idxPalabra == idxTarget); 
        txtIndicadorColor.setText(nombres[idxPalabra]);
    }

    private void manejarToqueSimon(int colorIdx) {
        if (!juegoActivo || mostrandoSecuenciaSimon || btnSiguienteEtapa.getVisibility() == View.VISIBLE) return;
        
        int posView = mapaColores.indexOf(colorIdx);
        animarView(cuadrosSimon[posView]);

        entradaJugadorSimon.add(colorIdx);
        if (!entradaJugadorSimon.get(entradaJugadorSimon.size()-1).equals(secuenciaSimon.get(entradaJugadorSimon.size()-1))) {
            perder();
            return;
        }
        if (entradaJugadorSimon.size() == secuenciaSimon.size()) {
            puntajeTotalSimon++;
            txtPuntaje.setText(String.valueOf(puntajeTotalSimon));
            handler.postDelayed(this::siguienteRondaSimon, 700);
        } else if (dificultadActual == Dificultad.DIFICIL) {
            shuffleGridSimon();
        }
    }

    // --- UTILIDADES ---

    private void ganar() {
        juegoActivo = false;
        cancelarTemporizador();
        Toast.makeText(this, "¡NIVEL SUPERADO!", Toast.LENGTH_LONG).show();
        btnPlay.setVisibility(View.VISIBLE);
        txtIndicadorColor.setText("");
    }

    private void perder() {
        juegoActivo = false;
        cancelarTemporizador();
        Toast.makeText(this, "GAME OVER", Toast.LENGTH_SHORT).show();
        btnPlay.setVisibility(View.VISIBLE);
        txtIndicadorColor.setText("");
    }

    private void iniciarTemporizadorGlobal() {
        countDownTimer = new CountDownTimer(tiempoRestanteGlobal, 50) {
            @Override
            public void onTick(long millis) {
                tiempoRestanteGlobal = millis;
                float progreso = (float) millis / tiempoTotalConfigurado;
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) barraTiempoProgreso.getLayoutParams();
                p.weight = progreso;
                barraTiempoProgreso.setLayoutParams(p);
            }
            @Override
            public void onFinish() { perder(); }
        }.start();
    }

    private void cancelarTemporizador() { if (countDownTimer != null) countDownTimer.cancel(); }

    private void animarView(View v) {
        ObjectAnimator sX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator sY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.1f, 1f);
        AnimatorSet anim = new AnimatorSet();
        anim.playTogether(sX, sY);
        anim.setDuration(300);
        anim.start();
    }

    private void animarColor(int idx) {
        animarView(cuadrosSimon[idx]);
    }
}
