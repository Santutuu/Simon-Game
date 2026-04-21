package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBackHome;
    private TextView txtTimeValue;
    private int reactionTime = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBackHome = findViewById(R.id.btnBackHome);
        txtTimeValue = findViewById(R.id.txtTimeValue);
        ImageButton btnTimeMinus = findViewById(R.id.btnTimeMinus);
        ImageButton btnTimePlus = findViewById(R.id.btnTimePlus);

        btnBackHome.setOnClickListener(v -> {
            finish();
        });

        btnTimeMinus.setOnClickListener(v -> {
            if (reactionTime > 5) {
                reactionTime -= 5;
                updateTimeText();
            }
        });

        btnTimePlus.setOnClickListener(v -> {
            if (reactionTime < 120) {
                reactionTime += 5;
                updateTimeText();
            }
        });
    }

    private void updateTimeText() {
        txtTimeValue.setText(reactionTime + "s");
    }
}