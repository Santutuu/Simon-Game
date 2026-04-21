package com.example.myapplication;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class PantallaIntermediaActivity extends AppCompatActivity {

    private Button btnReiniciar;
    private ImageButton btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_intermedia);

        btnReiniciar = findViewById(R.id.btnReiniciar);
        btnHome = findViewById(R.id.btnHome);

        btnReiniciar.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaIntermediaActivity.this, JuegoActivity.class);
            startActivity(intent);
            finish();
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaIntermediaActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}