package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBackHome;
    private TextView txtTimeValue;
    private EditText etUsername;
    private int reactionTime = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBackHome = findViewById(R.id.btnBackHome);
        txtTimeValue = findViewById(R.id.txtTimeValue);
        etUsername = findViewById(R.id.etUsername);
        ImageButton btnTimeMinus = findViewById(R.id.btnTimeMinus);
        ImageButton btnTimePlus = findViewById(R.id.btnTimePlus);

        etUsername.setText(ResultadosManager.getUsuarioActual(this));
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                ResultadosManager.setUsuarioActual(SettingsActivity.this, s.toString());
            }
        });

        btnBackHome.setOnClickListener(v -> finish());

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
