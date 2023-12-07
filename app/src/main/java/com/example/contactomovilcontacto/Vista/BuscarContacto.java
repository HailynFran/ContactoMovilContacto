package com.example.contactomovilcontacto.Vista;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contactomovilcontacto.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BuscarContacto extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ListView listViewContactos;
    private ArrayList<String> contactosList;
    private ArrayList<String> originalContactosList;
    private ArrayAdapter<String> adapter;
    private EditText editTextBusqueda;
    private String uidUsuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_contacto);

        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");

        listViewContactos = findViewById(R.id.listViewContactos);
        contactosList = new ArrayList<>();
        originalContactosList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, contactosList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }
        };
        listViewContactos.setAdapter(adapter);

        editTextBusqueda = findViewById(R.id.editTextBuscar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uidUsuarioActual = user.getUid();
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactosList.clear();
                originalContactosList.clear();

                for (DataSnapshot usuarioSnapshot : dataSnapshot.getChildren()) {
                    String contactoNombre = usuarioSnapshot.child("nombre").getValue(String.class);
                    String contactoCorreo = usuarioSnapshot.child("correo").getValue(String.class);

                    if (contactoNombre != null && contactoCorreo != null) {
                        String infoContacto = contactoNombre + "\n" + contactoCorreo;
                        contactosList.add(infoContacto);
                        originalContactosList.add(infoContacto);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        listViewContactos.setOnItemClickListener((parent, view, position, id) -> {
            String contactoSeleccionado = contactosList.get(position);

            DatabaseReference userContactosRef = FirebaseDatabase.getInstance().getReference("usuarios")
                    .child(uidUsuarioActual)
                    .child("contactos");

            userContactosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean contactoYaExiste = false;

                    for (DataSnapshot contactoSnapshot : dataSnapshot.getChildren()) {
                        String contactoExistente = contactoSnapshot.getValue(String.class);

                        if (contactoExistente != null && contactoExistente.equals(contactoSeleccionado)) {
                            contactoYaExiste = true;
                            break;
                        }
                    }

                    if (!contactoYaExiste) {
                        userContactosRef.child("contacto_" + position).setValue(contactoSeleccionado);
                        Toast.makeText(BuscarContacto.this, "Contacto agregado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(BuscarContacto.this, "El contacto ya está en tu lista", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        });

        editTextBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterResults(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void filterResults(String searchText) {
        ArrayList<String> filteredList = new ArrayList<>();

        if (searchText.isEmpty()) {
            filteredList.addAll(originalContactosList);
        } else {
            for (String contactoInfo : originalContactosList) {
                String[] partes = contactoInfo.split("\n");
                if (partes.length == 2 &&
                        (partes[0].toLowerCase().startsWith(searchText.toLowerCase()) || partes[1].toLowerCase().startsWith(searchText.toLowerCase()))) {
                    filteredList.add(contactoInfo);
                }
            }
        }

        adapter.clear();
        adapter.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}
