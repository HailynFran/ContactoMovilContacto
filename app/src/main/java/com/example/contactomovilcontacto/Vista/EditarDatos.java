package com.example.contactomovilcontacto.Vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contactomovilcontacto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditarDatos extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_datos);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        Button buttonCambiarContrasena = findViewById(R.id.btnCambiarContrasena);
        buttonCambiarContrasena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });

        Button buttonCambiarNombre = findViewById(R.id.btnCambiarNombre);
        buttonCambiarNombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeNameDialog();
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_contrasena_layout, null);

        EditText editText1 = dialogView.findViewById(R.id.editTextContrasenaActual);
        EditText editText2 = dialogView.findViewById(R.id.editTextContrasenaNueva);
        EditText editText3 = dialogView.findViewById(R.id.editTextConfirmarContrasenaNueva);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialogInterface, i) -> {
                    String contraseñaActualIngresada = editText1.getText().toString();
                    String nuevaContraseña = editText2.getText().toString();
                    String confirmarContraseña = editText3.getText().toString();

                    if (!nuevaContraseña.equals(confirmarContraseña)) {
                        showToast("Las contraseñas nuevas no coinciden");
                    } else {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            mAuth.signInWithEmailAndPassword(user.getEmail(), contraseñaActualIngresada)
                                    .addOnCompleteListener(this, task -> {
                                        if (task.isSuccessful()) {
                                            updatePassword(nuevaContraseña);
                                        } else {
                                            showToast("La contraseña actual ingresada no es correcta");
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("Cancelar", (dialogInterface, i) -> {
                    // Acción al presionar Cancelar
                })
                .create();

        dialog.show();
    }

    private void updatePassword(String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Contraseña cambiada con éxito");
                        } else {
                            showToast("Error al cambiar la contraseña: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void showChangeNameDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nombre_layout, null);

        TextView textViewChangeName = dialogView.findViewById(R.id.textViewChangeName);
        EditText editTextNewName = dialogView.findViewById(R.id.editTextNewName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialogInterface, i) -> {
                    String newName = editTextNewName.getText().toString();
                    updateDisplayName(newName);
                })
                .setNegativeButton("Cancelar", (dialogInterface, i) -> {
                    // se puede poner las acciones al presionar el boton
                })
                .create();

        dialog.show();
    }

    private void updateDisplayName(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateNameInDatabase(newName, user.getUid());
                        } else {
                            showToast("Error al cambiar el nombre en Authentication: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void updateNameInDatabase(String newName, String userId) {
        databaseReference.child(userId).child("nombre").setValue(newName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Nombre cambiado con éxito");
                    } else {
                        showToast("Error al cambiar el nombre en Realtime Database: " + task.getException().getMessage());
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
