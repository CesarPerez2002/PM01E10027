package com.app.pmoe10027;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Spinner spPais;
    private EditText etNombre, etTelefono, etNota;
    private ImageView imgPreview;
    private Button btnTomarFoto, btnGaleria, btnGuardar, btnVerContactos;

    private String rutaImagenSeleccionada = "";
    private DatabaseHelper dbHelper;

    // Lanzador para la Cámara
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        if (bitmap != null) {
                            imgPreview.setImageBitmap(bitmap);
                            rutaImagenSeleccionada = guardarImagenInterna(bitmap);
                        }
                    }
                }
            }
    );

    // Lanzador para la Galería
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uriImagen = result.getData().getData();
                    if (uriImagen != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uriImagen);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            imgPreview.setImageBitmap(bitmap);
                            rutaImagenSeleccionada = guardarImagenInterna(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mostrarAlerta("Error", "No se pudo cargar la imagen seleccionada.");
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar helper de BD
        dbHelper = new DatabaseHelper(this);

        // Referenciar Vistas
        spPais = findViewById(R.id.spPais);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        imgPreview = findViewById(R.id.imgPreview);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGaleria = findViewById(R.id.btnGaleria);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVerContactos = findViewById(R.id.btnVerContactos);

        // InputFilter para prohibir números en el campo Nombre en tiempo de escritura
        InputFilter filtroSoloLetras = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isDigit(source.charAt(i))) {
                        return ""; // Bloquea los dígitos
                    }
                }
                return null; // Acepta el carácter
            }
        };
        etNombre.setFilters(new InputFilter[]{filtroSoloLetras});

        // Eventos de botones
        btnTomarFoto.setOnClickListener(v -> abrirCamara());
        btnGaleria.setOnClickListener(v -> abrirGaleria());
        btnGuardar.setOnClickListener(v -> guardarContacto());
        btnVerContactos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListaContactosActivity.class);
            startActivity(intent);
        });
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private String guardarImagenInterna(Bitmap bitmap) {
        File directorio = new File(getFilesDir(), "imagenes_contactos");
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        String nombreArchivo = "IMG_" + System.currentTimeMillis() + ".jpg";
        File archivo = new File(directorio, nombreArchivo);

        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return archivo.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void guardarContacto() {
        String pais = spPais.getSelectedItem().toString();
        int posicionPais = spPais.getSelectedItemPosition();
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String nota = etNota.getText().toString().trim();

        // 1. Validación de País
        if (posicionPais == 0) {
            mostrarAlerta("Campo obligatorio", "Debe seleccionar un país de la lista.");
            return;
        }

        // 2. Validación de Nombre (No vacío, solo letras, espacios, tildes y ñ)
        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            mostrarAlerta("Campo vacío", "Por favor ingrese el nombre del contacto.");
            return;
        }

        // Expresión regular: letras, espacios, vocales con tilde y ñ/Ñ
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            etNombre.setError("Nombre inválido");
            mostrarAlerta("Nombre inválido", "El nombre solo puede contener letras, espacios, tildes y Ñ, sin números ni símbolos.");
            return;
        }

        // 3. Validación de Teléfono (Solo números)
        if (telefono.isEmpty()) {
            etTelefono.setError("El teléfono es obligatorio");
            mostrarAlerta("Campo vacío", "Por favor ingrese el número de teléfono.");
            return;
        }

        if (!telefono.matches("^[0-9]+$")) {
            etTelefono.setError("Teléfono inválido");
            mostrarAlerta("Teléfono inválido", "El teléfono debe contener únicamente dígitos numéricos.");
            return;
        }

        // 4. Validación de Nota
        if (nota.isEmpty()) {
            etNota.setError("La nota es obligatoria");
            mostrarAlerta("Campo vacío", "Por favor ingrese una nota u observación.");
            return;
        }

        // Crear objeto Contactos e insertar en BD
        Contactos nuevoContacto = new Contactos(pais, nombre, telefono, nota, rutaImagenSeleccionada);
        long idInsertado = dbHelper.insertarContacto(nuevoContacto);

        if (idInsertado != -1) {
            Toast.makeText(this, "Contacto guardado correctamente", Toast.LENGTH_LONG).show();
            limpiarCampos();
        } else {
            mostrarAlerta("Error", "No se pudo guardar el contacto en la base de datos.");
        }
    }

    private void limpiarCampos() {
        spPais.setSelection(0);
        etNombre.setText("");
        etTelefono.setText("");
        etNota.setText("");
        imgPreview.setImageResource(android.R.drawable.ic_menu_camera);
        rutaImagenSeleccionada = "";
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}
