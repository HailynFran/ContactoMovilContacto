package com.example.contactomovilcontacto.Vista;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.contactomovilcontacto.Controlador.MqttManager;
import com.example.contactomovilcontacto.R;

public class Chat extends AppCompatActivity {

    private TextView mensajeTextView;
    private EditText editTextMensaje;
    private Button botonEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inicializar los elementos de la interfaz de usuario
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mensajeTextView = findViewById(R.id.mensajeTextView);
        editTextMensaje = findViewById(R.id.editTextMensaje);
        botonEnviar = findViewById(R.id.botonEnviar);

        // Se obtienen los nosmbre del LIstView
        String nombreContacto = getIntent().getStringExtra("nombreContacto");

        if (nombreContacto != null && !nombreContacto.isEmpty()) {
            getSupportActionBar().setTitle(nombreContacto);
        }

        botonEnviar.setOnClickListener(v -> {
            String mensaje = editTextMensaje.getText().toString();
            if (!mensaje.isEmpty()) {
                MqttManager.getInstance(this).publishMessage("topic/usuario1", mensaje);
                editTextMensaje.setText("");
            }
        });
    }
}
