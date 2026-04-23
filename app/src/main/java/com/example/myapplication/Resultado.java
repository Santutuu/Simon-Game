package com.example.myapplication;

public class Resultado {
    private String usuario;
    private int puntaje;
    private double reaccionMedia;

    public Resultado(String usuario, int puntaje, double reaccionMedia) {
        this.usuario = usuario;
        this.puntaje = puntaje;
        this.reaccionMedia = reaccionMedia;
    }

    public String getUsuario() {
        return (usuario == null || usuario.isEmpty()) ? "Jugador" : usuario;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public double getReaccionMedia() {
        return reaccionMedia;
    }
}
