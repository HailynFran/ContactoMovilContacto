package com.example.contactomovilcontacto.Vista;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contactomovilcontacto.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class InicioSesion extends AppCompatActivity {

    TextView crearCuenta;


    EditText correo, contraseña;
    Button ingresar;

    FirebaseAuth firebaseAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        crearCuenta = findViewById(R.id.txtCrearCuenta);
        correo= findViewById(R.id.editTextIgresarCorreo);
        contraseña= findViewById(R.id.editTextIngresarContrasena);
        ingresar = findViewById(R.id.btnIngresar);
        firebaseAuth = FirebaseAuth.getInstance();


        crearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent (InicioSesion.this, NuevaCuenta.class);
                startActivity(i);
            }
        });

        ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });
    }

    private void iniciarSesion(){
        String correos = correo.getText().toString().trim();
        String password = contraseña.getText().toString();

        if (correos.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(correos, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(InicioSesion.this, "Se ha iniciado sesion exitosamente",Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(InicioSesion.this,Inicio.class);
                        startActivity(i);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InicioSesion.this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}