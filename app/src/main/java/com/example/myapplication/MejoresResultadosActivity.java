package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Locale;

public class MejoresResultadosActivity extends AppCompatActivity {

    private LinearLayout containerResultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mejores_resultados);

        containerResultados = findViewById(R.id.containerResultados);
        ImageButton btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());

        cargarResultados();
    }

    private void cargarResultados() {
        List<Resultado> lista = ResultadosManager.obtenerResultados(this);
        containerResultados.removeAllViews();

        for (int i = 0; i < lista.size(); i++) {
            Resultado res = lista.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_mejor_resultado, containerResultados, false);

            TextView txtPosicion = itemView.findViewById(R.id.txtPosicion);
            TextView txtNombre = itemView.findViewById(R.id.txtNombreJugador);
            TextView txtReaccion = itemView.findViewById(R.id.txtReaccionMedia);
            TextView txtPuntaje = itemView.findViewById(R.id.txtPuntajeFinal);

            txtPosicion.setText(String.valueOf(i + 1));
            txtNombre.setText(res.getUsuario());
            txtReaccion.setText(String.format(Locale.getDefault(), "Reaccion Media: %.2f seg", res.getReaccionMedia()));
            txtPuntaje.setText(String.valueOf(res.getPuntaje()));

            containerResultados.addView(itemView);
        }
    }
}
