package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultadosManager {
    private static final String PREFS_NAME = "SimonGamePrefs";
    private static final String KEY_RESULTADOS = "lista_resultados";
    private static final String KEY_USUARIO_ACTUAL = "usuario_actual";

    public static void guardarResultado(Context context, int puntaje, double reaccionMedia) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usuario = prefs.getString(KEY_USUARIO_ACTUAL, "Jugador");
        
        List<Resultado> resultados = obtenerResultados(context);
        resultados.add(new Resultado(usuario, puntaje, reaccionMedia));
        
        // Ordenar: 1º Puntaje (desc), 2º Reaccion (asc)
        Collections.sort(resultados, (r1, r2) -> {
            if (r2.getPuntaje() != r1.getPuntaje()) {
                return Integer.compare(r2.getPuntaje(), r1.getPuntaje());
            }
            return Double.compare(r1.getReaccionMedia(), r2.getReaccionMedia());
        });

        // Guardar solo los top 10 o similar si se desea, por ahora todos
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_RESULTADOS, new Gson().toJson(resultados));
        editor.apply();
    }

    public static List<Resultado> obtenerResultados(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_RESULTADOS, null);
        if (json == null) return new ArrayList<>();
        
        Type type = new TypeToken<List<Resultado>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void setUsuarioActual(Context context, String usuario) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USUARIO_ACTUAL, usuario).apply();
    }
    
    public static String getUsuarioActual(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USUARIO_ACTUAL, "Jugador");
    }
}
