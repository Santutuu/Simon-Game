package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

    // Sistema de Pantallas de Mensaje y Stats
    private View layoutOverlayMensaje;
    private TextView txtMensajeTitulo, txtMensajeDescripcion;
    private ImageView imgIconoMensaje;
    private Button btnEntendido;
    private FrameLayout contenedorEstadisticas;
    private TextView txtStatReaccion, txtStatPorcentaje;

    // Sistema de Vidas
    private LinearLayout layoutVidas;
    public static int vidasRestantes = 0; 

    // Métricas
    private final List<Long> tiemposReaccion = new ArrayList<>();
    private long momentoInicioEstimulo = 0;

    // Elementos Nivel 2: Cinta de Cine
    private HorizontalScrollView scrollSecuencia;
    private LinearLayout contenedorCintaCine;
    private final List<Integer> secuenciaNumeros = new ArrayList<>();
    private final List<Integer> numerosEnGrilla = new ArrayList<>();
    private int indiceActualSecuencia = 0;

    // Elementos Carrera (Nivel 3)
    private View contenedorCarrera, pelotaCarrera;
    private int progresoCarrera = 0;
    private int colorObjetivoCarrera = -1;
    private final Handler handlerCarrera = new Handler(Looper.getMainLooper());
    private Runnable runnableTimeoutCarrera;
    
    // Configuración dinámica
    private int iteracionesParaGanar = 20;
    private long tiempoConfiguradoIntent = 0;

    // Elementos para Niveles Simon
    private GridLayout gridSimon, gridNumeros;
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
        actualizarInterfazVidas();

        mostrarPantallaIntro();
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
        
        layoutVidas = findViewById(R.id.layoutVidas);
        scrollSecuencia = findViewById(R.id.scrollSecuencia);
        contenedorCintaCine = findViewById(R.id.contenedorCintaCine);
        contenedorCarrera = findViewById(R.id.contenedorCarrera);
        pelotaCarrera = findViewById(R.id.pelotaCarrera);
        gridSimon = findViewById(R.id.gridSimon);
        gridNumeros = findViewById(R.id.gridNumeros);

        layoutOverlayMensaje = getLayoutInflater().inflate(R.layout.layout_mensaje_nivel, null);
        addContentView(layoutOverlayMensaje, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        layoutOverlayMensaje.setVisibility(View.GONE);

        txtMensajeTitulo = layoutOverlayMensaje.findViewById(R.id.txtMensajeTitulo);
        txtMensajeDescripcion = layoutOverlayMensaje.findViewById(R.id.txtMensajeDescripcion);
        imgIconoMensaje = layoutOverlayMensaje.findViewById(R.id.imgIconoMensaje);
        btnEntendido = layoutOverlayMensaje.findViewById(R.id.btnEntendido);
        contenedorEstadisticas = layoutOverlayMensaje.findViewById(R.id.contenedorEstadisticas);
        txtStatReaccion = layoutOverlayMensaje.findViewById(R.id.txtStatReaccion);
        txtStatPorcentaje = layoutOverlayMensaje.findViewById(R.id.txtStatPorcentaje);
    }

    private void mostrarPantallaIntro() {
        layoutOverlayMensaje.setVisibility(View.VISIBLE);
        imgIconoMensaje.setVisibility(View.GONE);
        contenedorEstadisticas.setVisibility(View.GONE);
        layoutOverlayMensaje.findViewById(R.id.layoutScoreMensaje).setVisibility(View.GONE);
        layoutOverlayMensaje.findViewById(R.id.layoutProgresoGlobal).setVisibility(View.GONE);
        btnEntendido.setText("¡EMPEZAR!");

        if (dificultadActual == Dificultad.FACIL) {
            txtMensajeTitulo.setText("NIVEL 1: SIMON");
            txtMensajeDescripcion.setText("Memoriza la secuencia de colores que parpadean.");
        } else if (dificultadActual == Dificultad.MEDIO) {
            txtMensajeTitulo.setText("NIVEL 2: NÚMEROS");
            txtMensajeDescripcion.setText("Toca los números en el orden que muestra la cinta de cine.");
        } else if (dificultadActual == Dificultad.DIFICIL) {
            txtMensajeTitulo.setText("NIVEL 3: REACCIÓN");
            txtMensajeDescripcion.setText("¡Rápido! Toca el color de la TINTA de la palabra.");
        }

        btnEntendido.setOnClickListener(v -> {
            layoutOverlayMensaje.setVisibility(View.GONE);
            iniciarJuego();
        });
    }

    private void mostrarPantallaVictoriaVida() {
        layoutOverlayMensaje.setVisibility(View.VISIBLE);
        actualizarOverlayConResultados("¡NIVEL SUPERADO!", "¡Excelente trabajo! Has ganado un corazón extra.");
        imgIconoMensaje.setVisibility(View.VISIBLE);
        imgIconoMensaje.setImageResource(android.R.drawable.btn_star_big_on);
        imgIconoMensaje.setColorFilter(Color.YELLOW);
        btnEntendido.setText("VOLVER AL MENÚ");
        btnEntendido.setOnClickListener(v -> finish());
    }

    private void mostrarPantallaPerderStats() {
        layoutOverlayMensaje.setVisibility(View.VISIBLE);
        actualizarOverlayConResultados("GAME OVER", "¡No te rindas! Aquí están tus resultados:");
        imgIconoMensaje.setVisibility(View.GONE);
        btnEntendido.setText("REINTENTAR");
        btnEntendido.setOnClickListener(v -> {
            layoutOverlayMensaje.setVisibility(View.GONE);
            iniciarJuego();
        });
    }

    private void actualizarOverlayConResultados(String titulo, String desc) {
        txtMensajeTitulo.setText(titulo);
        txtMensajeDescripcion.setText(desc);
        
        layoutOverlayMensaje.findViewById(R.id.layoutScoreMensaje).setVisibility(View.VISIBLE);
        contenedorEstadisticas.setVisibility(View.VISIBLE);
        layoutOverlayMensaje.findViewById(R.id.layoutProgresoGlobal).setVisibility(View.VISIBLE);

        // Score
        int puntaje = 0;
        if (dificultadActual == Dificultad.FACIL) puntaje = puntajeTotalSimon;
        else if (dificultadActual == Dificultad.MEDIO) puntaje = indiceActualSecuencia;
        else if (dificultadActual == Dificultad.DIFICIL) puntaje = progresoCarrera;
        ((TextView)layoutOverlayMensaje.findViewById(R.id.txtScoreValue)).setText(String.valueOf(puntaje));

        // Reacción
        long suma = 0;
        for (Long t : tiemposReaccion) suma += t;
        double promedio = tiemposReaccion.isEmpty() ? 0 : (suma / (double) tiemposReaccion.size()) / 1000.0;
        txtStatReaccion.setText(String.format(Locale.getDefault(), "%.1fs", promedio));

        // Porcentaje Nivel
        int porcentaje = (puntaje * 100) / iteracionesParaGanar;
        txtStatPorcentaje.setText(String.format(Locale.getDefault(), "%d%%", porcentaje));

        // Progreso Global
        int totalProgress = (MainActivity.nivelAlcanzado * 100) / 3;
        ProgressBar pb = layoutOverlayMensaje.findViewById(R.id.progressBarGlobal);
        pb.setProgress(totalProgress);

        View progLayout = layoutOverlayMensaje.findViewById(R.id.layoutProgresoGlobal);
        for(int i=0; i<((LinearLayout)progLayout).getChildCount(); i++){
            View child = ((LinearLayout)progLayout).getChildAt(i);
            if(child instanceof TextView) ((TextView)child).setText(totalProgress + "% TOTAL");
        }
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
            scrollSecuencia.setVisibility(View.VISIBLE);
            txtIndicadorColor.setVisibility(View.GONE);
            contenedorCarrera.setVisibility(View.GONE);
        } else if (dificultadActual == Dificultad.DIFICIL) {
            gridSimon.setVisibility(View.VISIBLE);
            gridNumeros.setVisibility(View.GONE);
            scrollSecuencia.setVisibility(View.GONE);
            txtIndicadorColor.setVisibility(View.VISIBLE);
            contenedorCarrera.setVisibility(View.VISIBLE);
            gridSimon.setBackgroundColor(Color.WHITE); 
        } else {
            gridSimon.setVisibility(View.VISIBLE);
            gridNumeros.setVisibility(View.GONE);
            scrollSecuencia.setVisibility(View.GONE);
            txtIndicadorColor.setVisibility(View.GONE);
            contenedorCarrera.setVisibility(View.GONE);
            gridSimon.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void configurarListeners() {
        btnPlay.setOnClickListener(v -> iniciarJuego());
        btnHome.setOnClickListener(v -> finish());
        btnSiguienteEtapa.setOnClickListener(v -> {
            btnSiguienteEtapa.setVisibility(View.GONE);
            if (dificultadActual == Dificultad.FACIL) siguienteRondaSimon();
        });
        
        for (int i = 0; i < 4; i++) {
            final int posView = i;
            cuadrosSimon[i].setOnClickListener(v -> {
                int colorIdx = mapaColores.get(posView);
                if (dificultadActual == Dificultad.DIFICIL) manejarToqueCarrera(colorIdx);
                else manejarToqueSimon(colorIdx);
            });
        }

        rootLayout.setOnLongClickListener(v -> {
            if (juegoActivo && dificultadActual == Dificultad.MEDIO) {
                int numeroBuscado = secuenciaNumeros.get(indiceActualSecuencia);
                if (!numerosEnGrilla.contains(numeroBuscado)) {
                    avanzarSecuenciaNumeros();
                    return true;
                } else {
                    intentarSeguirOPerder();
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
        tiemposReaccion.clear();
        
        if (tiempoConfiguradoIntent > 0) {
            tiempoTotalConfigurado = tiempoConfiguradoIntent;
        } else {
            tiempoTotalConfigurado = (dificultadActual == Dificultad.FACIL || dificultadActual == Dificultad.MEDIO) ? 25000 : 20000;
        }
        
        tiempoRestanteGlobal = tiempoTotalConfigurado;

        if (dificultadActual == Dificultad.MEDIO) {
            iniciarNivelNumeros();
        } else if (dificultadActual == Dificultad.DIFICIL) {
            iniciarNivelCarreraStroop();
        } else {
            iniciarNivelSimon();
        }
    }

    private void actualizarInterfazVidas() {
        layoutVidas.removeAllViews();
        for (int i = 0; i < vidasRestantes; i++) {
            ImageView corazon = new ImageView(this);
            corazon.setImageResource(android.R.drawable.btn_star_big_on); 
            corazon.setColorFilter(Color.RED);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
            params.setMargins(5, 0, 5, 0);
            corazon.setLayoutParams(params);
            layoutVidas.addView(corazon);
        }
    }

    private void registrarReaccion() {
        if (momentoInicioEstimulo > 0) {
            tiemposReaccion.add(System.currentTimeMillis() - momentoInicioEstimulo);
        }
    }

    // --- LÓGICA NIVEL 3: CARRERA STROOP ---

    private void iniciarNivelCarreraStroop() {
        progresoCarrera = 0;
        actualizarPosicionPelota();
        shuffleGridSimon();
        siguienteEstimuloCarrera();
        iniciarTemporizadorGlobal();
    }

    private void siguienteEstimuloCarrera() {
        colorObjetivoCarrera = random.nextInt(4);
        mostrarEstimuloPalabra(colorObjetivoCarrera);
        momentoInicioEstimulo = System.currentTimeMillis();
        reiniciarTimeoutCarrera();
    }

    private void mostrarEstimuloPalabra(int idxTarget) {
        String[] nombres = {"VERDE", "ROJO", "AMARILLO", "AZUL"};
        int[] colores = {Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN};
        txtIndicadorColor.setTextColor(colores[idxTarget]);
        int idxPalabra;
        do { idxPalabra = random.nextInt(4); } while (idxPalabra == idxTarget); 
        txtIndicadorColor.setText(nombres[idxPalabra]);
    }

    private void manejarToqueCarrera(int colorIdxTocado) {
        if (!juegoActivo) return;

        if (colorIdxTocado == colorObjetivoCarrera) {
            registrarReaccion();
            progresoCarrera++;
            txtPuntaje.setText(String.valueOf(progresoCarrera));
            if (progresoCarrera >= iteracionesParaGanar) {
                detenerCarrera();
                ganar();
            } else {
                shuffleGridSimon();
                siguienteEstimuloCarrera();
            }
        } else {
            if (progresoCarrera > 0) progresoCarrera--;
            txtPuntaje.setText(String.valueOf(progresoCarrera));
            actualizarPosicionPelota();
            animarView(rootLayout); 
            shuffleGridSimon();
            siguienteEstimuloCarrera();
        }
    }

    private void reiniciarTimeoutCarrera() {
        if (runnableTimeoutCarrera != null) handlerCarrera.removeCallbacks(runnableTimeoutCarrera);
        runnableTimeoutCarrera = () -> {
            if (juegoActivo) {
                if (progresoCarrera > 0) progresoCarrera--;
                txtPuntaje.setText(String.valueOf(progresoCarrera));
                actualizarPosicionPelota();
                reiniciarTimeoutCarrera();
            }
        };
        handlerCarrera.postDelayed(runnableTimeoutCarrera, 1500); 
        actualizarPosicionPelota();
    }

    private void detenerCarrera() {
        if (runnableTimeoutCarrera != null) handlerCarrera.removeCallbacks(runnableTimeoutCarrera);
    }

    private void actualizarPosicionPelota() {
        float maxAncho = contenedorCarrera.getWidth() - pelotaCarrera.getWidth();
        float paso = maxAncho / iteracionesParaGanar;
        float nuevaX = progresoCarrera * paso;
        ObjectAnimator animX = ObjectAnimator.ofFloat(pelotaCarrera, "translationX", pelotaCarrera.getTranslationX(), nuevaX);
        animX.setDuration(200);
        animX.start();
    }

    // --- LÓGICA NIVEL 2: NÚMEROS (CINTA DE CINE) ---

    private void iniciarNivelNumeros() {
        secuenciaNumeros.clear();
        indiceActualSecuencia = 0;
        for (int i = 0; i < iteracionesParaGanar; i++) {
            secuenciaNumeros.add(random.nextInt(10));
        }
        dibujarCintaCine();
        actualizarTableroNumeros();
        momentoInicioEstimulo = System.currentTimeMillis();
        iniciarTemporizadorGlobal();
    }

    private void dibujarCintaCine() {
        contenedorCintaCine.removeAllViews();
        for (int i = 0; i < iteracionesParaGanar; i++) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(secuenciaNumeros.get(i)));
            tv.setTextColor(i == indiceActualSecuencia ? Color.YELLOW : Color.GRAY);
            tv.setTextSize(34);
            tv.setPadding(40, 10, 40, 10);
            tv.setTypeface(null, i == indiceActualSecuencia ? Typeface.BOLD : Typeface.NORMAL);
            contenedorCintaCine.addView(tv);
        }
        desplazarCinta();
    }

    private void desplazarCinta() {
        handler.postDelayed(() -> {
            if (indiceActualSecuencia < contenedorCintaCine.getChildCount()) {
                View target = contenedorCintaCine.getChildAt(indiceActualSecuencia);
                int scrollX = target.getLeft() - (scrollSecuencia.getWidth() / 2) + (target.getWidth() / 2);
                ObjectAnimator animScroll = ObjectAnimator.ofInt(scrollSecuencia, "scrollX", scrollSecuencia.getScrollX(), scrollX);
                animScroll.setDuration(300);
                animScroll.start();
            }
        }, 100);
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

    private void manejarToqueNumero(int num) {
        if (!juegoActivo) return;
        if (num == secuenciaNumeros.get(indiceActualSecuencia)) {
            registrarReaccion();
            avanzarSecuenciaNumeros();
            momentoInicioEstimulo = System.currentTimeMillis();
        }
        else intentarSeguirOPerder();
    }

    private void avanzarSecuenciaNumeros() {
        indiceActualSecuencia++;
        txtPuntaje.setText(String.valueOf(indiceActualSecuencia));
        if (indiceActualSecuencia >= iteracionesParaGanar) ganar();
        else {
            for (int i = 0; i < contenedorCintaCine.getChildCount(); i++) {
                TextView tv = (TextView) contenedorCintaCine.getChildAt(i);
                tv.setTextColor(i == indiceActualSecuencia ? Color.YELLOW : Color.GRAY);
                tv.setTypeface(null, i == indiceActualSecuencia ? Typeface.BOLD : Typeface.NORMAL);
            }
            desplazarCinta();
            actualizarTableroNumeros();
        }
    }

    // --- LÓGICA NIVEL 1: SIMON ---

    private void iniciarNivelSimon() {
        secuenciaSimon.clear();
        puntajeTotalSimon = 0;
        etapaActualSimon = 1;
        actualizarEsteticaSimon();
        siguienteRondaSimon();
    }

    private void siguienteRondaSimon() {
        int limiteEtapa;
        if (etapaActualSimon == 1) limiteEtapa = 5;
        else if (etapaActualSimon == 2) limiteEtapa = 12; // 5 + 7
        else limiteEtapa = 20; // 12 + 8
        
        if (puntajeTotalSimon >= limiteEtapa) {
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
            if (etapaActualSimon == 2) tiempoTotalConfigurado = 30000; 
            else if (etapaActualSimon == 3) tiempoTotalConfigurado = 33000;
            tiempoRestanteGlobal = tiempoTotalConfigurado; 
        }
        btnSiguienteEtapa.setText("EMPEZAR ETAPA " + etapaActualSimon);
        btnSiguienteEtapa.setVisibility(View.VISIBLE);
        actualizarEsteticaSimon();
    }

    private void actualizarEsteticaSimon() {
        int colorFondo = (etapaActualSimon == 1) ? Color.BLACK : (etapaActualSimon == 2) ? Color.parseColor("#1A1A1A") : Color.parseColor("#111111");
        rootLayout.setBackgroundColor(colorFondo);
        resetGridSimon();
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

    private void shuffleGridSimon() {
        Collections.shuffle(mapaColores);
        for (int i = 0; i < 4; i++) {
            int colorIdx = mapaColores.get(i);
            cuadrosSimon[i].setBackgroundResource(simonResIds[colorIdx]);
        }
    }

    private void mostrarSecuenciaSimon() {
        mostrandoSecuenciaSimon = true;
        cancelarTemporizador();
        long tiempoEncendido = (etapaActualSimon == 1) ? 600 : (etapaActualSimon == 2) ? 450 : 300;
        long totalDelay = 500;
        for (int i = 0; i < secuenciaSimon.size(); i++) {
            int idxTarget = secuenciaSimon.get(i);
            handler.postDelayed(() -> animarColor(idxTarget), totalDelay);
            totalDelay += tiempoEncendido + 200;
        }
        handler.postDelayed(() -> {
            mostrandoSecuenciaSimon = false;
            momentoInicioEstimulo = System.currentTimeMillis();
            iniciarTemporizadorGlobal();
        }, totalDelay);
    }

    private void manejarToqueSimon(int colorIdx) {
        if (!juegoActivo || mostrandoSecuenciaSimon || btnSiguienteEtapa.getVisibility() == View.VISIBLE || layoutOverlayMensaje.getVisibility() == View.VISIBLE) return;
        
        int posView = mapaColores.indexOf(colorIdx);
        animarView(cuadrosSimon[posView]);

        entradaJugadorSimon.add(colorIdx);
        if (!entradaJugadorSimon.get(entradaJugadorSimon.size()-1).equals(secuenciaSimon.get(entradaJugadorSimon.size()-1))) {
            intentarSeguirOPerder();
            return;
        }
        
        registrarReaccion();
        momentoInicioEstimulo = System.currentTimeMillis();

        if (entradaJugadorSimon.size() == secuenciaSimon.size()) {
            puntajeTotalSimon++;
            txtPuntaje.setText(String.valueOf(puntajeTotalSimon));
            handler.postDelayed(this::siguienteRondaSimon, 700);
        }
    }

    // --- UTILIDADES ---

    private void intentarSeguirOPerder() {
        if (vidasRestantes > 0) {
            vidasRestantes--;
            actualizarInterfazVidas();
            Toast.makeText(this, "¡Vida perdida! Reiniciando nivel...", Toast.LENGTH_SHORT).show();
            cancelarTemporizador();
            detenerCarrera();
            handler.removeCallbacksAndMessages(null);
            iniciarJuego(); 
        } else {
            perder();
        }
    }

    private void ganar() {
        juegoActivo = false;
        cancelarTemporizador();
        detenerCarrera();
        if (dificultadActual == Dificultad.FACIL && MainActivity.nivelAlcanzado < 2) {
            MainActivity.nivelAlcanzado = 2; vidasRestantes++;
        } else if (dificultadActual == Dificultad.MEDIO && MainActivity.nivelAlcanzado < 3) {
            MainActivity.nivelAlcanzado = 3; vidasRestantes++;
        }
        mostrarPantallaVictoriaVida();
    }

    private void perder() {
        juegoActivo = false;
        cancelarTemporizador();
        detenerCarrera();
        MainActivity.nivelAlcanzado = 1;
        vidasRestantes = 0;
        mostrarPantallaPerderStats();
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
            public void onFinish() { intentarSeguirOPerder(); }
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

    private void animarColor(int idx) { animarView(cuadrosSimon[idx]); }
}
