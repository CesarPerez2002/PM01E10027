package com.app.pmoe10027;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListaContactosActivity extends AppCompatActivity {

    private EditText etBuscar;
    private ListView lvContactos;
    private TextView tvSinContactos;

    private DatabaseHelper dbHelper;
    private List<Contactos> listaCompleta = new ArrayList<>();
    private List<Contactos> listaFiltrada = new ArrayList<>();
    private ContactoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        dbHelper = new DatabaseHelper(this);

        etBuscar = findViewById(R.id.etBuscar);
        lvContactos = findViewById(R.id.lvContactos);
        tvSinContactos = findViewById(R.id.tvSinContactos);

        // TextWatcher para el buscador en tiempo real
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarContactos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    private void cargarContactos() {
        listaCompleta = dbHelper.obtenerTodosContactos();
        String textoBusqueda = etBuscar.getText().toString();
        if (textoBusqueda.isEmpty()) {
            actualizarLista(listaCompleta);
        } else {
            filtrarContactos(textoBusqueda);
        }
    }

    private void filtrarContactos(String texto) {
        listaFiltrada.clear();
        String busqueda = texto.toLowerCase().trim();

        if (busqueda.isEmpty()) {
            listaFiltrada.addAll(listaCompleta);
        } else {
            for (Contactos c : listaCompleta) {
                if (c.getNombre().toLowerCase().contains(busqueda) ||
                        c.getTelefono().contains(busqueda)) {
                    listaFiltrada.add(c);
                }
            }
        }
        actualizarLista(listaFiltrada);
    }

    private void actualizarLista(List<Contactos> lista) {
        if (lista.isEmpty()) {
            tvSinContactos.setVisibility(View.VISIBLE);
            lvContactos.setVisibility(View.GONE);
        } else {
            tvSinContactos.setVisibility(View.GONE);
            lvContactos.setVisibility(View.VISIBLE);
        }

        adapter = new ContactoAdapter(this, lista, new ContactoAdapter.OnContactoListener() {
            @Override
            public void onVerImagen(Contactos contacto) {
                mostrarImagenAmpliada(contacto);
            }

            @Override
            public void onLlamar(Contactos contacto) {
                Intent intent = new Intent(ListaContactosActivity.this, LlamarActivity.class);
                intent.putExtra("contacto_id", contacto.getId());
                intent.putExtra("nombre", contacto.getNombre());
                intent.putExtra("pais", contacto.getPais());
                intent.putExtra("telefono", contacto.getTelefono());
                intent.putExtra("imagen", contacto.getImagen());
                startActivity(intent);
            }

            @Override
            public void onCompartir(Contactos contacto) {
                compartirContacto(contacto);
            }

            @Override
            public void onEditar(Contactos contacto) {
                // Carga los datos en el formulario de registro de MainActivity
                Intent intent = new Intent(ListaContactosActivity.this, MainActivity.class);
                intent.putExtra("contacto_id", contacto.getId());
                intent.putExtra("pais", contacto.getPais());
                intent.putExtra("nombre", contacto.getNombre());
                intent.putExtra("telefono", contacto.getTelefono());
                intent.putExtra("nota", contacto.getNota());
                intent.putExtra("imagen", contacto.getImagen());
                startActivity(intent);
            }

            @Override
            public void onEliminar(Contactos contacto) {
                confirmarEliminacion(contacto);
            }
        });

        lvContactos.setAdapter(adapter);
    }

    private void mostrarImagenAmpliada(Contactos contacto) {
        if (contacto.getImagen() == null || contacto.getImagen().isEmpty()) {
            Toast.makeText(this, "El contacto no tiene fotografía guardada.", Toast.LENGTH_SHORT).show();
            return;
        }

        File imgFile = new File(contacto.getImagen());
        if (!imgFile.exists()) {
            Toast.makeText(this, "No se encontró el archivo de la imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(20, 20, 20, 20);

        new AlertDialog.Builder(this)
                .setTitle("Fotografía: " + contacto.getNombre())
                .setView(imageView)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void compartirContacto(Contactos contacto) {
        String numeroCompleto = LlamarActivity.construirNumeroCompleto(contacto.getPais(), contacto.getTelefono());
        String mensaje = "Contacto: " + contacto.getNombre() + "\nTeléfono: " + numeroCompleto;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mensaje);

        startActivity(Intent.createChooser(shareIntent, "Compartir contacto vía"));
    }

    private void confirmarEliminacion(Contactos contacto) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Contacto")
                .setMessage("¿Está seguro de que desea eliminar a " + contacto.getNombre() + "?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    int filasEliminadas = dbHelper.eliminarContacto(contacto.getId());
                    if (filasEliminadas > 0) {
                        Toast.makeText(ListaContactosActivity.this, "Contacto eliminado correctamente", Toast.LENGTH_SHORT).show();
                        cargarContactos();
                    } else {
                        Toast.makeText(ListaContactosActivity.this, "Error al eliminar contacto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
