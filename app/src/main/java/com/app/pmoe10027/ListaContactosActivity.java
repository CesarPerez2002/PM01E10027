package com.app.pmoe10027;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ListaContactosActivity extends AppCompatActivity {

    private EditText etBuscarId;
    private Button btnBuscar, btnLimpiarBusqueda;
    private ListView lvContactos;
    private TextView tvSinContactos;

    private DatabaseHelper dbHelper;
    private List<Contactos> listaContactos;
    private ContactoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        dbHelper = new DatabaseHelper(this);

        etBuscarId = findViewById(R.id.etBuscarId);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnLimpiarBusqueda = findViewById(R.id.btnLimpiarBusqueda);
        lvContactos = findViewById(R.id.lvContactos);
        tvSinContactos = findViewById(R.id.tvSinContactos);

        cargarContactos();

        btnBuscar.setOnClickListener(v -> buscarPorId());
        btnLimpiarBusqueda.setOnClickListener(v -> {
            etBuscarId.setText("");
            cargarContactos();
        });
    }

    private void cargarContactos() {
        listaContactos = dbHelper.obtenerTodosContactos();
        actualizarLista(listaContactos);
    }

    private void buscarPorId() {
        String idTexto = etBuscarId.getText().toString().trim();
        if (idTexto.isEmpty()) {
            Toast.makeText(this, "Ingrese un ID para buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = Integer.parseInt(idTexto);
        Contactos contactoEncontrado = dbHelper.obtenerContactoPorId(id);

        List<Contactos> resultado = new ArrayList<>();
        if (contactoEncontrado != null) {
            resultado.add(contactoEncontrado);
        } else {
            Toast.makeText(this, "No se encontró ningún contacto con ID: " + id, Toast.LENGTH_SHORT).show();
        }

        actualizarLista(resultado);
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
            public void onEditar(Contactos contacto) {
                mostrarDialogoEditar(contacto);
            }

            @Override
            public void onEliminar(Contactos contacto) {
                confirmarEliminacion(contacto);
            }
        });

        lvContactos.setAdapter(adapter);
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
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditar(Contactos contacto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_contacto, null);
        builder.setView(dialogView);

        Spinner spPaisDialog = dialogView.findViewById(R.id.spPaisDialog);
        EditText etNombreDialog = dialogView.findViewById(R.id.etNombreDialog);
        EditText etTelefonoDialog = dialogView.findViewById(R.id.etTelefonoDialog);
        EditText etNotaDialog = dialogView.findViewById(R.id.etNotaDialog);

        // Llenar datos actuales
        etNombreDialog.setText(contacto.getNombre());
        etTelefonoDialog.setText(contacto.getTelefono());
        etNotaDialog.setText(contacto.getNota());

        // Seleccionar país en el Spinner
        ArrayAdapter<CharSequence> adapterPaises = ArrayAdapter.createFromResource(
                this, R.array.paises_array, android.R.layout.simple_spinner_item);
        adapterPaises.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaisDialog.setAdapter(adapterPaises);

        int posPais = adapterPaises.getPosition(contacto.getPais());
        if (posPais >= 0) {
            spPaisDialog.setSelection(posPais);
        }

        builder.setTitle("Editar Contacto ID: " + contacto.getId());
        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String nuevoPais = spPaisDialog.getSelectedItem().toString();
            int posSeleccionada = spPaisDialog.getSelectedItemPosition();
            String nuevoNombre = etNombreDialog.getText().toString().trim();
            String nuevoTelefono = etTelefonoDialog.getText().toString().trim();
            String nuevaNota = etNotaDialog.getText().toString().trim();

            if (posSeleccionada == 0) {
                Toast.makeText(ListaContactosActivity.this, "Debe seleccionar un país válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nuevoNombre.isEmpty() || !nuevoNombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
                Toast.makeText(ListaContactosActivity.this, "Nombre inválido o vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nuevoTelefono.isEmpty() || !nuevoTelefono.matches("^[0-9]+$")) {
                Toast.makeText(ListaContactosActivity.this, "Teléfono inválido o vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nuevaNota.isEmpty()) {
                Toast.makeText(ListaContactosActivity.this, "La nota es obligatoria", Toast.LENGTH_SHORT).show();
                return;
            }

            contacto.setPais(nuevoPais);
            contacto.setNombre(nuevoNombre);
            contacto.setTelefono(nuevoTelefono);
            contacto.setNota(nuevaNota);

            int filas = dbHelper.actualizarContacto(contacto);
            if (filas > 0) {
                Toast.makeText(ListaContactosActivity.this, "Contacto actualizado correctamente", Toast.LENGTH_SHORT).show();
                cargarContactos();
            } else {
                Toast.makeText(ListaContactosActivity.this, "Error al actualizar contacto", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
