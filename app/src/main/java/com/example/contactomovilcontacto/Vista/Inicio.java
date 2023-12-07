package com.example.contactomovilcontacto.Vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.contactomovilcontacto.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Inicio extends AppCompatActivity {

    private ArrayList<String> contactosListInicio;
    private ArrayAdapter<String> adapter;
    private ListView listViewInicio;
    private static final String PREFS_NAME = "MisPreferencias";
    private String uidUsuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        contactosListInicio = new ArrayList<>();
        listViewInicio = findViewById(R.id.listViewInicio);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactosListInicio);
        listViewInicio.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uidUsuarioActual = user.getUid();
        }

        listViewInicio.setOnItemClickListener((parent, view, position, id) -> {
            String contactoSeleccionado = contactosListInicio.get(position);
            iniciarActividadChat(contactoSeleccionado);
        });

        loadContactList();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(Inicio.this, BuscarContacto.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContactList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inicio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_opcion1) {
            Intent i = new Intent(Inicio.this, MiCuenta.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_opcion2) {
            mostrarDialogoEliminarContactos();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void iniciarActividadChat(String nombreContacto) {
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra("nombreContacto", nombreContacto);
        startActivity(intent);
    }

    private void mostrarDialogoEliminarContactos() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_eliminar_contacto, null);
        builder.setView(view);

        ListView listViewContactos = view.findViewById(R.id.listViewContactos);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, contactosListInicio);
        listViewContactos.setAdapter(adapter);
        listViewContactos.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            eliminarContactosSeleccionados(listViewContactos);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
        });

        builder.show();
    }

    private void eliminarContactosSeleccionados(ListView listView) {
        SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();

        DatabaseReference userContactosRef = FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uidUsuarioActual)
                .child("contactos");

        ArrayList<String> contactosAEliminar = new ArrayList<>();

        for (int i = contactosListInicio.size() - 1; i >= 0; i--) {
            if (checkedItemPositions.get(i)) {
                String contacto = contactosListInicio.get(i);

                try {
                    userContactosRef.child(contacto).removeValue();
                    Log.d("Inicio", "Contacto eliminado de Firebase: " + contacto);
                } catch (Exception e) {
                    Log.e("Inicio", "Error al eliminar contacto de Firebase: " + e.getMessage());
                }

                contactosAEliminar.add(contacto);
            }
        }

        contactosListInicio.removeAll(contactosAEliminar);

        Toast.makeText(this, "Contactos seleccionados eliminados", Toast.LENGTH_SHORT).show();

        adapter.notifyDataSetChanged();

        saveContactList();
    }

    private void saveContactList() {
        DatabaseReference userContactosRef = FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uidUsuarioActual)
                .child("contactos");
        userContactosRef.setValue(contactosListInicio);
    }

    private void loadContactList() {
        DatabaseReference userContactosRef = FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uidUsuarioActual)
                .child("contactos");
        userContactosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    contactosListInicio.clear();
                    for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                        String contacto = contactSnapshot.getValue(String.class);
                        contactosListInicio.add(contacto);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Inicio", "Error al cargar la lista de contactos: " + databaseError.getMessage());
            }
        });
    }
}
