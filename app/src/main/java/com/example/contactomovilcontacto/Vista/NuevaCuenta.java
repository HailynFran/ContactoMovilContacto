package com.example.contactomovilcontacto.Vista;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contactomovilcontacto.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class NuevaCuenta extends AppCompatActivity {

    Button btnAdd;
    TextView volverLogin;
    EditText correo, contraseña, usuario, confirmarContraseña;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_cuenta);

        usuario = findViewById(R.id.editTextUsuario);
        correo = findViewById(R.id.editTextCorreo);
        contraseña = findViewById(R.id.editTextContraseña);
        confirmarContraseña = findViewById(R.id.editTextConfirmPass);
        btnAdd = findViewById(R.id.btnAdd);
        volverLogin = findViewById(R.id.txtIrLogin);

        firebaseAuth = FirebaseAuth.getInstance();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComprobarDatos();
            }
        });

        volverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(NuevaCuenta.this, InicioSesion.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        });
    }

    private void ComprobarDatos() {
        String nombre = usuario.getText().toString().trim();
        String email = correo.getText().toString().trim();
        String password = contraseña.getText().toString();
        String confPassword = confirmarContraseña.getText().toString();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confPassword)) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        VerificarCorreoExistente(email, password, nombre);
    }

    private void VerificarCorreoExistente(final String correo, final String contraseña, final String nombre) {
        firebaseAuth.fetchSignInMethodsForEmail(correo)
                .addOnSuccessListener(signInMethodsResult -> {
                    if (signInMethodsResult.getSignInMethods().isEmpty()) {
                        AddUser(correo, contraseña, nombre);
                    } else {
                        Toast.makeText(NuevaCuenta.this, "El correo electrónico ya está en uso. Por favor, elija otro.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NuevaCuenta.this, "Error al verificar el correo electrónico: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void AddUser(String correo, String contraseña, String nombre) {
        firebaseAuth.createUserWithEmailAndPassword(correo, contraseña)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(NuevaCuenta.this, "Se ha registrado exitosamente", Toast.LENGTH_SHORT).show();

                        String imagenBase64 = obtenerImagenPredeterminadaBase64();

                        guardarInformacionUsuario(authResult.getUser().getUid(), nombre, correo, imagenBase64);

                        Intent i = new Intent(NuevaCuenta.this, Inicio.class);
                        startActivity(i);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e.getMessage() != null) {
                            if (e.getMessage().contains("The email address is already in use")) {
                                Toast.makeText(NuevaCuenta.this, "El correo ya tiene una cuenta creada", Toast.LENGTH_SHORT).show();
                            } else if (e.getMessage().contains("Password should be at least 6 characters")) {
                                Toast.makeText(NuevaCuenta.this, "La contraseña debe tener al menos seis dígitos", Toast.LENGTH_SHORT).show();
                            } else if (e.getMessage().contains("The given password is invalid")) {
                                Toast.makeText(NuevaCuenta.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NuevaCuenta.this, "Error en el registro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(NuevaCuenta.this, "Error en el registro. " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String obtenerImagenPredeterminadaBase64() {
        Drawable drawable = getResources().getDrawable(R.drawable.user);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void guardarInformacionUsuario(String userId, String nombre, String correo, String imagenBase64) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId);

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("nombre", nombre);
        userData.put("correo", correo);
        userData.put("imagen", imagenBase64);

        databaseReference.setValue(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
}
