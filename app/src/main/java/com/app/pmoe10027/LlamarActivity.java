package com.app.pmoe10027;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;

public class LlamarActivity extends AppCompatActivity {

    private ImageView imgContactoLlamar;
    private TextView tvNombreLlamar, tvPaisLlamar, tvTelefonoLlamar;

    private String nombre = "";
    private String pais = "";
    private String telefono = "";
    private String imagenRuta = "";
    private String numeroCompleto = "";

    // aqui se solicita la llamada pero solo qued asi como tirando sonido inge sjsjs
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    realizarLlamadaDirecta();
                } else {
                    Toast.makeText(this, "Permiso de llamada denegado. Abriendo marcador...", Toast.LENGTH_SHORT).show();
                    abrirMarcadorDirecto();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);

        imgContactoLlamar = findViewById(R.id.imgContactoLlamar);
        tvNombreLlamar = findViewById(R.id.tvNombreLlamar);
        tvPaisLlamar = findViewById(R.id.tvPaisLlamar);
        tvTelefonoLlamar = findViewById(R.id.tvTelefonoLlamar);
        Button btnLlamar = findViewById(R.id.btnLlamar);
        Button btnCompartir = findViewById(R.id.btnCompartir);
        Button btnVolver = findViewById(R.id.btnVolver);

        // Obtener datos del Intent
        Intent intent = getIntent();
        if (intent != null) {
            nombre = intent.getStringExtra("nombre");
            pais = intent.getStringExtra("pais");
            telefono = intent.getStringExtra("telefono");
            imagenRuta = intent.getStringExtra("imagen");
        }

        if (nombre == null) nombre = "Contacto";
        if (pais == null) pais = "";
        if (telefono == null) telefono = "";

        // Construir número internacional completo sin duplicar prefijos
        numeroCompleto = construirNumeroCompleto(pais, telefono);

        // Mostrar datos en las vistas
        tvNombreLlamar.setText(nombre);
        tvPaisLlamar.setText("País: " + pais);
        tvTelefonoLlamar.setText("Teléfono: " + numeroCompleto);

        // Cargar fotografía
        if (imagenRuta != null && !imagenRuta.isEmpty()) {
            File imgFile = new File(imagenRuta);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imgContactoLlamar.setImageBitmap(bitmap);
            }
        }

        // para ampliar la foto pero la verdad no se funiona porque me cuelga el emulador
        imgContactoLlamar.setOnClickListener(v -> mostrarImagenAmpliada());

        // Eventos
        btnLlamar.setOnClickListener(v -> confirmarLlamada());
        btnCompartir.setOnClickListener(v -> compartirContacto());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void confirmarLlamada() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación de Llamada")
                .setMessage("¿Desea llamar a " + nombre + "?")
                .setPositiveButton("Sí", (dialog, which) -> procesarLlamada())
                .setNegativeButton("No", null)
                .show();
    }

    private void procesarLlamada() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            realizarLlamadaDirecta();
        } else {
            // Solicitar permiso en tiempo de ejecución
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void realizarLlamadaDirecta() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + numeroCompleto));
            startActivity(callIntent);
        } catch (Exception e) {
            e.printStackTrace();
            abrirMarcadorDirecto();
        }
    }

    private void abrirMarcadorDirecto() {
        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + numeroCompleto));
            startActivity(dialIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No se pudo abrir el marcador de llamadas.", Toast.LENGTH_SHORT).show();
        }
    }

    private void compartirContacto() {
        String mensaje = "Contacto: " + nombre + "\nTeléfono: " + numeroCompleto;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(shareIntent, "Compartir contacto vía"));
    }

    private void mostrarImagenAmpliada() {
        if (imagenRuta == null || imagenRuta.isEmpty()) {
            Toast.makeText(this, "El contacto no tiene fotografía guardada.", Toast.LENGTH_SHORT).show();
            return;
        }

        File imgFile = new File(imagenRuta);
        if (!imgFile.exists()) {
            Toast.makeText(this, "No se encontró la imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(20, 20, 20, 20);

        new AlertDialog.Builder(this)
                .setTitle("Fotografía: " + nombre)
                .setView(imageView)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // Extrae el código entre paréntesis, ej. "Honduras (+504)" -> "+504"
    public static String obtenerCodigoPais(String paisTexto) {
        if (paisTexto == null) return "";
        int inicio = paisTexto.indexOf("(");
        int fin = paisTexto.indexOf(")");
        if (inicio != -1 && fin != -1 && fin > inicio) {
            return paisTexto.substring(inicio + 1, fin).trim();
        }
        return "";
    }

    // para construir el numero
    public static String construirNumeroCompleto(String paisTexto, String telefono) {
        if (telefono == null) return "";
        String telLimpio = telefono.trim();
        if (telLimpio.startsWith("+")) {
            return telLimpio;
        }
        String codigo = obtenerCodigoPais(paisTexto);
        if (!codigo.isEmpty() && !telLimpio.startsWith(codigo)) {
            return codigo + telLimpio;
        }
        return telLimpio;
    }
}
