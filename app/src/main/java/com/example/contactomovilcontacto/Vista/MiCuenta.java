package com.example.contactomovilcontacto.Vista;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contactomovilcontacto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MiCuenta extends AppCompatActivity {

    private static final String TAG = "MiCuenta";
    private static final int GALLERY_REQUEST_CODE = 123;
    private static final int STORAGE_PERMISSION_CODE = 456;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private AlertDialog dialog;

    ImageView imageView;
    Button editarDatos;
    TextView textViewNombre;
    TextView textViewCorreo;

    private String imagenActualEnFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_cuenta);

        editarDatos = findViewById(R.id.btnEditarDatos);
        textViewNombre = findViewById(R.id.textViewNombre);
        textViewCorreo = findViewById(R.id.textViewCorreo);
        imageView = findViewById(R.id.imageView5);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nombreUsuario = dataSnapshot.child("nombre").getValue(String.class);
                        if (nombreUsuario != null) {
                            textViewNombre.setText(nombreUsuario);
                        }

                        imagenActualEnFirebase = dataSnapshot.child("imagen").getValue(String.class);
                        cargarImagenDesdeFirebase(imagenActualEnFirebase);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "Error al leer la base de datos: " + databaseError.getMessage());
                }
            });

            String correoUsuario = user.getEmail();
            if (correoUsuario != null) {
                textViewCorreo.setText(correoUsuario);
            }
        } else {
            Log.d(TAG, "El usuario es nulo");
        }

        ImageButton imageView = findViewById(R.id.imageView5);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoImagenGrande();
            }
        });

        editarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MiCuenta.this, EditarDatos.class);
                startActivity(i);
            }
        });

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cerrarSesion();
            }
        });

        Button btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);
        btnEliminarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogoAdvertencia();
            }
        });
    }

    private void cerrarSesion() {
        if (mAuth != null) {
            mAuth.signOut();
        }

        Intent intent = new Intent(this, InicioSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }

    private void mostrarDialogoAdvertencia() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_advertencia, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        TextView textViewConfirmacion = dialogView.findViewById(R.id.textViewConfirmacion);
        Button btnEliminar = dialogView.findViewById(R.id.btnEliminar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        textViewConfirmacion.setText("¿Está seguro de eliminar todos los datos? Luego de eliminar no podrá volver a tener sus datos");

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (databaseReference != null) {
                    databaseReference.removeValue();
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(MiCuenta.this,InicioSesion.class);
                            startActivity(i);
                            Toast.makeText(getApplicationContext(), "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error al eliminar el usuario", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                dialog.dismiss();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    private void mostrarDialogoImagenGrande() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_imagen_grande, null);

        ImageView imageViewImagenGrande = view.findViewById(R.id.imageViewEnGrande);
        Button btnCambiarImagen = view.findViewById(R.id.btnCambiarImagen);

        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        imageViewImagenGrande.setImageBitmap(bitmap);

        btnCambiarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Para abrir la galeria
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
                dialog.dismiss();
            }
        });

        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }

    private void cargarImagenDesdeFirebase(String base64String) {
        try {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            int nuevoTamaño = 300;
            decodedByte = escalarRecortarImagen(decodedByte, nuevoTamaño);

            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);

            imageView.setImageBitmap(decodedByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                int nuevoTamaño = 300; //
                selectedImage = escalarRecortarImagen(selectedImage, nuevoTamaño);

                String base64Image = bitmapToBase64(selectedImage);

                if (databaseReference != null) {
                    databaseReference.child("imagen").setValue(base64Image);
                }

                imagenActualEnFirebase = base64Image;

                imageView.setImageBitmap(selectedImage);

                dialog.dismiss();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap escalarRecortarImagen(Bitmap originalBitmap, int nuevoTamaño) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        int tamañoCuadrado = Math.min(width, height);

        int x = (width - tamañoCuadrado) / 2;
        int y = (height - tamañoCuadrado) / 2;

        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(0, 0, width, height), new RectF(0, 0, tamañoCuadrado, tamañoCuadrado), Matrix.ScaleToFit.CENTER);

        Bitmap cuadradoBitmap = Bitmap.createBitmap(originalBitmap, x, y, tamañoCuadrado, tamañoCuadrado, matrix, true);

        return Bitmap.createScaledBitmap(cuadradoBitmap, nuevoTamaño, nuevoTamaño, true);
    }
}
